/*
 * Copyright 2016 Nathan Howard
 * 
 * This file is part of OpenGrave
 * 
 * OpenGrave is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenGrave is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenGrave. If not, see <http://www.gnu.org/licenses/>.
 */
package com.opengrave.og.terrain;

import java.io.IOException;
import java.util.ArrayList;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.world.CommonAreaLoc;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.base.RenderableMultitex;
import com.opengrave.og.base.VertexMultiTex;
import com.opengrave.og.engine.Location;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.TextureAtlas;
import com.opengrave.og.resources.TextureEditable;
import com.opengrave.og.resources.TextureEditableDeferedChanges;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

public class TerrainLayer extends RenderableMultitex implements Pickable {

	TerrainWorld world = null; // The world this belongs to.
	CommonAreaLoc loc = null; // The location inside of the world
	public static final int size = 64;
	// private ArrayList<TerrainVertex> vertList = new ArrayList<TerrainVertex>( size * size);
	// private TerrainVertex[] vertList = new TerrainVertex[size * size];

	private float[] height = new float[size * size];
	private float[] r = new float[size * size];
	private float[] g = new float[size * size];
	private float[] b = new float[size * size];
	private float[] nx = new float[size * size];
	private float[] ny = new float[size * size];
	private float[] nz = new float[size * size];
	private int[] texture = new int[size * size];

	private int layerNumber;
	private RenderStyle style;
	private TextureEditable colourtexture;
	public static int detail = 0;

	private ArrayList<TextureEditableDeferedChanges> colChange = new ArrayList<TextureEditableDeferedChanges>();

	public TextureAtlas getTextureList() {
		return textureAtlas;
	}

	public void show() {
	}

	public void hide() {
	}

	public TerrainLayer(TerrainWorld world, CommonAreaLoc loc, int layerNumber, OGInputStream stream) {
		this.layerNumber = layerNumber;
		this.world = world;
		this.loc = loc;
		if (stream == null) {
			randomise();
		} else {
			try {

				for (int x = 0; x < size; x++) {
					for (int y = 0; y < size; y++) {
						int i = x + (y * size);
						Vector4f col = stream.readVector4f();
						float h = stream.readFloat();
						int t = stream.readInt();
						setFloorRGB(x, y, col);
						height[i] = h;
						texture[i] = t;
					}
				}
				for (int x = 0; x < size; x++) {
					for (int y = 0; y < size; y++) {
						calculateNormal(x, y);
					}
				}
			} catch (IOException e) {
				System.out.println("Bad map format. Old or possible malformed. Using random data");
				randomise();
			}

		}
		textureAtlas = world.getTextures();
		normalAtlas = world.getNormalTextures();
	}

