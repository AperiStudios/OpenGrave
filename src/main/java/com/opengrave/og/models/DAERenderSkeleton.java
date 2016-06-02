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

import com.opengrave.og.base.RenderableLines;
import com.opengrave.og.base.VertexAnimated;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

public class DAERenderSkeleton extends RenderableLines {

	private DAESceneNode skeleton;

	@Override
	public void recreate() {
		if (this.skeleton == null) {
			return;
		}
		addBoneLines(skeleton, new Matrix4f());

	}

	@Override
	public void update(float delta) {
		if (this.skeleton == null) {
			return;
		}
		setChanged();
	}

	public void init(DAESceneNode skeleton) {
		this.skeleton = skeleton;
	}

	private void addBoneLines(DAESceneNode bone, Matrix4f parent) {
		// addLineBox();
		VertexAnimated point1 = new VertexAnimated();
		Matrix4f local = bone.animatedWorldMatrix;
		Vector4f vec = local.mult4(new Vector4f(0f, 0f, 0f, 1f), null);
		point1.setPos(vec);
		for (DAESceneNode child : bone.children) {
			VertexAnimated point2 = new VertexAnimated();
			Matrix4f childLocal = child.animatedWorldMatrix;
			vec = childLocal.mult4(new Vector4f(0f, 0f, 0f, 1f), null);
			point2.setPos(vec);
			addVertex(point1);
			addVertex(point2);
			addBoneLines(child, local);
		}
	}

	@Override
	public void delete() {
		// TODO Delete links to this object
	}

}
