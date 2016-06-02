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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.opengrave.common.pathing.PathNode;
import com.opengrave.common.pathing.PathProgress;
import com.opengrave.og.Util;
import com.opengrave.og.engine.Location;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.terrain.TerrainWorld;
import com.opengrave.og.util.Matrix4f;

public class RenderableLines extends Renderable {

	public boolean visible = true;
	ArrayList<VertexPoint> vertexList = new ArrayList<VertexPoint>();
	private int vaoID;
	private int vboID, vbocID;
	private FloatBuffer posBuffer, colBuffer;
	public Matrix4f matrix;
	private int vertCount;

	public void setFromPath(PathProgress path, TerrainWorld world) {
		vertexList.clear();
		PathNode lastNode = null;
		for (PathNode node : path.getAll()) {
			if (lastNode != null) {
				Location lastNodeLoc = new Location();
				lastNodeLoc.set(lastNode);
				Location nodeLoc = new Location();
				nodeLoc.set(node);
				float z1 = world.getHeightAt(lastNodeLoc);
				float z2 = world.getHeightAt(nodeLoc);
				addVertex(new VertexPoint(lastNode.getX() / 2f, lastNode.getY() / 2f, z1, 0f, 0f, 1f, 1f, 0f, 0f));
				addVertex(new VertexPoint(node.getX() / 2f, node.getY() / 2f, z2, 0f, 0f, 1f, 1f, 0f, 0f));
			}
			lastNode = node;
		}
		setChanged();
	}

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
		// non-world items, no shadow
	}

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		dealWithChange();
		if (!visible) {
			return;
		}
		if (vertCount == 0) {
			return;
		}
		GL11.glPointSize(3f);

		int pID = Resources.loadShader("lines.vs", "lines.fs").getProgram();
		GL20.glUseProgram(pID);
		if (pID == 0) {
			return;
		}
		GL30.glBindVertexArray(vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);

		Util.checkErr();

		getContext().setMatrices(pID, matrix);
		Util.checkErr();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDrawArrays(GL11.GL_LINES, 0, vertCount);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		Util.checkErr();
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		Util.checkErr();

		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		Util.checkErr();

	}

	@Override
	public void renderForPicking(Matrix4f matrix, Pickable object) {
	}

	@Override
	public void dealWithChange() {
		if (vaoID == 0) {
			vaoID = GL30.glGenVertexArrays();
		}
		GL30.glBindVertexArray(vaoID);
		if (vboID == 0) {
			vboID = GL15.glGenBuffers();
		}
		if (vbocID == 0) {
			vbocID = GL15.glGenBuffers();
		}

		if (changed) {
			changed = false;
			recreate();
			vertCount = vertexList.size();
			if ((vertCount & 1) == 1) { // Dirty fix for odd numbers
				vertCount = 0;
				return;
			}
			if (vertCount == 0) {
				return;
			}
			if (posBuffer == null || posBuffer.capacity() != vertCount * 3) {
				posBuffer = BufferUtils.createFloatBuffer(vertCount * 3);
			}
			if (colBuffer == null || colBuffer.capacity() != vertCount * 4) {
				colBuffer = BufferUtils.createFloatBuffer(vertCount * 4);
			}
			for (VertexPoint va : vertexList) {
				posBuffer.put(va.x).put(va.y).put(va.z);
				colBuffer.put(va.r).put(va.g).put(va.b).put(va.a);
			}
			posBuffer.flip();
			colBuffer.flip();

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		}
		GL30.glBindVertexArray(0);
	}

	@Override
	public void addVertex(VertexData vd) {
		if (vd instanceof VertexPoint) {
			vertexList.add((VertexPoint) vd);
			changed = true;
		}
	}

	@Override
	public void recreate() {
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void delete() {

	}

	public void clearLines() {
		vertexList.clear();
	}

}
