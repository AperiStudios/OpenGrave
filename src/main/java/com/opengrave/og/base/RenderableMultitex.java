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
import com.opengrave.og.resources.TextureAtlas;
import com.opengrave.og.resources.TextureEditable;
import com.opengrave.og.util.Matrix4f;

public abstract class RenderableMultitex extends Renderable {

	ArrayList<VertexMultiTex> vertexList = new ArrayList<VertexMultiTex>();
	FloatBuffer posBuffer = BufferUtils.createFloatBuffer(0), texBuffer = BufferUtils.createFloatBuffer(0), texDBuffer = BufferUtils.createFloatBuffer(0),
			normBuffer = BufferUtils.createFloatBuffer(0);
	int vaoID = 0, vbopID = 0, vbotID = 0, vbotDID = 0, vboNID = 0;
	public int hidden = 0;
	protected TextureAtlas textureAtlas = null;
	protected TextureAtlas normalAtlas = null;

	// protected TextureEditable colourTexture = null;

	public abstract TextureEditable getColourTexture();

	public static void init() {
		// terrainshader = new ShaderProgram("terrain.vs", "terrain.fs");
		// terrainshadow = new ShaderProgram("terrainshadow.vs",
		// "castshadow.fs");
		// terrainpicking = new ShaderProgram("terrainshadow.vs",
		// "pickingmodel.fs");
	}

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
		dealWithChange();
		if (vertexList.size() == 0) {
			return;
		}
		int pID = Resources.loadShader("terrainshadow.vs", "castshadow.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vaoID);

		GL20.glEnableVertexAttribArray(0);
		getContext().setShadowMatrices(pID, matrix, shadow);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexList.size());
		// System.out.println(getClass().getName()+" : "+vertexList.size()+" rendered");
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		dealWithChange();
		if (vertexList.size() == 0) {
			return;
		}
		// GL11.glBindTexture(GL11.GL_TEXTURE_2D,
		// Resources.loadTexture("tex/blank.png").id);
		int pID = Resources.loadShader("terrain.vs", "terrain.fs").getProgram();
		GL20.glUseProgram(pID);
		int atlas = GL20.glGetUniformLocation(pID, "arraytexture");
		int colours = GL20.glGetUniformLocation(pID, "colours");
		int shadow = GL20.glGetUniformLocation(pID, "shadowMap");
		int normals = GL20.glGetUniformLocation(pID, "arraytexturedata");
		GL20.glUniform1i(atlas, 0);
		GL20.glUniform1i(colours, 1);
		GL20.glUniform1i(normals, 2);
		GL20.glUniform1i(shadow, 3);
		GL30.glBindVertexArray(vaoID);

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glBindAttribLocation(pID, 0, "in_Position");
		GL20.glBindAttribLocation(pID, 1, "in_TextureCoord");
		GL20.glBindAttribLocation(pID, 2, "in_TexIndex");
		GL20.glBindAttribLocation(pID, 3, "in_Normal");

		getContext().setMatrices(pID, matrix);
		GL20.glUniform1i(GL20.glGetUniformLocation(pID, "hidden"), hidden);
		textureAtlas.bind(GL13.GL_TEXTURE0);
		getColourTexture().bind(GL13.GL_TEXTURE1);
		normalAtlas.bind(GL13.GL_TEXTURE2);
		getContext().bindShadowData(GL13.GL_TEXTURE3);
		getContext().bindLights(pID, GL13.GL_TEXTURE4);
		Util.setRenderStyle(pID, style);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexList.size());
		getContext().unbindLights();
		getContext().unbindShadowData();

		textureAtlas.unbind();
		// HGMainThread.shadowMap.unbind();

		getColourTexture().unbind();
		// System.out.println(getClass().getName()+" : "+vertexList.size()+" rendered");
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public void addVertex(VertexData vd) {
		if (vd instanceof VertexMultiTex) {
			vertexList.add((VertexMultiTex) vd);
		}
	}

	@Override
	public void renderForPicking(Matrix4f matrix, Pickable object) {
		dealWithChange();
		if (vertexList.size() == 0) {
			return;
		}
		int pID = Resources.loadShader("terrainshadow.vs", "pickingmodel.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vaoID);
		GL20.glEnableVertexAttribArray(0);
		getContext().setMatrices(pID, matrix);
		Picking.registerObject(pID, getContext(), object);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexList.size());
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public void dealWithChange() {
		if (vaoID == 0) {
			vaoID = GL30.glGenVertexArrays();
		}
		GL30.glBindVertexArray(vaoID);
		if (vbopID == 0) {
			vbopID = GL15.glGenBuffers();
		}
		if (vbotID == 0) {
			vbotID = GL15.glGenBuffers();
		}
		if (vbotDID == 0) {
			vbotDID = GL15.glGenBuffers();
		}
		if (vboNID == 0) {
			vboNID = GL15.glGenBuffers();
		}
		if (changed) {
			changed = false;
			vertexList.clear();
			posBuffer.clear();
			texBuffer.clear();
			texDBuffer.clear();
			normBuffer.clear();
			recreate();
			int verts = vertexList.size();
			if (vertexList.size() == 0) {
				return;
			}
			if (posBuffer.capacity() != verts * 3) {
				posBuffer = BufferUtils.createFloatBuffer(verts * 3);
			}
			if (texBuffer.capacity() != verts * 3) {
				texBuffer = BufferUtils.createFloatBuffer(verts * 3);
			}
			if (texDBuffer.capacity() != verts * 3) {
				texDBuffer = BufferUtils.createFloatBuffer(verts * 3);
			}
			if (normBuffer.capacity() != verts * 3) {
				normBuffer = BufferUtils.createFloatBuffer(verts * 3);
			}
			for (int i = 0; i < vertexList.size(); i += 3) {
				VertexMultiTex vm0, vm1, vm2;
				vm0 = vertexList.get(i);
				vm1 = vertexList.get(i + 1);
				vm2 = vertexList.get(i + 2);
				posBuffer.put(vm0.x).put(vm0.y).put(vm0.z);
				posBuffer.put(vm1.x).put(vm1.y).put(vm1.z);
				posBuffer.put(vm2.x).put(vm2.y).put(vm2.z);
				texBuffer.put(vm0.tx).put(vm0.ty).put((float) vm0.tex);
				texBuffer.put(vm1.tx).put(vm1.ty).put((float) vm1.tex);
				texBuffer.put(vm2.tx).put(vm2.ty).put((float) vm2.tex);
				texDBuffer.put((float) vm0.tex).put((float) vm1.tex).put((float) vm2.tex);
				texDBuffer.put((float) vm0.tex).put((float) vm1.tex).put((float) vm2.tex);
				texDBuffer.put((float) vm0.tex).put((float) vm1.tex).put((float) vm2.tex);
				normBuffer.put(vm0.nx).put(vm0.ny).put(vm0.nz);
				normBuffer.put(vm1.nx).put(vm1.ny).put(vm1.nz);
				normBuffer.put(vm2.nx).put(vm2.ny).put(vm2.nz);
			}
			posBuffer.flip();
			texBuffer.flip();
			texDBuffer.flip();
			normBuffer.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbopID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotDID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texDBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboNID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, true, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		}

	}

}
