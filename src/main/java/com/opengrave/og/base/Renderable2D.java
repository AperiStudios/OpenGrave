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

import com.opengrave.og.Util;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.resources.Texture;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public abstract class Renderable2D extends Renderable {

	protected Vector3f location2d = new Vector3f();
	ArrayList<Vertex2D> vertexList = new ArrayList<Vertex2D>();
	FloatBuffer posBuffer = BufferUtils.createFloatBuffer(0), colBuffer = BufferUtils.createFloatBuffer(0), texBuffer = BufferUtils.createFloatBuffer(0);
	int vaoID = 0, vbopID = 0, vbocID = 0, vbotID = 0;
	protected Texture texture;

	public static void init() {
	}

	@Override
	public void addVertex(VertexData vd) {
		if (vd instanceof Vertex2D) {
			vertexList.add((Vertex2D) vd);
		}
	}

	@Override
	public void dealWithChange() {
		Util.checkErr();
		if (vaoID == 0) {
			vaoID = GL30.glGenVertexArrays();
		}
		GL30.glBindVertexArray(vaoID);
		Util.checkErr();
		if (vbopID == 0) {
			vbopID = GL15.glGenBuffers();
		}
		Util.checkErr();
		if (vbocID == 0) {
			vbocID = GL15.glGenBuffers();
		}
		Util.checkErr();
		if (vbotID == 0) {
			vbotID = GL15.glGenBuffers();
		}
		Util.checkErr();
		if (changed) {
			changed = false;
			vertexList.clear();
			posBuffer.clear();
			colBuffer.clear();
			texBuffer.clear();
			recreate();
			Util.checkErr();
			int verts = vertexList.size();
			if (vertexList.size() == 0) {
				return;
			}
			if (posBuffer.capacity() != verts * 2) {
				posBuffer = BufferUtils.createFloatBuffer(verts * 2);
			}
			Util.checkErr();
			if (colBuffer.capacity() != verts * 4) {
				colBuffer = BufferUtils.createFloatBuffer(verts * 4);
			}
			if (texBuffer.capacity() != verts * 3) {
				texBuffer = BufferUtils.createFloatBuffer(verts * 3);
			}
			for (Vertex2D v2 : vertexList) {
				posBuffer.put(v2.x).put(v2.y);
				texBuffer.put(v2.tx).put(v2.ty).put(v2.tz);
				colBuffer.put(v2.r).put(v2.g).put(v2.b).put(v2.a);
			}
			posBuffer.flip();
			texBuffer.flip();
			colBuffer.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbopID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STREAM_DRAW);
			GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
			Util.checkErr();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colBuffer, GL15.GL_STREAM_DRAW);
			GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
			Util.checkErr();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texBuffer, GL15.GL_STREAM_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
			Util.checkErr();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		}
	}

	@Override
	public void renderForPicking(Matrix4f matrix, Pickable object) {
		dealWithChange();
		if (vertexList.size() == 0) {
			return;
		}
		int pID = Resources.loadShader("pickinggui.vs", "pickinggui.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vaoID);
		GL20.glEnableVertexAttribArray(0);
		Util.setMatrices(pID, location2d);
		Picking.registerObject(pID, getContext(), object);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexList.size());
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
		// GUI never casts shadow
	}

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		Util.checkErr();
		dealWithChange();
		Util.checkErr();
		if (vertexList.size() == 0) {
			return;
		}
		int pID = Resources.loadShader("gui.vs", "gui.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vaoID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		Util.checkErr();
		if (texture != null) {
			texture.bind(GL13.GL_TEXTURE0);
		}
		Util.setMatrices(pID, location2d);
		Util.checkErr();

		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexList.size());
		Util.checkErr();
		if (texture != null) {
			texture.unbind();
		}
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		Util.checkErr();

		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		Util.checkErr();
	}

	public int textureIndex = 0;

	public void setTextureIndex(int i) {
		// If using an atlas, specify which texture we use
		textureIndex = i;
		setChanged();
	}

}
