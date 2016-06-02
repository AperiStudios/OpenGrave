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

import com.opengrave.og.Util;
import com.opengrave.og.util.Matrix4f;

public class BoringWall extends Wall {

	float length = 10f;
	float height = 1.5f;
	float depth = 0.1f;

	@Override
	public float getLength() {
		return length;
	}

	@Override
	public void setLength(float length) {
		this.length = length;
	}

	@Override
	public String getMetaData() {
		return "";
	}

	@Override
	public void setMetaData(String s) {

	}

	/**
	 * Might be better suited in Renderable3DStatic
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param tx
	 * @param ty
	 */
	public void addVertex(float x, float y, float z, float tx, float ty, float nx, float ny, float nz) {
		Vertex3D vert = new Vertex3D();
		vert.x = x;
		vert.y = y;
		vert.z = z;
		vert.tx = tx;
		vert.ty = ty;
		vert.nx = nx;
		vert.ny = ny;
		vert.nz = nz;
		renderable.addVertex(vert);
	}

	public BoringWall() {
		renderable = new Renderable3DStatic();
		recreateWall();
	}

	public void recreateWall() {
		renderable.vertexList.clear();
		// Create wall, This is just 5 quads/10 tris here
		// Top Quad
		addVertex(-depth, -depth, height, 0f, .5f, 0f, 0f, 1f);
		addVertex(depth, length + depth, height, length + 1f, .25f, 0f, 0f, 1f);
		addVertex(-depth, length + depth, height, length + 1f, .5f, 0f, 0f, 1f);

		addVertex(-depth, -depth, height, 0f, .5f, 0f, 0f, 1f);
		addVertex(depth, -depth, height, 0f, .25f, 0f, 0f, 1f);
		addVertex(depth, length + depth, height, length + 1f, .25f, 0f, 0f, 1f);
		// Near
		addVertex(-depth, -depth, 0f, 0f, .5f, 0f, -1f, 0f);
		addVertex(depth, -depth, height, 1f, .25f, 0f, -1f, 0f);
		addVertex(-depth, -depth, height, 1f, .5f, 0f, -1f, 0f);

		addVertex(-depth, -depth, 0f, 0f, .5f, 0f, -1f, 0f);
		addVertex(depth, -depth, 0f, 0f, .25f, 0f, -1f, 0f);
		addVertex(depth, -depth, height, 1f, .25f, 0f, -1f, 0f);
		// Left
		addVertex(depth, -depth, 0f, 0f, 0f, 1f, 0f, 0f);
		addVertex(depth, length + depth, height, length + 1f, 1f, 1f, 0f, 0f);
		addVertex(depth, -depth, height, 0f, 1f, 1f, 0f, 0f);

		addVertex(depth, -depth, 0f, 0f, 0f, 1f, 0f, 0f);
		addVertex(depth, length + depth, 0f, length + 1f, 0f, 1f, 0f, 0f);
		addVertex(depth, length + depth, height, length + 1f, 1f, 1f, 0f, 0f);

		// Right
		addVertex(-depth, -depth, 0f, 0f, 0f, -1f, 0f, 0f);
		addVertex(-depth, -depth, height, 0f, 1f, -1f, 0f, 0f);
		addVertex(-depth, length + depth, height, length + 1f, 1f, -1f, 0f, 0f);

		addVertex(-depth, -depth, 0f, 0f, 0f, -1f, 0f, 0f);
		addVertex(-depth, length + depth, height, length + 1f, 1f, -1f, 0f, 0f);
		addVertex(-depth, length + depth, 0f, length + 1f, 0f, -1f, 0f, 0f);

		// Far
		addVertex(-depth, length + depth, 0f, 0f, .5f, 0f, 1f, 0f);
		addVertex(-depth, length + depth, height, 1f, .5f, 0f, 1f, 0f);
		addVertex(depth, length + depth, height, 1f, .25f, 0f, 1f, 0f);

		addVertex(-depth, length + depth, 0f, 0f, .5f, 0f, 1f, 0f);
		addVertex(depth, length + depth, height, 1f, .25f, 0f, 1f, 0f);
		addVertex(depth, length + depth, 0f, 0f, .25f, 0f, 1f, 0f);
	}

	@Override
	public String getType() {
		return "boring";
	}

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(location, null, null, null);
	}

}
