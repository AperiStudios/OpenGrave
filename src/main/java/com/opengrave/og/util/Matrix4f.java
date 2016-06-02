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

import com.opengrave.og.Util;

public class Matrix4f {

	private float[] matrix = new float[16];

	public Matrix4f() {
		clearToIdentity();
	}

	public Matrix4f(float[] m) {
		this();
		set(m);
	}

	public Matrix4f(Matrix4f m) {
		this();
		set(m);
	}

	public Matrix4f clear() {
		for (int a = 0; a < 16; a++)
			matrix[a] = 0;

		return this;
	}

	public Matrix4f clearToIdentity() {
		return clear().put(0, 1).put(5, 1).put(10, 1).put(15, 1);
	}

	// public Matrix4f ortho(float left, float right, float bottom, float top, float near, float far) {
	// return clear().put(0, 2 / (right - left)).put(5, 2 / (top - bottom)).put(10, -2 / (far - near)).put(12, -(right + left) / (right - left))
	// .put(13, -(top + bottom) / (top - bottom)).put(14, -(far + near) / (far - near)).put(15, 1);
	// }

	public static Matrix4f lookAt(Vector3f eye, Vector3f location, Vector3f up) {
		Vector3f f = new Vector3f(location.x - eye.x, location.y - eye.y, location.z - eye.z);
		if (f.length() == 0f) {
			return new Matrix4f();
		} // Zero length means we can't look from and to the same place. Return
			// ident to avoid error
		f.normalise(f);
		Vector3f u = new Vector3f(up.x, up.y, up.z);
		u.normalise(u);
		Vector3f s = new Vector3f(f).cross(u, null);

		u = new Vector3f(s).cross(f, u);
		Matrix4f matrix = new Matrix4f();
		matrix.set(0, 0, s.x);
		matrix.set(0, 1, u.x);
		matrix.set(0, 2, -f.x);

		matrix.set(1, 0, s.y);
		matrix.set(1, 1, u.y);
		matrix.set(1, 2, -f.y);

		matrix.set(2, 0, s.z);
		matrix.set(2, 1, u.z);
		matrix.set(2, 2, -f.z);

		matrix.set(3, 3, 1f);
		matrix.translate(eye.negate(), matrix);
		return matrix;
	}

	public static Matrix4f ortho(float left, float right, float top, float bottom, float near, float far) {
		Matrix4f matrix = new Matrix4f();
		matrix.set(0, 0, 2f / (right - left));
		matrix.set(0, 1, 0f);
		matrix.set(0, 2, 0f);
		matrix.set(0, 3, 0f);

		matrix.set(1, 0, 0f);
		matrix.set(1, 1, 2f / (top - bottom));
		matrix.set(1, 2, 0f);
		matrix.set(1, 3, 0f);

		matrix.set(2, 0, 0f);
		matrix.set(2, 1, 0f);
		matrix.set(2, 2, -2f / (far - near));
		matrix.set(2, 3, 0f);

		matrix.set(3, 0, 0f - ((right + left) / (right - left)));
		matrix.set(3, 1, 0f - ((top + bottom) / (top - bottom)));
		matrix.set(3, 2, 0f - ((far + near) / (far - near)));
		matrix.set(3, 3, 1f);
		return matrix;
	}

	public Matrix4f perspective(float fovRad, float width, float height, float near, float far) {
		float fov = 1 / (float) Math.tan(fovRad / 2);
		return clear().put(0, fov * (height / width)).put(5, fov).put(10, (far + near) / (near - far)).put(14, (2 * far * near) / (near - far)).put(11, -1);
	}

	public Matrix4f perspectiveDeg(float fov, float width, float height, float near, float far) {
		return perspective((float) Math.toRadians(fov), width, height, near, far);
	}

	public float get(int index) {
		return matrix[index];
	}

	public float get(int col, int row) {
		return matrix[col * 4 + row];
	}

	public void set(int i, int j, float f) {
		matrix[(i * 4) + j] = f;
	}

	public Vector4f getColumn(int index, Vector4f result) {
		return result.set(get(index, 0), get(index, 1), get(index, 2), get(index, 3));
	}

	public Matrix4f put(int index, float f) {
		matrix[index] = f;
		return this;
	}

	public Matrix4f put(int col, int row, float f) {
		matrix[col * 4 + row] = f;
		return this;
	}

