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

import com.opengrave.og.models.DAEVertexWeighted;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class VertexAnimated extends VertexData {

	public float x, y, z;
	public float nx, ny, nz;
	public float r, g, b;
	public float tx, ty, tz;
	public DAEVertexWeighted influence;

	public VertexAnimated() {
	}

	public VertexAnimated(float x, float y, float z, float nx, float ny, float nz, float r, float g, float b, float tx, float ty, float tz) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		this.r = r;
		this.g = g;
		this.b = b;
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
	}

	public void setPos(Vector4f v) {
		// Throw away w. Because
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	public void setNorm(Vector4f v) {
		this.nx = v.x;
		this.ny = v.y;
		this.nz = v.z;
	}

	public void setTex(Vector4f v) {
		this.tx = v.x;
		this.ty = v.y;
		this.tz = v.z;
	}

	public void setInfluences(DAEVertexWeighted influence) {
		this.influence = influence;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(b);
		result = prime * result + Float.floatToIntBits(g);
		result = prime * result + ((influence == null) ? 0 : influence.hashCode());
		result = prime * result + Float.floatToIntBits(nx);
		result = prime * result + Float.floatToIntBits(ny);
		result = prime * result + Float.floatToIntBits(nz);
		result = prime * result + Float.floatToIntBits(r);
		result = prime * result + Float.floatToIntBits(tx);
		result = prime * result + Float.floatToIntBits(ty);
		result = prime * result + Float.floatToIntBits(tz);
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
		if (!(obj instanceof VertexAnimated))
			return false;
		VertexAnimated other = (VertexAnimated) obj;
		if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
			return false;
		if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
			return false;
		if (influence == null) {
			if (other.influence != null)
				return false;
		} else if (!influence.equals(other.influence))
			return false;
		if (Float.floatToIntBits(nx) != Float.floatToIntBits(other.nx))
			return false;
		if (Float.floatToIntBits(ny) != Float.floatToIntBits(other.ny))
			return false;
		if (Float.floatToIntBits(nz) != Float.floatToIntBits(other.nz))
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
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}

	public Vector3f getPos() {
		return new Vector3f(x, y, z);
	}
}
