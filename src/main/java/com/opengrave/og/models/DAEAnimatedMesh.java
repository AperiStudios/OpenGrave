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

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.opengrave.og.Util;
import com.opengrave.og.base.RenderableBoneAnimatedStatic;
import com.opengrave.og.util.Matrix4f;

public class DAEAnimatedMesh {

	public DAERenderSkeleton skeletonModel = new DAERenderSkeleton();
	private DAEMesh model;
	public DAESceneNode skeleton;

	float poseTime = 0f;
	private String name;
	public boolean visible;
	public Matrix4f matrix;

	public DAEAnimatedMesh(DAEFile file, DAEMesh sMesh, DAESceneNode skeleton, ArrayList<DAEAnimClip> animation, String animName) {
		this.model = sMesh;
		this.skeleton = skeleton;
		this.name = animName;
		skeletonModel.init(this.skeleton);
		skeleton.applyAnimations(animation);
	}

	public String getName() {
		return name;
	}

	public void setBonesUniform(int pID) {
		FloatBuffer matrix44 = BufferUtils.createFloatBuffer(128 * 16);
		GL20.glUseProgram(pID);
		int boneNo = 0;
		DAESceneNode bone = null;
		while ((bone = skeleton.getBone(boneNo)) != null) {
			bone.skinningMatrix.store(matrix44);
			boneNo++;
		}
		Matrix4f ident = new Matrix4f();
		while (boneNo < 128) {

			ident.store(matrix44);
			boneNo++;
		}

		matrix44.flip();
		Util.setUniformMat44(pID, "u_BoneTransform", matrix44);
	}

	public void addGeom(RenderableBoneAnimatedStatic stat, float material) {
		if (stat == null) {
			throw (new RuntimeException("Need a RenderableBoneAnimtedStatic to fill"));
		}
		for (DAETriangleList tris : model.trianglesLists) {
			tris.bake(stat, model, material, matrix);
		}
	}

}
