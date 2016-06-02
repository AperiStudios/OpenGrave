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

public class VertexMultiTex extends VertexData {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(nx);
		result = prime * result + Float.floatToIntBits(ny);
		result = prime * result + Float.floatToIntBits(nz);
		result = prime * result + tex;
		result = prime * result + Float.floatToIntBits(tx);
		result = prime * result + Float.floatToIntBits(ty);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof VertexMultiTex))
			return false;
		VertexMultiTex other = (VertexMultiTex) obj;
		if (Float.floatToIntBits(nx) != Float.floatToIntBits(other.nx))
			return false;
		if (Float.floatToIntBits(ny) != Float.floatToIntBits(other.ny))
			return false;
		if (Float.floatToIntBits(nz) != Float.floatToIntBits(other.nz))
			return false;
		if (tex != other.tex)
			return false;
		if (Float.floatToIntBits(tx) != Float.floatToIntBits(other.tx))
			return false;
		if (Float.floatToIntBits(ty) != Float.floatToIntBits(other.ty))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}

	public float x, y, z, tx, ty, nx, ny, nz;
	public int tex;

	public VertexMultiTex(float x, float y, float z, float tx, float ty, int tex, float nx, float ny, float nz) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.tx = tx;
		this.ty = ty;
		this.tex = tex;
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
	}

	public VertexMultiTex() {
	}
}
