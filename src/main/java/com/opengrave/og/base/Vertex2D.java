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

public class Vertex2D extends VertexData {

	float x, y, tx, ty, tz, r, g, b, a;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(a);
		result = prime * result + Float.floatToIntBits(b);
		result = prime * result + Float.floatToIntBits(g);
		result = prime * result + Float.floatToIntBits(r);
		result = prime * result + Float.floatToIntBits(tx);
		result = prime * result + Float.floatToIntBits(ty);
		result = prime * result + Float.floatToIntBits(tz);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Vertex2D))
			return false;
		Vertex2D other = (Vertex2D) obj;
		if (Float.floatToIntBits(a) != Float.floatToIntBits(other.a))
			return false;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
			return false;
		if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
			return false;
		if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r))
			return false;
		if (Float.floatToIntBits(tx) != Float.floatToIntBits(other.tx))
			return false;
		if (Float.floatToIntBits(ty) != Float.floatToIntBits(other.ty))
			return false;
		if (Float.floatToIntBits(tz) != Float.floatToIntBits(other.tz))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	public Vertex2D(float x, float y, float tx, float ty, float tz, float r, float g, float b, float a) {
		this.x = x;
		this.y = y;
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
}