	public Matrix4f putColumn(int index, Vector4f v) {
		put(index, 0, v.x());
		put(index, 1, v.y());
		put(index, 2, v.z());
		put(index, 3, v.z());
		return this;
	}

	public Matrix4f putColumn3(int index, Vector3f v) {
		put(index, 0, v.x());
		put(index, 1, v.y());
		put(index, 2, v.z());
		return this;
	}

	public Matrix4f putColumn(int index, Vector3f v, float w) {
		put(index, 0, v.x());
		put(index, 1, v.y());
		put(index, 2, v.z());
		put(index, 3, w);
		return this;
	}

	public Matrix4f set(float[] m) {
		if (m.length < 16) {
			throw new IllegalArgumentException("float array must have at least 16 values.");
		}

		for (int a = 0; a < m.length && a < 16; a++) {
			matrix[a] = m[a];
		}

		return this;
	}

	public Matrix4f set(Matrix4f m) {
		for (int a = 0; a < 16; a++) {
			matrix[a] = m.matrix[a];
		}
		return this;
	}

	public Matrix4f set3x3(Matrix3f m) {
		for (int a = 0; a < 3; a++) {
			put(a, 0, m.get(a, 0));
			put(a, 1, m.get(a, 1));
			put(a, 2, m.get(a, 2));
		}

		return this;
	}

	public Matrix4f mult(float f, Matrix4f res) {
		if (res == null) {
			res = new Matrix4f();
		}
		for (int a = 0; a < 16; a++)
			res.put(a, get(a) * f);

		return res;
	}

	public Matrix4f mult(float[] m, Matrix4f res) {
		if (m.length < 16) {
			throw new IllegalArgumentException("float array must have at least 16 values.");
		}

		return mult(new Matrix4f(m), res);
	}

	public Matrix4f mult(Matrix4f m, Matrix4f res) {
		if (res == null) {
			res = new Matrix4f();
		}

		for (int a = 0; a < 4; a++) {
			res.put(a, 0, get(0) * m.get(a, 0) + get(4) * m.get(a, 1) + get(8) * m.get(a, 2) + get(12) * m.get(a, 3));
			res.put(a, 1, get(1) * m.get(a, 0) + get(5) * m.get(a, 1) + get(9) * m.get(a, 2) + get(13) * m.get(a, 3));
			res.put(a, 2, get(2) * m.get(a, 0) + get(6) * m.get(a, 1) + get(10) * m.get(a, 2) + get(14) * m.get(a, 3));
			res.put(a, 3, get(3) * m.get(a, 0) + get(7) * m.get(a, 1) + get(11) * m.get(a, 2) + get(15) * m.get(a, 3));
		}

		return res;
	}

	public Vector3f mult3(Vector3f vec, float w, Vector3f result) {
		return result.set4(mult4(new Vector4f(vec, w), new Vector4f()));
	}

	public Vector4f mult4(Vector4f vec, Vector4f result) {
		if (result == null) {
			result = new Vector4f();
		}
		return result.set(matrix[0] * vec.x() + matrix[4] * vec.y() + matrix[8] * vec.z() + matrix[12] * vec.w(), matrix[1] * vec.x() + matrix[5] * vec.y()
				+ matrix[9] * vec.z() + matrix[13] * vec.w(), matrix[2] * vec.x() + matrix[6] * vec.y() + matrix[10] * vec.z() + matrix[14] * vec.w(),
				matrix[3] * vec.x() + matrix[7] * vec.y() + matrix[11] * vec.z() + matrix[15] * vec.w());
	}

	public Matrix4f transpose(Matrix4f res) {
		if (res == null) {
			res = new Matrix4f(this);
		}
		float old = get(1);
		res.put(1, get(4));
		res.put(4, old);

		old = get(2);
		res.put(2, get(8));
		res.put(8, old);

		old = get(3);
		res.put(3, get(12));
		res.put(12, old);

		old = get(7);
		res.put(7, get(13));
		res.put(13, old);

		old = get(11);
		res.put(11, get(14));
		res.put(14, old);

		old = get(6);
		res.put(6, get(9));
		res.put(9, old);

		return res;
	}

