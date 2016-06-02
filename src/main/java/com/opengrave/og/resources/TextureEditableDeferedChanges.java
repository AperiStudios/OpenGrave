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
package com.opengrave.og.resources;

import com.opengrave.og.util.Vector4f;

public class TextureEditableDeferedChanges {

	private int x, y;
	private Vector4f col;

	public TextureEditableDeferedChanges(int x, int y, Vector4f col) {
		this.x = x;
		this.y = y;
		this.col = col;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Vector4f getColour() {
		return col;
	}

	public void setColour(Vector4f col) {
		this.col = col;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TextureEditableDeferedChanges))
			return false;
		TextureEditableDeferedChanges other = (TextureEditableDeferedChanges) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}
