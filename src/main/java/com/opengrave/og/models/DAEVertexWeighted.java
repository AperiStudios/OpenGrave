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
package com.opengrave.og.models;

import java.util.ArrayList;

import com.opengrave.og.base.VertexAnimated;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

public class DAEVertexWeighted {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jointIndex == null) ? 0 : jointIndex.hashCode());
		result = prime * result + ((weight == null) ? 0 : weight.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DAEVertexWeighted))
			return false;
		DAEVertexWeighted other = (DAEVertexWeighted) obj;
		if (jointIndex == null) {
			if (other.jointIndex != null)
				return false;
		} else if (!jointIndex.equals(other.jointIndex))
			return false;
		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;
		return true;
	}

	public ArrayList<Integer> jointIndex = new ArrayList<Integer>();
	public ArrayList<Float> weight = new ArrayList<Float>();

	public DAEVertexWeighted(int count) {
	}

	public void touch(int index) {
		while (jointIndex.size() <= index) {
			jointIndex.add(0);
		}
		while (weight.size() <= index) {
			weight.add(0f);
		}
	}

	public int size() {
		int jS = jointIndex.size(), wS = weight.size();
		return jS < wS ? jS : wS; // return which is smaller
	}

	public VertexAnimated getWeighted(VertexAnimated va, DAESceneNode rootSkel) {
		VertexAnimated newVa = new VertexAnimated();
		/*
		 * newVa.tx = va.tx; newVa.ty = va.ty; newVa.r = va.r; newVa.g = va.g;
		 * newVa.b = va.b; Vector4f original; = va.toV4f(); Vector3f normal; =
		 * va.toV3fNormal(); Vector4f finalVec = new Vector4f(); Vector3f
		 * finalNormal = new Vector3f(); Vector3f normalTransform = new
		 * Vector3f();
		 * 
		 * int totWeight = 0; for(int i = 0 ; i < size(); i++){ DAESceneNode
		 * bone = rootSkel.getBone(jointIndex.get(i)); // Add more to the vertex
		 * Vector4f vec = new Vector4f(); vec =
		 * Matrix4f.transform(bone.skinningMatrix, original, null); int w =
		 * weightIndex.get(i); vec = multVec(vec, w); Vector4f.add(finalVec,
		 * vec, finalVec); // Add more to the normal Vector3f.add(finalNormal,
		 * rotate(bone.skinningMatrix, normal, w), finalNormal);
		 * 
		 * totWeight += w; } if(totWeight > 1){ float normalize = 1f /
		 * totWeight; finalVec = multVec(finalVec, normalize); finalNormal =
		 * multVec(finalNormal, normalize); } newVa.x = finalVec.x; newVa.y =
		 * finalVec.y; newVa.z = finalVec.z; newVa.nx = finalNormal.x; newVa.ny
		 * = finalNormal.y; newVa.nz = finalNormal.z;
		 */
		return newVa;

	}

	public Vector4f rotate(Matrix4f skinningMatrix, Vector4f normal, float w) {
		Vector4f vec = new Vector4f();
		vec.x = (normal.x * skinningMatrix.get(0, 0) + normal.y * skinningMatrix.get(0, 1) + normal.z * skinningMatrix.get(0, 2)) * w;
		vec.y = (normal.x * skinningMatrix.get(1, 0) + normal.y * skinningMatrix.get(1, 1) + normal.z * skinningMatrix.get(1, 2)) * w;
		vec.z = (normal.x * skinningMatrix.get(2, 0) + normal.y * skinningMatrix.get(2, 1) + normal.z * skinningMatrix.get(2, 2)) * w;
		return vec;
	}

	public Vector4f multVec(Vector4f vec, float w) {
		vec.x = vec.x * w;
		vec.y = vec.y * w;
		vec.z = vec.z * w;
		vec.w = 1f;
		return vec;
	}

	public int getBoneId(int index) {
		if (index >= jointIndex.size()) {
			return -1;
		}
		return jointIndex.get(index);
	}

	public float getWeight(int index) {
		if (index >= weight.size()) {
			return 0f;
		}
		return weight.get(index);
	}
}
