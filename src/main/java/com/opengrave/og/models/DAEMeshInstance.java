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

import com.opengrave.og.base.Renderable3DStatic;
import com.opengrave.og.util.Matrix4f;

public class DAEMeshInstance {

	private DAEMesh mesh;
	private Matrix4f matrix;
	private String name;

	public DAEMeshInstance(String name, DAEMesh mesh, Matrix4f matrix) {
		this.name = name;
		this.mesh = mesh;
		this.matrix = matrix;
	}

	public DAEMesh getMesh() {
		return mesh;
	}

	public Matrix4f getMatrix() {
		return matrix;
	}

	public String getName() {
		return name;
	}

	public void addGeom(Renderable3DStatic stat, float material) {
		if (stat == null) {
			return;
		}
		for (DAETriangleList tris : mesh.trianglesLists) {
			tris.bake(stat, material, matrix);
		}
	}
}
