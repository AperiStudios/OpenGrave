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
package com.opengrave.og.engine;

import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class BoundingBox {

	Vector3f lowest = null, highest = null;

	public void addVector3f(Vector3f vec) {
		if (lowest == null || highest == null) {
			lowest = copy(vec);
			highest = copy(vec);
		}
		if (vec.x < lowest.x) {
			lowest.x = vec.x;
		}
		if (vec.x > highest.x) {
			highest.x = vec.x;
		}
		if (vec.y < lowest.y) {
			lowest.y = vec.y;
		}
		if (vec.y > highest.y) {
			highest.y = vec.y;
		}
		if (vec.z < lowest.z) {
			lowest.z = vec.z;
		}
		if (vec.z > highest.z) {
			highest.z = vec.z;
		}
	}

	private Vector3f copy(Vector3f vec) {
		return new Vector3f(vec.x, vec.y, vec.z);
	}

	public void clear() {
		lowest = null;
		highest = null;
	}

	public Vector3f getLowest() {
		if (lowest == null) {
			return new Vector3f(0f, 0f, 0f);
		}
		return lowest;
	}

	public Vector3f getHighest() {
		if (highest == null) {
			return new Vector3f(1f, 1f, 1f);
		}
		return highest;
	}

	public void addVector4f(Vector4f v) {
		Vector3f vec = new Vector3f(v.x * v.w, v.y * v.w, v.z * v.w);
		addVector3f(vec);
		;

	}
}
