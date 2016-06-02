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
package com.opengrave.og.base;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.opengrave.og.Util;
import com.opengrave.og.engine.gait.Skeleton;
import com.opengrave.og.util.Matrix4f;

/**
 * Possibly confusingly named. The Vertex List is set once and should never
 * change (Although not enforced). Used to allow API access to animated model
 * creation
 * 
 * @author triggerhapp
 * 
 */
public class RenderableBoneAnimatedStatic extends RenderableBoneAnimated {

	private Skeleton skeleton;
	private ArrayList<Matrix4f> skinningMatrixList;

	public void setSkeleton(Skeleton skeleton) {
		this.skeleton = skeleton;
	}

	public Skeleton getSkeleton() {
		return skeleton;
	}

	@Override
	public void setBonesUniform(int pID) {
		if (skeleton == null) {
			return;
		}
		FloatBuffer matrix44 = BufferUtils.createFloatBuffer(128 * 16);
		GL20.glUseProgram(pID);
		int boneNo = 0;
		for (Matrix4f m : skinningMatrixList) {
			m.store(matrix44);
			boneNo++;
		}
		// DAESceneNode bone = null;
		// while ((bone = skeleton.getBone(boneNo)) != null) {
		// bone.skinningMatrix.store(matrix44);
		// System.out.println(boneNo);
		// boneNo++;
		// }
		Matrix4f ident = new Matrix4f();
		while (boneNo < 128) {

			ident.store(matrix44);
			boneNo++;
		}

		matrix44.flip();
		Util.setUniformMat44(pID, "u_BoneTransform", matrix44);
	}

	@Override
	public void recreate() {
		// Nope
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void delete() {

	}

	public void setSkinningMatrix(ArrayList<Matrix4f> skinningMatrixList) {
		this.skinningMatrixList = skinningMatrixList;
	}

}
