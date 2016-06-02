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
import org.lwjgl.opengl.*;

import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.resources.TextureAtlas;
import com.opengrave.og.util.Matrix4f;

public class RenderablePoints extends Renderable {

	public boolean visible = true;
	public ArrayList<VertexPoint> vertexList = new ArrayList<VertexPoint>();
	private int vaoID;
	private int vboID, vboCID, vbosID;
	private FloatBuffer posBuffer;
	private FloatBuffer colBuffer;
	private FloatBuffer scaleBuffer;

	private int vertCount;
	private TextureAtlas tex;

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
	}

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		Util.checkErr();

		dealWithChange();
		if (!visible) {
			return;
		}
		if (vertCount == 0) {
			return;
		}
		Util.checkErr();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		Util.checkErr();
		GL11.glPointSize(3f);
		Util.checkErr();
		int pID = Resources.loadShader("particle.vs", "particle.fs").getProgram();
		Util.checkErr();
		GL20.glUseProgram(pID);
		Util.checkErr();
		if (pID == 0) {
			return;
		}
		Util.checkErr();
		GL30.glBindVertexArray(vaoID);
		Util.checkErr();
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		Util.checkErr();
		int wSS = GL20.glGetUniformLocation(pID, "windowSizeScale");

		GL20.glUniform1f(wSS, MainThread.lastW / 1024f);

		getContext().setMatrices(pID, matrix);
		Util.checkErr();

		if (tex != null) {
			tex.bind(GL13.GL_TEXTURE0);
		}
		GL11.glEnable(GL20.GL_POINT_SPRITE);
		GL11.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, vertCount);

		GL11.glDisable(GL20.GL_POINT_SPRITE);
		GL11.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
		if (tex != null) {
			tex.unbind();
		}
		Util.checkErr();

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		Util.checkErr();

		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		Util.checkErr();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
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
		if (vboCID == 0) {
			vboCID = GL15.glGenBuffers();
		}
		if (vbosID == 0) {
			vbosID = GL15.glGenBuffers();
		}

		if (changed) {
			changed = false;
			// vertexList.clear();
			recreate();
			synchronized (vertexList) {
				vertCount = vertexList.size();
				if (vertCount == 0) {
					return;
				}
				if (posBuffer == null || posBuffer.capacity() != vertCount * 3) {
					posBuffer = BufferUtils.createFloatBuffer(vertCount * 3);
				}
				if (colBuffer == null || colBuffer.capacity() != vertCount * 4) {
					colBuffer = BufferUtils.createFloatBuffer(vertCount * 4);
				}
				if (scaleBuffer == null || scaleBuffer.capacity() != vertCount * 4) {
					scaleBuffer = BufferUtils.createFloatBuffer(vertCount * 4);
				}
				posBuffer.position(0);
				colBuffer.position(0);
				scaleBuffer.position(0);
				for (VertexPoint va : vertexList) {
					posBuffer.put(va.x).put(va.y).put(va.z);
					colBuffer.put(va.r).put(va.g).put(va.b).put(va.a);
					scaleBuffer.put(va.size).put(va.texture).put(0f).put(0f);
				}
			}
			posBuffer.flip();
			colBuffer.flip();
			scaleBuffer.flip();

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbosID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, scaleBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		}
		GL30.glBindVertexArray(0);
	}

	@Override
	public void addVertex(VertexData vd) {
		if (vd instanceof VertexPoint) {
			synchronized (vertexList) {
				vertexList.add((VertexPoint) vd);
			}
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

	public void clearPoints() {
		synchronized (vertexList) {
			vertexList.clear();
		}
	}

	public void setTexture(TextureAtlas tex) {
		this.tex = tex;
	}
}