	public Matrix4f translate(float x, float y, float z, Matrix4f res) {
		if (res == null) {
			res = new Matrix4f(this);
		}
		Matrix4f temp = new Matrix4f();

		temp.put(0, 1);
		temp.put(5, 1);
		temp.put(10, 1);
		temp.put(15, 1);

		temp.put(12, x);
		temp.put(13, y);
		temp.put(14, z);

		return mult(temp, res);
	}

	public Matrix4f translate(Vector3f vec, Matrix4f res) {
		return translate(vec.x(), vec.y(), vec.z(), res);
	}

	public Matrix4f scale(float f, Matrix4f res) {
		return scale(f, f, f, res);
	}

	public Matrix4f scale(float x, float y, float z, Matrix4f res) {
		Matrix4f temp = new Matrix4f();

		temp.put(0, x);
		temp.put(5, y);
		temp.put(10, z);
		temp.put(15, 1);

		return mult(temp, res);
	}

	public Matrix4f scale(Vector3f vec, Matrix4f res) {
		return scale(vec.x(), vec.y(), vec.z(), res);
	}

	public Matrix4f rotate(float angle, float x, float y, float z, Matrix4f res) {
		if (res == null)
			res = new Matrix4f();
		float c = (float) Math.cos(angle);
		float s = (float) Math.sin(angle);
		float oneminusc = 1.0f - c;
		float xy = x * y;
		float yz = y * z;
		float xz = x * z;
		float xs = x * s;
		float ys = y * s;
		float zs = z * s;

		float f00 = x * x * oneminusc + c;
		float f01 = xy * oneminusc + zs;
		float f02 = xz * oneminusc - ys;
		// n[3] not used
		float f10 = xy * oneminusc - zs;
		float f11 = y * y * oneminusc + c;
		float f12 = yz * oneminusc + xs;
		// n[7] not used
		float f20 = xz * oneminusc + ys;
		float f21 = yz * oneminusc - xs;
		float f22 = z * z * oneminusc + c;

		float t00 = get(0, 0) * f00 + get(1, 0) * f01 + get(2, 0) * f02;
		float t01 = get(0, 1) * f00 + get(1, 1) * f01 + get(2, 1) * f02;
		float t02 = get(0, 2) * f00 + get(1, 2) * f01 + get(2, 2) * f02;
		float t03 = get(0, 3) * f00 + get(1, 3) * f01 + get(2, 3) * f02;
		float t10 = get(0, 0) * f10 + get(1, 0) * f11 + get(2, 0) * f12;
		float t11 = get(0, 1) * f10 + get(1, 1) * f11 + get(2, 1) * f12;
		float t12 = get(0, 2) * f10 + get(1, 2) * f11 + get(2, 2) * f12;
		float t13 = get(0, 3) * f10 + get(1, 3) * f11 + get(2, 3) * f12;
		res.put(2, 0, get(0, 0) * f20 + get(1, 0) * f21 + get(2, 0) * f22);
		res.put(2, 1, get(0, 1) * f20 + get(1, 1) * f21 + get(2, 1) * f22);
		res.put(2, 2, get(0, 2) * f20 + get(1, 2) * f21 + get(2, 2) * f22);
		res.put(2, 3, get(0, 3) * f20 + get(1, 3) * f21 + get(2, 3) * f22);
		res.put(0, 0, t00);
		res.put(0, 1, t01);
		res.put(0, 2, t02);
		res.put(0, 3, t03);
		res.put(1, 0, t10);
		res.put(1, 1, t11);
		res.put(1, 2, t12);
		res.put(1, 3, t13);
		return res;
	}

	public Matrix4f rotate(float angle, Vector3f vec, Matrix4f res) {
		return rotate(angle, vec.x(), vec.y(), vec.z(), res);
	}

	public Matrix4f rotateDeg(float angle, float x, float y, float z, Matrix4f res) {
		return rotate((float) Math.toRadians(angle), x, y, z, res);
	}

	public Matrix4f rotateDeg(float angle, Vector3f vec, Matrix4f res) {
		return rotate((float) Math.toRadians(angle), vec, res);
	}

