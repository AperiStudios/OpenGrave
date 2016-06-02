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

import java.nio.FloatBuffer;

public class Matrix3f {
	private float[] matrix = new float[9];

	public Matrix3f() {
		clearToIdentity();
	}

	public Matrix3f(float[] m) {
		this();
		set(m);
	}

	public Matrix3f(Matrix3f m) {
		this();
		set(m);
	}

	public Matrix3f(Matrix4f mat4) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				put(i, j, mat4.get(i, j));
			}
		}
	}

	public Matrix3f clear() {
		for (int a = 0; a < 9; a++)
			matrix[a] = 0;

		return this;
	}

	public Matrix3f clearToIdentity() {
		return clear().put(0, 1).put(4, 1).put(8, 1);
	}

	public float get(int index) {
		return matrix[index];
	}

	public float get(int col, int row) {
		return matrix[col * 3 + row];
	}

	public Matrix3f put(int index, float f) {
		matrix[index] = f;
		return this;
	}

	public Matrix3f put(int col, int row, float f) {
		matrix[col * 3 + row] = f;
		return this;
	}

	public Matrix3f putColumn(int index, Vector3f v) {
		put(index, 0, v.x());
		put(index, 1, v.y());
		put(index, 2, v.z());
		return this;
	}

	public Matrix3f set(float[] m) {
		if (m.length < 9) {
			throw new IllegalArgumentException("float array must have at least 9 values.");
		}

		for (int a = 0; a < m.length && a < 9; a++) {
			matrix[a] = m[a];
		}

		return this;
	}

	public Matrix3f set(Matrix3f m) {
		for (int a = 0; a < 9; a++) {
			matrix[a] = m.matrix[a];
		}
		return this;
	}

	public Matrix3f set4x4(Matrix4f m) {
		for (int a = 0; a < 3; a++) {
			put(a, 0, m.get(a, 0));
			put(a, 1, m.get(a, 1));
			put(a, 2, m.get(a, 2));
		}

		return this;
	}

	public Matrix3f mult(float f) {
		for (int a = 0; a < 9; a++)
			put(a, get(a) * f);

		return this;
	}

	public Matrix3f mult(float[] m) {
		if (m.length < 9) {
			throw new IllegalArgumentException("float array must have at least 9 values.");
		}

		return mult(new Matrix3f(m));
	}

	public Matrix3f mult(Matrix3f m) {
		Matrix3f temp = new Matrix3f();

		for (int a = 0; a < 3; a++) {
			temp.put(a, 0, get(0) * m.get(a, 0) + get(3) * m.get(a, 1) + get(6) * m.get(a, 2));
			temp.put(a, 1, get(1) * m.get(a, 0) + get(4) * m.get(a, 1) + get(7) * m.get(a, 2));
			temp.put(a, 2, get(2) * m.get(a, 0) + get(5) * m.get(a, 1) + get(8) * m.get(a, 2));
		}

		set(temp);

		return this;
	}

	public Vector3f mult3(Vector3f vec, Vector3f result) {
		if (result == null) {
			result = new Vector3f();
		}
		return result.set(get(0) * vec.x() + get(3) * vec.y() + get(6) * vec.z(), get(1) * vec.x() + get(4) * vec.y() + get(7) * vec.z(), get(2) * vec.x()
				+ get(5) * vec.y() + get(8) * vec.z());
	}

	public Matrix3f transpose() {
		float old = get(1);
		put(1, get(3));
		put(3, old);

		old = get(2);
		put(2, get(6));
		put(6, old);

		old = get(5);
		put(5, get(7));
		put(7, old);

		return this;
	}

	public float determinant() {
		return +get(0) * get(4) * get(8) + get(3) * get(7) * get(2) + get(6) * get(1) * get(5) - get(2) * get(4) * get(6) - get(5) * get(7) * get(0) - get(8)
				* get(1) * get(3);
	}

	public Matrix3f inverse() {
		Matrix3f inv = new Matrix3f();

		inv.put(0, +(get(4) * get(8) - get(5) * get(7)));
		inv.put(1, -(get(3) * get(8) - get(5) * get(6)));
		inv.put(2, +(get(3) * get(7) - get(4) * get(6)));

		inv.put(3, -(get(1) * get(8) - get(2) * get(7)));
		inv.put(4, +(get(0) * get(8) - get(2) * get(6)));
		inv.put(5, -(get(0) * get(7) - get(1) * get(6)));

		inv.put(6, +(get(1) * get(5) - get(2) * get(4)));
		inv.put(7, -(get(0) * get(5) - get(2) * get(3)));
		inv.put(8, +(get(0) * get(4) - get(1) * get(3)));

		return set(inv.transpose().mult(1 / determinant()));
	}

	public void store(FloatBuffer buf) {
		for (int a = 0; a < 9; a++) {
			buf.put(matrix[a]);
		}
	}
}
