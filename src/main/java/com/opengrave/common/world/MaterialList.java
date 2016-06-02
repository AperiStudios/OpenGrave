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
package com.opengrave.common.world;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.resources.TextureAtlas;

public class MaterialList {

	ArrayList<Material> matList = new ArrayList<Material>();
	ArrayList<String> textureNames = new ArrayList<String>(), textureDataNames = new ArrayList<String>();
	TextureAtlas textureAtlas, textureDataAtlas;
	boolean changed = false;

	// String label;

	public MaterialList() {
		changed = true;
		/*
		 * this.label = label;
		 * for (String mat : label.split(":")) {
		 * String[] data = mat.split("#");
		 * String texture = data[0];
		 * Vector4f col = new Vector4f(0.5f, 0.5f, 0.5f, 1f);
		 * if (data.length == 2) {
		 * col = GUIXML.getColour("#" + data[1]);
		 * }
		 * if (texture == null) {
		 * texture = "blank";
		 * }
		 * if (!textureNames.contains(texture)) {
		 * textureNames.add(texture);
		 * }
		 * int texNum = textureNames.indexOf(texture);
		 * addMaterial(texNum, col);
		 * }
		 */
	}

	public MaterialList(OGInputStream in) throws IOException {
		changed = true;
		int number = in.readInt();
		for (int i = 0; i < number; i++) {
			addMaterial(new Material(in));
		}
	}

	public synchronized void save(OGOutputStream out) throws IOException {
		out.writeInt(matList.size());
		for (Material m : matList) {
			m.save(out);
		}
	}

	public synchronized int addMaterial(Material m) {
		changed = true;
		int i = matList.indexOf(m);
		if (i == -1) {
			matList.add(m);
			redoTextureList();
			return matList.size() - 1;
		}
		return i;
	}

	public synchronized int addMaterialForce(Material m) {
		changed = true;
		matList.add(m);
		redoTextureList();
		return matList.size() - 1;

	}

	public synchronized ArrayList<Material> all() {
		return matList;
	}

	// public TextureAtlas getTextureAtlas() {
	// updateTex();
	// return textureAtlas;
	// }

	// public TextureAtlas getTextureDataAtlas() {
	// updateTex();
	// return textureDataAtlas;
	// }

	private synchronized void updateTex() {
		if (changed) {
			changed = false;
			textureAtlas = Resources.loadTextures(textureNames);
			textureDataAtlas = Resources.loadTextures(textureDataNames);
		}
	}

	public synchronized boolean valid() {
		updateTex();
		return textureAtlas != null && textureDataAtlas != null;
	}

	public synchronized void bind(int pID, int glTexture) {
		updateTex();
		int texMain = GL20.glGetUniformLocation(pID, "arraytexture");
		int texData = GL20.glGetUniformLocation(pID, "arraytexturedata");
		GL20.glUniform1i(texMain, glTexture - GL13.GL_TEXTURE0);
		GL20.glUniform1i(texData, (glTexture - GL13.GL_TEXTURE0) + 1);
		if (!valid()) {
			return;
		}
		textureAtlas.bind(glTexture);
		textureDataAtlas.bind(glTexture + 1);
	}

	public synchronized void unbind() {
		textureAtlas.unbind();
		textureDataAtlas.unbind();
	}

	public synchronized Material getMaterial(int i) {
		if (i < 0) {
			return null;
		}
		if (i >= matList.size()) {
			return null;
		}
		return matList.get(i);
	}

	private synchronized void redoTextureList() {
		changed = true;
		textureNames.clear();
		textureDataNames.clear();
		for (Material m : matList) {
			// Set the Texture Indecies
			String tN = m.getTextureName();
			if (tN != null && !textureNames.contains(tN)) {
				textureNames.add(tN);
			}
			m.setTextureIndex(textureNames.indexOf(tN));

			String tDN = m.getTextureDataName();
			if (tDN != null && !textureDataNames.contains(tDN)) {
				textureDataNames.add(tDN);
			}
			m.setTextureDataIndex(textureDataNames.indexOf(tDN));
		}
	}

	public synchronized void setTexture(int number, String texture) {
		if (number < 0) {
			return;
		}
		Material m = getMaterial(number);
		if (m != null) {
			m.setTexture(texture);
		}
		redoTextureList();
	}

	public synchronized void setDataTexture(int number, String texture) {
		if (number < 0) {
			return;
		}
		Material m = getMaterial(number);
		if (m != null) {
			m.setDataTexture(texture);
		}
		redoTextureList();
	}

}
