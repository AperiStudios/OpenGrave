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

import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.og.util.Vector4f;

public class Material {

	public Material(OGInputStream in) throws IOException {
		texName = in.readString();
		texDataName = in.readString();
		colour = in.readVector4f();
	}

	public void save(OGOutputStream out) throws IOException {
		out.writeString(texName);
		out.writeString(texDataName);
		out.writeVector4f(colour);
	}

	public Material(String tex, String texData, Vector4f col) {
		texName = tex;
		texDataName = texData;
		colour = col;

	}

	private Vector4f colour = new Vector4f();
	private int textureIndex = 0;
	private int textureDataIndex = 0;
	private String texName, texDataName;

	public Vector4f getColour() {
		return colour;
	}

	public int getTextureIndex() {
		return textureIndex;
	}

	public int getTextureDataIndex() {
		return textureDataIndex;
	}

	public String getTextureName() {
		return texName;
	}

	public String getTextureDataName() {
		return texDataName;
	}

	public void setTextureIndex(int index) {
		textureIndex = index;
	}

	public void setTextureDataIndex(int index) {
		textureDataIndex = index;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result + ((texDataName == null) ? 0 : texDataName.hashCode());
		result = prime * result + ((texName == null) ? 0 : texName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Material))
			return false;
		Material other = (Material) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (texDataName == null) {
			if (other.texDataName != null)
				return false;
		} else if (!texDataName.equals(other.texDataName))
			return false;
		if (texName == null) {
			if (other.texName != null)
				return false;
		} else if (!texName.equals(other.texName))
			return false;
		return true;
	}

	public void setTexture(String texture) {
		this.texName = texture;
	}

	public void setDataTexture(String texture) {
		this.texDataName = texture;
	}

	public void setColour(Vector4f col) {
		colour = col;
	}
}