	public void save(OGOutputStream stream) {
		try {
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					int i = x + (y * size);
					stream.writeVector4f(new Vector4f(r[i], g[i], b[i], 1f));
					stream.writeFloat(height[i]);
					stream.writeInt(texture[i]);
				}
			}
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}

	}

	public void randomise() {
		Vector4f col = new Vector4f((float) Math.random(), (float) Math.random(), (float) Math.random(), 1f);
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				int i = x + (y * size);
				setFloorRGB(x, y, col);
				height[i] = layerNumber;
				nz[i] = 1f;
			}
		}
	}

	@Override
	public void recreate() {
		int maxTex = this.textureAtlas.size(), minTex = 0;
		for (int x = 0; x < size - 1; x++) {
			for (int y = 0; y < size - 1; y++) {
				int i0 = x + (y * size);
				int i1 = (x + 1) + (y * size);
				int i2 = (x + 1) + ((y + 1) * size);
				int i3 = x + ((y + 1) * size);
				if (texture[i2] < minTex || texture[i2] > maxTex) {
					// Bottom right missing, check if a top left is needed
					if (texture[i0] < minTex || texture[i0] > maxTex || texture[i1] < minTex || texture[i1] > maxTex || texture[i3] < minTex
							|| texture[i3] > maxTex) {
						continue;
					}
					addVertexToBuffer(x, y, i0);
					addVertexToBuffer(x + 1, y, i1);
					addVertexToBuffer(x, y + 1, i3);
					continue;
				} else if (texture[i0] < minTex || texture[i0] > maxTex) {
					// Top Left is missing, check is a bottom right is needed
					if (texture[i2] < minTex || texture[i2] > maxTex || texture[i1] < minTex || texture[i1] > maxTex || texture[i3] < minTex
							|| texture[i3] > maxTex) {
						continue;
					}
					addVertexToBuffer(x, y + 1, i3);
					addVertexToBuffer(x + 1, y, i1);
					addVertexToBuffer(x + 1, y + 1, i2);
					continue;
				}
				if (!(texture[i1] < minTex || texture[i1] > maxTex)) {
					addVertexToBuffer(x, y, i0);
					addVertexToBuffer(x + 1, y, i1);
					addVertexToBuffer(x + 1, y + 1, i2);
				}
				if (!(texture[i3] < minTex || texture[i3] > maxTex)) {
					addVertexToBuffer(x, y, i0);
					addVertexToBuffer(x + 1, y + 1, i2);
					addVertexToBuffer(x, y + 1, i3);
				}
			}
		}
	}

	private void addVertexToBuffer(int x, int y, int i) {
		addVertex(new VertexMultiTex(x, y, height[i], x, y, texture[i], nx[i], ny[i], nz[i]));
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void delete() {
		// TODO Delete links to this object
	}

	public void setFloorRGB(int x, int y, Vector4f col) {
		if (colourtexture != null) {
			this.colourtexture.setColourAt(x + 1, y + 1, col);
		} else {
			colChange.add(new TextureEditableDeferedChanges(x + 1, y + 1, col));
		}
		if (x < 0 || y < 0 || x >= 64 || y >= 64) {
			return;
		}
		int i = (x) + ((y) * size);
		r[i] = col.x;
		g[i] = col.y;
		b[i] = col.z;
	}

	public int getLayer() {
		return layerNumber;
	}

	public void setRenderStyle(RenderStyle style) {
		this.style = style;
	}

	public void render(Matrix4f offset) {
		render(offset, style);
	}

	/**
	 * Only to be used on init. Will destroy all data in layer to be more
	 * helpful for creators
	 * 
	 * @param i
	 */
	public void setAllTextures(int in) {
		for (int x = 0; x < size - 1; x++) {
			for (int y = 0; y < size - 1; y++) {
				int i = x + (y * size);
				height[i] = 1f;
				texture[i] = in;
			}
		}
	}

	public float getHeightAt(Location loc) {
		// TODO getHeight... Not perfect, assume N-W most tile height will work for all the tile
		int x = (int) loc.getTileX();
		int y = (int) loc.getTileY();
		int i = x + (y * size);
		return height[i];
	}

	public void alter(int x, int y, TerrainLayerAlteration alter) {
		int i = x + (y * size);
		if (alter.setColour) {
			setFloorRGB(x, y, alter.col);
		}
		if (alter.setHeight) {
			height[i] = alter.height;
			// Change normals
			calculateNormal(x - 1, y);
			calculateNormal(x, y);
			calculateNormal(x + 1, y);
			calculateNormal(x, y - 1);
			calculateNormal(x, y + 1);

			changed = true;
		}
		if (alter.setTexture) {
			texture[i] = alter.tex;
			changed = true;
		}
	}

	private void calculateNormal(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size) {
			return;
		}
		int lefti = (x - 1) + (y * size), righti = (x + 1) + (y * size), upi = x + ((y - 1) * size), downi = x + ((y + 1) * size);
		boolean hasLeft = (x > 0 && texture[lefti] != -1), hasRight = (x < size - 1 && texture[righti] != -1), hasUp = (y > 0 && texture[upi] != -1), hasDown = (y < size - 1 && texture[downi] != -1);
		int thisi = x + (y * size);
		float sx = height[hasRight ? righti : thisi] - height[hasLeft ? lefti : thisi];
		if (!hasRight || !hasLeft) {
			sx *= 2f;
		}
		float sy = height[hasDown ? downi : thisi] - height[hasUp ? upi : thisi];
		if (!hasUp || !hasDown) {
			sy *= 2f;
		}
		nx[thisi] = -sx;
		ny[thisi] = -sy;
		nz[thisi] = 2f;
		changed = true;
	}

	@Override
	public TextureEditable getColourTexture() {
		if (colourtexture == null) {
			colourtexture = new TextureEditable(size + 2, 1f, 1f, 1f, 1f);
			if (colChange.size() > 0) {
				colourtexture.dumpChanges(colChange);
			}
		}
		return colourtexture;
	}

}