	public float determinant() {
		float a = get(5) * get(10) * get(15) + get(9) * get(14) * get(7) + get(13) * get(6) * get(11) - get(7) * get(10) * get(13) - get(11) * get(14) * get(5)
				- get(15) * get(6) * get(9);
		float b = get(1) * get(10) * get(15) + get(9) * get(14) * get(3) + get(13) * get(2) * get(11) - get(3) * get(10) * get(13) - get(11) * get(14) * get(1)
				- get(15) * get(2) * get(9);
		float c = get(1) * get(6) * get(15) + get(5) * get(14) * get(3) + get(13) * get(2) * get(7) - get(3) * get(6) * get(13) - get(7) * get(14) * get(1)
				- get(15) * get(2) * get(5);
		float d = get(1) * get(6) * get(11) + get(5) * get(10) * get(3) + get(9) * get(2) * get(7) - get(3) * get(6) * get(9) - get(7) * get(10) * get(1)
				- get(11) * get(2) * get(5);

		return get(0) * a - get(4) * b + get(8) * c - get(12) * d;
	}

	public Matrix4f inverse(Matrix4f res) {
		if (res == null) {
			res = new Matrix4f();
		}

		res.put(0, +(get(5) * get(10) * get(15) + get(9) * get(14) * get(7) + get(13) * get(6) * get(11) - get(7) * get(10) * get(13) - get(11) * get(14)
				* get(5) - get(15) * get(6) * get(9)));
		res.put(1, -(get(4) * get(10) * get(15) + get(8) * get(14) * get(7) + get(12) * get(6) * get(11) - get(7) * get(10) * get(12) - get(11) * get(14)
				* get(4) - get(15) * get(6) * get(8)));
		res.put(2, +(get(4) * get(9) * get(15) + get(8) * get(13) * get(7) + get(12) * get(5) * get(11) - get(7) * get(9) * get(12) - get(11) * get(13)
				* get(4) - get(15) * get(5) * get(8)));
		res.put(3, -(get(4) * get(9) * get(14) + get(8) * get(13) * get(6) + get(12) * get(5) * get(10) - get(6) * get(9) * get(12) - get(10) * get(13)
				* get(4) - get(14) * get(5) * get(8)));

		res.put(4, -(get(1) * get(10) * get(15) + get(9) * get(14) * get(3) + get(13) * get(2) * get(11) - get(3) * get(10) * get(13) - get(11) * get(14)
				* get(1) - get(15) * get(2) * get(9)));
		res.put(5, +(get(0) * get(10) * get(15) + get(8) * get(14) * get(3) + get(12) * get(2) * get(11) - get(3) * get(10) * get(12) - get(11) * get(14)
				* get(0) - get(15) * get(2) * get(8)));
		res.put(6, -(get(0) * get(9) * get(15) + get(8) * get(13) * get(3) + get(12) * get(1) * get(11) - get(3) * get(9) * get(12) - get(11) * get(13)
				* get(0) - get(15) * get(1) * get(8)));
		res.put(7, +(get(0) * get(9) * get(14) + get(8) * get(13) * get(2) + get(12) * get(1) * get(10) - get(2) * get(9) * get(12) - get(10) * get(13)
				* get(0) - get(14) * get(1) * get(8)));

		res.put(8,
				+(get(1) * get(6) * get(15) + get(5) * get(14) * get(3) + get(13) * get(2) * get(7) - get(3) * get(6) * get(13) - get(7) * get(14) * get(1) - get(15)
						* get(2) * get(5)));
		res.put(9,
				-(get(0) * get(6) * get(15) + get(4) * get(14) * get(3) + get(12) * get(2) * get(7) - get(3) * get(6) * get(12) - get(7) * get(14) * get(0) - get(15)
						* get(2) * get(4)));
		res.put(10,
				+(get(0) * get(5) * get(15) + get(4) * get(13) * get(3) + get(12) * get(1) * get(7) - get(3) * get(5) * get(12) - get(7) * get(13) * get(0) - get(15)
						* get(1) * get(4)));
		res.put(11,
				-(get(0) * get(5) * get(14) + get(4) * get(13) * get(2) + get(12) * get(1) * get(6) - get(2) * get(5) * get(12) - get(6) * get(13) * get(0) - get(14)
						* get(1) * get(4)));

		res.put(12,
				-(get(1) * get(6) * get(11) + get(5) * get(10) * get(3) + get(9) * get(2) * get(7) - get(3) * get(6) * get(9) - get(7) * get(10) * get(1) - get(11)
						* get(2) * get(5)));
		res.put(13,
				+(get(0) * get(6) * get(11) + get(4) * get(10) * get(3) + get(8) * get(2) * get(7) - get(3) * get(6) * get(8) - get(7) * get(10) * get(0) - get(11)
						* get(2) * get(4)));
		res.put(14,
				-(get(0) * get(5) * get(11) + get(4) * get(9) * get(3) + get(8) * get(1) * get(7) - get(3) * get(5) * get(8) - get(7) * get(9) * get(0) - get(11)
						* get(1) * get(4)));
		res.put(15,
				+(get(0) * get(5) * get(10) + get(4) * get(9) * get(2) + get(8) * get(1) * get(6) - get(2) * get(5) * get(8) - get(6) * get(9) * get(0) - get(10)
						* get(1) * get(4)));

		return res.transpose(null).mult(1 / determinant(), null);
	}

