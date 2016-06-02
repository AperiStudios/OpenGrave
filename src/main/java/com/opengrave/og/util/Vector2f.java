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

public class Vector2f {
	public float x, y;

	public static final Vector2f RIGHT = new Vector2f(1, 0);
	public static final Vector2f LEFT = new Vector2f(-1, 0);
	public static final Vector2f UP = new Vector2f(0, 1);
	public static final Vector2f DOWN = new Vector2f(0, -1);

	public Vector2f() {
		this(0, 0);
	}

	public Vector2f(float v) {
		this(v, v);
	}

	public Vector2f(float x, float y) {
		set(x, y);
	}

	public Vector2f(Vector2f vec) {
		set(vec);
	}

	public float x() {
		return x;
	}

	public Vector2f x(float x) {
		this.x = x;
		return this;
	}

	public float y() {
		return y;
	}

	public Vector2f y(float y) {
		this.y = y;
		return this;
	}

	public boolean equals(Vector2f v) {
		return x == v.x && y == v.y;
	}

	public Vector2f set(float f) {
		return set(f, f);
	}

	public Vector2f set(float x, float y) {
		this.x = x;
		this.y = y;
		return this;
	}

	public Vector2f set(Vector2f vec) {
		x = vec.x;
		y = vec.y;
		return this;
	}

	public Vector2f set(Vector3f vec) {
		return set(vec.x(), vec.y());
	}

	public Vector2f set(Vector4f vec) {
		return set(vec.x(), vec.y());
	}

	public float length() {
		return (float) Math.sqrt(lengthSquared());
	}

	public float lengthSquared() {
		return x * x + y * y;
	}

	public Vector2f normalise() {
		float length = 1f / length();
		x *= length;
		y *= length;
		return this;
	}

	public float dot(Vector2f vec) {
		return x * vec.x + y * vec.y;
	}

	public Vector2f add(float x, float y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Vector2f add(Vector2f vec) {
		return add(vec.x, vec.y);
	}

	public Vector2f sub(float x, float y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	public Vector2f sub(Vector2f vec) {
		return sub(vec.x, vec.y);
	}

	public Vector2f mult(float f) {
		return mult(f, f);
	}

	public Vector2f mult(float x, float y) {
		this.x *= x;
		this.y *= y;
		return this;
	}

	public Vector2f mult(Vector2f vec) {
		return mult(vec.x, vec.y);
	}

	public Vector2f divide(float f) {
		return divide(f, f);
	}

	public Vector2f divide(float x, float y) {
		this.x /= x;
		this.y /= y;
		return this;
	}

	public Vector2f divide(Vector2f vec) {
		return divide(vec.x, vec.y);
	}

	public Vector2f mod(float f) {
		x %= f;
		y %= f;

		return this;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}
