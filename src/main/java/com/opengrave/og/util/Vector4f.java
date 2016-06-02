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

public class Vector4f {
	public float x, y, z, w;

	public static final Vector4f RIGHT = new Vector4f(1, 0, 0, 1);
	public static final Vector4f LEFT = new Vector4f(-1, 0, 0, 1);
	public static final Vector4f UP = new Vector4f(0, 1, 0, 1);
	public static final Vector4f DOWN = new Vector4f(0, -1, 0, 1);
	public static final Vector4f FORWARD = new Vector4f(0, 0, -1, 1);
	public static final Vector4f BACK = new Vector4f(0, 0, 1, 1);

	public Vector4f() {
		set(0, 0, 0, 0);
	}

	public Vector4f(float v) {
		this(v, v, v, v);
	}

	public Vector4f(float x, float y, float z, float w) {
		set(x, y, z, w);
	}

	public Vector4f(Vector2f vec, float z, float w) {
		set(vec, z, w);
	}

	public Vector4f(Vector3f vec, float w) {
		set(vec, w);
	}

	public Vector4f(Vector4f vec) {
		set(vec);
	}

	public float x() {
		return x;
	}

	public Vector4f x(float x) {
		this.x = x;
		return this;
	}

	public float y() {
		return y;
	}

	public Vector4f y(float y) {
		this.y = y;
		return this;
	}

	public float z() {
		return z;
	}

	public Vector4f z(float z) {
		this.z = z;
		return this;
	}

	public float w() {
		return w;
	}

	public Vector4f w(float w) {
		this.w = w;
		return this;
	}

	public boolean equals(Vector4f v) {
		return x == v.x && y == v.y && z == v.z && w == v.w;
	}

	public Vector4f set(float f) {
		return set(f, f, f, f);
	}

	public Vector4f set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	public Vector4f set2(Vector2f vec) {
		return set(vec, 0, 0);
	}

	public Vector4f set(Vector2f vec, float z, float w) {
		return set(vec.x(), vec.y(), z, w);
	}

	public Vector4f set3(Vector3f vec) {
		return set(vec, 0);
	}

	public Vector4f set(Vector3f vec, float w) {
		return set(vec.x(), vec.y(), vec.z(), w);
	}

	public Vector4f set(Vector4f vec) {
		x = vec.x;
		y = vec.y;
		z = vec.z;
		w = vec.w;
		return this;
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public float lengthSquared() {
		return x * x + y * y + z * z + w * w;
	}

	public Vector4f normalise() {
		float length = 1f / length();
		x *= length;
		y *= length;
		z *= length;
		w *= length;
		return this;
	}

	public float dot(Vector4f vec) {
		return x * vec.x + y * vec.y + z * vec.z + w * vec.w;
	}

	public Vector4f add(float x, float y, float z, float w, Vector4f res) {
		res.x = this.x + x;
		res.y = this.y + y;
		res.z = this.z + z;
		res.w = this.w + w;
		return res;
	}

	public Vector4f add(Vector4f vec, Vector4f res) {
		return add(vec.x, vec.y, vec.z, vec.w, res);
	}

	public Vector4f sub(float x, float y, float z, float w, Vector4f res) {
		if (res == null) {
			res = new Vector4f();
		}
		res.x = this.x - x;
		res.y = this.y - y;
		res.z = this.z - z;
		res.w = this.w - w;
		return res;
	}

	public Vector4f sub(Vector4f vec, Vector4f res) {
		return sub(vec.x, vec.y, vec.z, vec.w, res);
	}

	public Vector4f mult(float f, Vector4f res) {
		return mult(f, f, f, f, res);
	}

	public Vector4f mult(float x, float y, float z, float w, Vector4f res) {
		if (res == null) {
			res = new Vector4f();
		}
		res.x = this.x * x;
		res.y = this.y * y;
		res.z = this.z * z;
		res.w = this.w * w;
		return this;
	}

	public Vector4f mult(Vector4f vec, Vector4f res) {
		return mult(vec.x, vec.y, vec.z, vec.w, res);
	}

	public Vector4f divide(float f, Vector4f res) {
		return divide(f, f, f, f, res);
	}

	public Vector4f divide(float x, float y, float z, float w, Vector4f res) {
		if (res == null) {
			res = new Vector4f();
		}
		res.x = this.x / x;
		res.y = this.y / y;
		res.z = this.z / z;
		res.w = this.w / w;
		return res;
	}

	public Vector4f divide(Vector4f vec, Vector4f res) {
		return divide(vec.x, vec.y, vec.z, vec.w, res);
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}

}