	public Quaternion toQuaternion(Quaternion res) {
		float x = get(0) - get(5) - get(10);
		float y = get(5) - get(0) - get(10);
		float z = get(10) - get(0) - get(5);
		float w = get(0) + get(5) + get(10);

		int biggestIndex = 0;
		float biggest = w;

		if (x > biggest) {
			biggest = x;
			biggestIndex = 1;
		}

		if (y > biggest) {
			biggest = y;
			biggestIndex = 2;
		}

		if (z > biggest) {
			biggest = z;
			biggestIndex = 3;
		}

		float biggestVal = (float) (Math.sqrt(biggest + 1) * 0.5);
		float mult = 0.25f / biggestVal;

		switch (biggestIndex) {
		case 0:
			res.w(biggestVal);
			res.x((get(6) - get(9)) * mult);
			res.y((get(8) - get(2)) * mult);
			res.z((get(1) - get(4)) * mult);
			break;
		case 1:
			res.w((get(6) - get(9)) * mult);
			res.x(biggestVal);
			res.y((get(1) + get(4)) * mult);
			res.z((get(8) + get(2)) * mult);
			break;
		case 2:
			res.w((get(8) - get(2)) * mult);
			res.x((get(1) + get(4)) * mult);
			res.y(biggestVal);
			res.z((get(6) + get(9)) * mult);
			break;
		case 3:
			res.w((get(1) - get(4)) * mult);
			res.x((get(8) + get(2)) * mult);
			res.y((get(6) + get(9)) * mult);
			res.z(biggestVal);
			break;
		}

		return res;
	}

	public void store(FloatBuffer buf) {
		buf.position(0);
		for (int a = 0; a < 4; a++) {
			for (int b = 0; b < 4; b++) {
				buf.put(get(a, b));
			}
		}
	}

	public boolean equals(Matrix4f mat) {
		for (int i = 0; i < 16; i++) {
			if (get(i) != mat.get(i)) {
				return false;
			}
		}
		return true;
	}

	public Matrix4f interp(Matrix4f m2, float interp, Matrix4f res) {
		if (res == null) {
			res = new Matrix4f();
		}
		for (int i = 0; i < 4; i++) {
			res.put(i, get(i) * (1 - interp) + m2.get(i) * interp);
		}
		return res;
	}

	public static Matrix4f proj(float fov, int width, int height, float nZ, float fZ) {
		fov = Util.degreesToRadians(fov);
		Matrix4f projMatrix = new Matrix4f();
		float aspectRatio = (float) width / (float) height;
		float f = (float) (1f / Math.tan(fov * .5f));
		projMatrix.set(0, 0, f / aspectRatio);
		projMatrix.set(0, 1, 0f);
		projMatrix.set(0, 2, 0f);
		projMatrix.set(0, 3, 0f);

		projMatrix.set(1, 0, 0f);
		projMatrix.set(1, 1, f);
		projMatrix.set(1, 2, 0f);
		projMatrix.set(1, 3, 0f);

		projMatrix.set(2, 0, 0f);
		projMatrix.set(2, 1, 0f);
		projMatrix.set(2, 2, (fZ + nZ) / (nZ - fZ));
		projMatrix.set(2, 3, -1f);

		projMatrix.set(3, 0, 0f);
		projMatrix.set(3, 1, 0f);
		projMatrix.set(3, 2, (2 * nZ * fZ) / (nZ - fZ));
		projMatrix.set(3, 3, 0f);
		return projMatrix;
	}
}
