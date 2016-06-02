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
import com.opengrave.og.base.RenderableLiquid;
import com.opengrave.og.base.VertexLiquid;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.TextureAtlas;
import com.opengrave.og.resources.TextureEditable;
import com.opengrave.og.resources.TextureEditableDeferedChanges;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

public class TerrainLiquidLayer extends RenderableLiquid {
	public static final int size = 64;
	// ArrayList<TerrainLiquidVertex> vertList = new ArrayList<TerrainLiquidVertex>();
	private float[] height = new float[size * size];
	private float[] r = new float[size * size];
	private float[] g = new float[size * size];
	private float[] b = new float[size * size];
	private float[] nx = new float[size * size];
	private float[] ny = new float[size * size];
	private float[] nz = new float[size * size];
	private int[] texture = new int[size * size];
	private float[] flowx = new float[size * size];
	private float[] flowy = new float[size * size];
	private float[] flowlocx = new float[size * size];
	private float[] flowlocy = new float[size * size];

	private RenderStyle style;
	private TerrainWorld world;
	private CommonAreaLoc loc;
	private int layerNumber;
	private TextureEditable flowtexture;
	private TextureEditable colourtexture;
	private ArrayList<TextureEditableDeferedChanges> colChange = new ArrayList<TextureEditableDeferedChanges>();
	private ArrayList<TextureEditableDeferedChanges> flowChange = new ArrayList<TextureEditableDeferedChanges>();

	public TerrainLiquidLayer(TerrainWorld world, CommonAreaLoc locationInWorld, int layer, OGInputStream stream) {
		this.world = world;
		this.loc = locationInWorld;
		this.layerNumber = layer;
		this.textureAtlas = (TextureAtlas) world.getLiquidTextures();
		this.normalAtlas = (TextureAtlas) world.getLiquidNormTextures();
		if (stream == null) {
			randomise();
		} else {
			try {
				for (int x = 0; x < TerrainLayer.size; x++) {
					for (int y = 0; y < TerrainLayer.size; y++) {
						// TerrainLiquidEditableVertex tVtx = getVertexAt(x, y, true);
						int i = x + (y * size);
						Vector4f col = stream.readVector4f();
						setColour(x, y, col);
						height[i] = stream.readFloat();
						texture[i] = stream.readInt();
						flowx[i] = stream.readFloat();
						flowy[i] = stream.readFloat();
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
	}

	private void randomise() {
		Vector4f waterCol = new Vector4f(0f, 0.2f, 1f, 1f);
		for (int x = 0; x < TerrainLayer.size; x++) {
			for (int y = 0; y < TerrainLayer.size; y++) {
				int i = x + (y * size);
				height[i] = 1f * layerNumber;
				setColour(x, y, waterCol);
				texture[i] = 1;
				flowx[i] = 0f;
				flowy[i] = 0f;
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
		addVertex(new VertexLiquid(x, y, height[i], texture[i], nx[i], ny[i], nz[i]));
	}

	@Override
	public void update(float delta) {
		delta = delta * 0.005f;
		for (int x = 0; x < TerrainLayer.size - 1; x++) {
			for (int y = 0; y < TerrainLayer.size - 1; y++) {
				int i = x + (y * size);
				if (texture[i] < 0) {
					continue;
				}
				flowlocx[i] = (flowlocx[i] + (flowx[i] * delta)) % 1f;
				flowlocy[i] = (flowlocy[i] + (flowy[i] * delta)) % 1f;
				setLastLiquidFlow(x + 1, y + 1, flowlocx[i], flowlocy[i]);
			}
		}
		changed = true;
	}

	public void setLastLiquidFlow(int x, int y, float fx, float fy) {
		if (flowtexture != null) {
			this.flowtexture.setColourAt(x, y, new Vector4f(fx, fy, 0f, 0f));
		} else {
			flowChange.add(new TextureEditableDeferedChanges(x, y, new Vector4f(fx, fy, 0f, 0f)));
		}
	}

	public void setColour(int x, int y, Vector4f col) {
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

	public void render(Matrix4f offset) {
		render(offset, style);
	}

	public void setRenderStyle(RenderStyle renderStyle) {
		style = renderStyle;
	}

	public void setAllTextures(int in) {
		for (int i = 0; i < texture.length; i++) {
			texture[i] = in;
		}
	}

	public void save(OGOutputStream stream) {
		try {
			for (int x = 0; x < TerrainLayer.size; x++) {
				for (int y = 0; y < TerrainLayer.size; y++) {
					// TerrainLiquidEditableVertex tVtx = getVertexAt(x, y, true);
					int i = x + (y * size);
					stream.writeVector4f(new Vector4f(r[i], g[i], b[i], 1f));
					stream.writeFloat(height[i]);
					stream.writeInt(texture[i]);
					stream.writeFloat(flowx[i]);
					stream.writeFloat(flowy[i]);
				}
			}
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}
	}

	public void alter(int x, int y, TerrainLiquidLayerAlteration alter) {
		int i = x + (y * size);
		if (alter.setColour) {
			setColour(x, y, alter.col);
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
			texture[i] = alter.texture;
			changed = true;
		}
		if (alter.setFlow) {
			flowx[i] = alter.flowx;
			flowy[i] = alter.flowy;
		}
	}

	public float getCurrentFlowXAt(int x, int y) {
		return flowlocx[x + (y * size)];
	}

	public float getCurrentFlowYAt(int x, int y) {
		return flowlocy[x + (y * size)];
	}

	public int getTextureAt(int x, int y) {
		int i = x + (y * size);
		return texture[i];
	}

	private void calculateNormal(int x, int y) {
		if (x < 0 || x > size - 1 || y < 0 || y > size - 1) {
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
	public TextureEditable getFlowTexture() {
		if (flowtexture == null) {
			flowtexture = new TextureEditable(size + 2, 1f, 1f, 1f, 1f);
			if (flowChange.size() > 0) {
				flowtexture.dumpChanges(flowChange);
			}
		}
		return flowtexture;
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
