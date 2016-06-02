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
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import com.opengrave.common.world.MaterialList;
import com.opengrave.og.Util;
import com.opengrave.og.engine.BoundingBox;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

/**
 * Rendering mostly-static models. Can deal with change if data is replaced and
 * this.setChanged() is run Not the best object for animating anything
 * 
 * @author triggerhapp
 * 
 */
public abstract class Renderable3D extends Renderable {

	private int indexCount = 0;

	// protected Matrix4f matrix = new Matrix4f();
	// public TextureAtlas texture;

	public static void init() {
		// pickingmodel = new ShaderProgram("model.vs", "pickingmodel.fs");
	}

	public ArrayList<Vertex3D> vertexList = new ArrayList<Vertex3D>();
	FloatBuffer buffer;
	int vao_ID = 0, vbo_pos_ID = 0, vbo_norm_ID = 0, vbo_tex_ID = 0;
	public boolean visible = false;
	protected Vector4f color = new Vector4f(0f, 0f, 0f, 1f);
	// private int vertCount;
	protected MaterialList matList;
	private int vbo_index_ID;
	protected BoundingBox box = new BoundingBox();

	public void setMaterialList(MaterialList mlist) {
		this.matList = mlist;
	}

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		dealWithChange();
		int pID = Resources.loadShader("model.vs", "model.fs").getProgram();
		GL20.glUseProgram(pID);
		int texture = GL20.glGetUniformLocation(pID, "texture");
		int shadow = GL20.glGetUniformLocation(pID, "shadowMap");
		GL20.glUniform1i(texture, 0);
		GL20.glUniform1i(shadow, 1);
		GL30.glBindVertexArray(vao_ID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glBindAttribLocation(pID, 0, "in_Position");
		GL20.glBindAttribLocation(pID, 1, "in_Normal");
		GL20.glBindAttribLocation(pID, 2, "in_TextureCoord");

		Util.checkErr();
		if (matList != null && matList.valid()) {
			matList.bind(pID, GL13.GL_TEXTURE0);
			Util.loadMaterials(matList, pID);

		}
		Util.checkErr();
		getContext().setMatrices(pID, matrix);
		getContext().bindShadowData(GL13.GL_TEXTURE2);
		getContext().bindLights(pID, GL13.GL_TEXTURE3);
		Util.setRenderStyle(pID, style);

		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo_index_ID);
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		getContext().unbindLights();
		getContext().unbindShadowData();

		if (matList != null && matList.valid()) {
			matList.unbind();
		}
		Util.checkErr();

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		Util.checkErr();

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
		dealWithChange();

		int pID = Resources.loadShader("model.vs", "castshadow.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vao_ID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glBindAttribLocation(pID, 0, "in_Position");

		Util.checkErr();
		getContext().setShadowMatrices(pID, matrix, shadow);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo_index_ID);
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		Util.checkErr();

		GL20.glDisableVertexAttribArray(0);
		Util.checkErr();

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	@Override
	public void dealWithChange() {
		if (vao_ID == 0) {
			vao_ID = GL30.glGenVertexArrays();
			changed = true;
		}
		// set VAO object
		GL30.glBindVertexArray(vao_ID);
		if (vbo_index_ID == 0) {
			vbo_index_ID = GL15.glGenBuffers();
		}
		if (vbo_pos_ID == 0) {
			vbo_pos_ID = GL15.glGenBuffers();
		}
		if (vbo_norm_ID == 0) {
			vbo_norm_ID = GL15.glGenBuffers();
		}
		if (vbo_tex_ID == 0) {
			vbo_tex_ID = GL15.glGenBuffers();
		}

		if (changed) {

			recreate();
			changed = false;
			indexCount = vertexList.size();
			IntBuffer indexList = BufferUtils.createIntBuffer(indexCount);
			ArrayList<Vertex3D> verts = new ArrayList<Vertex3D>();
			for (Vertex3D thisVert : vertexList) {
				// Vertex3D thisVert = vertexList.get(i);
				int index = -1;
				if (verts.indexOf(thisVert) == -1) {
					verts.add(thisVert);
				}
				index = verts.indexOf(thisVert);
				indexList.put(index);
			}
			int vertCount = verts.size();
			indexList.flip();
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo_index_ID);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexList, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

			FloatBuffer pos = BufferUtils.createFloatBuffer(vertCount * 3);
			FloatBuffer norm = BufferUtils.createFloatBuffer(vertCount * 3);
			FloatBuffer tex = BufferUtils.createFloatBuffer(vertCount * 3);
			box.clear();
			for (int i = 0; i < verts.size(); i++) {
				Vertex3D v1 = verts.get(i);
				box.addVector3f(v1.getPos());
				pos.put(v1.x).put(v1.y).put(v1.z);
				norm.put(v1.nx).put(v1.ny).put(v1.nz);
				tex.put(v1.tx).put(v1.ty).put(v1.tz);
			}

			// Set Vertex data
			pos.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_pos_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pos, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

			// Set Norm data
			norm.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_norm_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, norm, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 0, 0);

			// Set Texture Co-ord data
			tex.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_tex_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tex, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);

			// Unset VBO object
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		}
		// Unset VAO object
		GL30.glBindVertexArray(0);
	}

	@Override
	public void addVertex(VertexData vd) {
		changed = true;
		if (vd instanceof Vertex3D) {
			vertexList.add((Vertex3D) vd);
		}
	}

	@Override
	public void renderForPicking(Matrix4f matrix, Pickable object) {
		dealWithChange();
		int pID = Resources.loadShader("model.vs", "pickingmodel.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vao_ID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glBindAttribLocation(pID, 0, "in_Position");

		Util.checkErr();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo_index_ID);
		getContext().setMatrices(pID, matrix);
		Picking.registerObject(pID, getContext(), object);
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		Util.checkErr();

		GL20.glDisableVertexAttribArray(0);
		Util.checkErr();

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	public BoundingBox getBoundingBox() {
		dealWithChange();
		return box;
	}
}
