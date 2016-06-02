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
package com.opengrave.og.engine.gait;

import com.opengrave.og.engine.Location;
import com.opengrave.og.models.DAESceneNode;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class Skeleton {
	public Bone root;
	public Matrix4f matrix;
	public Location location;

	public Skeleton(DAESceneNode node) {
		this.matrix = new Matrix4f();
		makeSkeleton(node);
	}

	private void makeSkeleton(DAESceneNode node) {
		root = makeBone(null, node);
	}

	private Bone makeBone(Bone parent, DAESceneNode node) {

		Bone b = new Bone();
		b.jointName = node.jointName;
		b.joint = node.joint;
		b.inverseBindMatrix = node.inverseBindMatrix; // Should remain the same
		b.parent = parent;
		b.total = node.total;
		b.local = node.local;
		b.nodeName = node.nodeName;
		b.index = node.index;
		b.animation = node.animation;
		if (parent != null) {
			parent.getChildren().add(b);
		}

		// Time to do some basic calc

		Vector4f v = b.local.mult4(new Vector4f(0f, 0f, 0f, 1f), null);
		b.jointPoint = v;
		Vector3f v3 = new Vector3f(v.x, v.y, v.z);
		b.length = v3.length();

		// And children
		for (DAESceneNode nodeC : node.children) {
			makeBone(b, nodeC);
			// b2.jointPoint = Matrix4f.transform(b.local, new Vector4f(0f,0f,0f,1f), null);

		}
		return b;
	}

	public Bone getBone(String string) {
		if (root == null) {
			return null;
		}
		return root.getBone(string);
	}

}
