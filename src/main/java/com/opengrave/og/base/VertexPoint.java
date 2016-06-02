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
package com.opengrave.og.base;

import com.opengrave.og.engine.Location;
import com.opengrave.og.util.Vector4f;

public class VertexPoint extends VertexData {

	public VertexPoint(float x, float y, float z, float r, float g, float b, float a, float size, float texture) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.size = size;
		this.texture = texture;
	}

	public VertexPoint() {
	}

	float x, y, z, r, g, b, a, size, texture;

	public void setPos(Location loc) {
		this.x = loc.getFullXAsFloat();
		this.y = loc.getFullYAsFloat();
		this.z = loc.getZ();
	}

	public void setPos(Vector4f vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
	}

	public void setCol(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
}
