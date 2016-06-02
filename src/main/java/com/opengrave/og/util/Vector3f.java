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
package com.opengrave.og.util;

public class Vector3f {
	public float x, y, z;

	public static final Vector3f ZERO = new Vector3f(0f);
	public static final Vector3f RIGHT = new Vector3f(1, 0, 0);
	public static final Vector3f LEFT = new Vector3f(-1, 0, 0);
	public static final Vector3f UP = new Vector3f(0, 1, 0);
	public static final Vector3f DOWN = new Vector3f(0, -1, 0);
	public static final Vector3f FORWARD = new Vector3f(0, 0, -1);
	public static final Vector3f BACK = new Vector3f(0, 0, 1);

	public Vector3f() {
		set(0, 0, 0);
	}

	public Vector3f(float v) {
		set(v, v, v);
	}

	public Vector3f(float x, float y, float z) {
		set(x, y, z);
	}

	public Vector3f(Vector2f vec, float z) {
		set(vec, z);
	}

	public Vector3f(Vector3f vec) {
		set(vec);
	}

	public float x() {
		return x;
	}

	public Vector3f x(float x) {
		this.x = x;
		return this;
	}

	public float y() {
		return y;
	}

	public Vector3f y(float y) {
		this.y = y;
		return this;
	}

	public float z() {
		return z;
	}

	public Vector3f z(float z) {
		this.z = z;
		return this;
	}

	public boolean equals(Vector3f v) {
		return x == v.x && y == v.y && z == v.z;
	}

	@Override
	public int hashCode() {
		return (int) (x * (2 << 4) + y * (2 << 2) + z);
	}

	public Vector3f set(float f) {
		return set(f, f, f);
	}

	public Vector3f set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vector3f set2(Vector2f vec) {
		return set(vec.x(), vec.y(), 0);
	}

	public Vector3f set(Vector2f vec, float z) {
		return set(vec.x(), vec.y(), z);
	}

	public Vector3f set(Vector3f vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
		return this;
	}

	public Vector3f set4(Vector4f vec) {
		return set(vec.x(), vec.y(), vec.z());
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public float lengthSquared() {
		return x * x + y * y + z * z;
	}

	public Vector3f normalise(Vector3f res) {
		if (res == null) {
			res = new Vector3f();
		}
		float length = 1f / length();
		res.x = x * length;
		res.y = y * length;
		res.z = z * length;
		return this;
	}

	public float dot(Vector3f vec) {
		return x * vec.x + y * vec.y + z * vec.z;
	}

	public Vector3f cross(Vector3f vec, Vector3f res) {
		if (res == null) {
			res = new Vector3f();
		}
		return res.set(y * vec.z - vec.y * z, z * vec.x - vec.z * x, x * vec.y - vec.x * y);
	}

	public Vector3f add(float x, float y, float z, Vector3f res) {
		if (res == null) {
			res = new Vector3f();
		}
		res.x = this.x + x;
		res.y = this.y + y;
		res.z = this.z + z;
		return res;
	}

	public Vector3f add(Vector3f vec, Vector3f res) {
		return add(vec.x, vec.y, vec.z, res);
	}

	public Vector3f sub(float x, float y, float z, Vector3f res) {
		if (res == null) {
			res = new Vector3f();
		}
		res.x = this.x - x;
		res.y = this.y - y;
		res.z = this.z - z;
		return res;
	}

	public Vector3f sub(Vector3f vec, Vector3f res) {
		return sub(vec.x, vec.y, vec.z, res);
	}

	public Vector3f mult(float f, Vector3f res) {
		return mult(f, f, f, res);
	}

	public Vector3f mult(float x, float y, float z, Vector3f res) {
		if (res == null) {
			res = new Vector3f();
		}
		res.x = this.x * x;
		res.y = this.y * y;
		res.z = this.z * z;
		return res;
	}

	public Vector3f mult(Vector3f vec, Vector3f res) {
		return mult(vec.x, vec.y, vec.z, res);
	}

	public Vector3f divide(float f, Vector3f res) {
		return divide(f, f, f, res);
	}

	public Vector3f divide(float x, float y, float z, Vector3f res) {
		if (res == null) {
			res = new Vector3f();
		}
		res.x = this.x / x;
		res.y = this.y / y;
		res.z = this.z / z;
		return res;
	}

	public Vector3f divide(Vector3f vec, Vector3f res) {
		return divide(vec.x, vec.y, vec.z, res);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public Vector3f negate() {
		return new Vector3f(-x, -y, -z);
	}
}
