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

public class Quaternion {
	public float x;
	public float y;
	public float z;
	public float w;

	public Quaternion() {
		reset();
	}

	public Quaternion(float x, float y, float z, float w) {
		set(x, y, z, w);
	}

	public Quaternion(float angle, Vector3f vec) {
		float s = (float) Math.sin(angle / 2);

		x = vec.x() * s;
		y = vec.y() * s;
		z = vec.z() * s;
		w = (float) Math.cos(angle / 2);
	}

	public Quaternion(Quaternion q) {
		set(q);
	}

	public float x() {
		return x;
	}

	public Quaternion x(float x) {
		this.x = x;
		return this;
	}

	public float y() {
		return y;
	}

	public Quaternion y(float y) {
		this.y = y;
		return this;
	}

	public float z() {
		return z;
	}

	public Quaternion z(float z) {
		this.z = z;
		return this;
	}

	public float w() {
		return w;
	}

	public Quaternion w(float w) {
		this.w = w;
		return this;
	}

	public Quaternion set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}

	public Quaternion set(Quaternion q) {
		return set(q.x, q.y, q.z, q.w);
	}

	public Quaternion reset() {
		x = 0;
		y = 0;
		z = 0;
		w = 1;
		return this;
	}

	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z + w * w);
	}

	public Quaternion normalise(Quaternion res) {
		if (res == null) {
			res = new Quaternion();
		}
		float length = 1f / length();
		res.x = x * length;
		res.y = y * length;
		res.z = z * length;
		res.w = w * length;
		return res;
	}

	public float dot(Quaternion q) {
		return x * q.x + y * q.y + z * q.z + w * q.w;
	}

	public Vector3f mult3(Vector3f v, Vector3f result) {
		Vector3f quatVector = new Vector3f(x, y, z);

		Vector3f uv = quatVector.cross(v, new Vector3f());
		Vector3f uuv = quatVector.cross(uv, new Vector3f());

		uv.mult(w * 2, uv);
		uuv.mult(2, uuv);

		return result.set(v).add(uv, null).add(uuv, null);
	}

	public Quaternion mult(Quaternion q, Quaternion res) {
		if (res == null) {
			res = new Quaternion();
		}
		float xx = w * q.x + x * q.w + y * q.z - z * q.y;
		float yy = w * q.y + y * q.w + z * q.x - x * q.z;
		float zz = w * q.z + z * q.w + x * q.y - y * q.x;
		float ww = w * q.w - x * q.x - y * q.y - z * q.z;

		res.x = xx;
		res.y = yy;
		res.z = zz;
		res.w = ww;

		return res;
	}

	public Quaternion conjugate(Quaternion res) {
		if (res == null) {
			res = new Quaternion();
		}
		res.x = x * -1;
		res.y = y * -1;
		res.z = z * -1;

		return res;
	}

	public Quaternion inverse() {
		return normalise(null).conjugate(null);
	}

	public Matrix4f toMatrix(Matrix4f mat4) {
		if (mat4 == null) {
			mat4 = new Matrix4f();
		}
		return mat4.set(new float[] { 1 - 2 * y * y - 2 * z * z, 2 * x * y + 2 * w * z, 2 * x * z - 2 * w * y, 0, 2 * x * y - 2 * w * z,
				1 - 2 * x * x - 2 * z * z, 2 * y * z + 2 * w * x, 0, 2 * x * z + 2 * w * y, 2 * y * z - 2 * w * x, 1 - 2 * x * x - 2 * y * y, 0, 0, 0, 0, 1, });
	}

	public void setFromMatrix(Matrix4f mat) {
		setFromMatrix(mat.get(0, 0), mat.get(0, 1), mat.get(0, 2), mat.get(1, 0), mat.get(1, 1), mat.get(1, 2), mat.get(2, 0), mat.get(2, 1), mat.get(2, 2));
	}

	private void setFromMatrix(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		float s;
		float tr = m00 + m11 + m22;
		if (tr >= 0.0) {
			s = (float) Math.sqrt(tr + 1.0);
			w = s * 0.5f;
			s = 0.5f / s;
			x = (m21 - m12) * s;
			y = (m02 - m20) * s;
			z = (m10 - m01) * s;
		} else {
			float max = Math.max(Math.max(m00, m11), m22);
			if (max == m00) {
				s = (float) Math.sqrt(m00 - (m11 + m22) + 1.0);
				x = s * 0.5f;
				s = 0.5f / s;
				y = (m01 + m10) * s;
				z = (m20 + m02) * s;
				w = (m21 - m12) * s;
			} else if (max == m11) {
				s = (float) Math.sqrt(m11 - (m22 + m00) + 1.0);
				y = s * 0.5f;
				s = 0.5f / s;
				z = (m12 + m21) * s;
				x = (m01 + m10) * s;
				w = (m02 - m20) * s;
			} else {
				s = (float) Math.sqrt(m22 - (m00 + m11) + 1.0);
				z = s * 0.5f;
				s = 0.5f / s;
				x = (m20 + m02) * s;
				y = (m12 + m21) * s;
				w = (m10 - m01) * s;
			}
		}
	}
}
