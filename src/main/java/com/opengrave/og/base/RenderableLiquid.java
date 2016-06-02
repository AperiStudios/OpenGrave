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

/**
 * While similar to RenderableMultitex, liquids should never blend from one
 * texture to another in the same quad.
 * 
 * @author triggerhapp
 * 
 */
public abstract class RenderableLiquid extends Renderable {

	protected TextureAtlas textureAtlas = null;
	protected TextureAtlas normalAtlas = null;
	// protected TextureEditable colourTexture = null, flowTexture = null;
	private int vaoID;
	private int vbopID;
	private int vbooID;
	private int vbonID;
	FloatBuffer posBuffer = BufferUtils.createFloatBuffer(0);
	FloatBuffer offsetBuffer = BufferUtils.createFloatBuffer(0);
	FloatBuffer normBuffer = BufferUtils.createFloatBuffer(0);
	ArrayList<VertexLiquid> vertexList = new ArrayList<VertexLiquid>();

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		dealWithChange();
		if (vertexList.size() == 0) {
			return;
		}
		int pID = Resources.loadShader("liquid.vs", "liquid.fs").getProgram();
		GL20.glUseProgram(pID);
		int atlas = GL20.glGetUniformLocation(pID, "arraytexture");
		int normals = GL20.glGetUniformLocation(pID, "arraytexturedata");
		int colours = GL20.glGetUniformLocation(pID, "colours");
		int flows = GL20.glGetUniformLocation(pID, "flows");
		int shadow = GL20.glGetUniformLocation(pID, "shadowMap");

		GL20.glUniform1i(atlas, 0);
		GL20.glUniform1i(normals, 1);
		GL20.glUniform1i(colours, 2);
		GL20.glUniform1i(flows, 3);
		GL20.glUniform1i(shadow, 4);
		GL30.glBindVertexArray(vaoID);

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		getContext().setMatrices(pID, matrix);
		textureAtlas.bind(GL13.GL_TEXTURE0);
		normalAtlas.bind(GL13.GL_TEXTURE1);
		getColourTexture().bind(GL13.GL_TEXTURE2);
		getFlowTexture().bind(GL13.GL_TEXTURE3);

		getContext().bindShadowData(GL13.GL_TEXTURE4);
		getContext().bindLights(pID, GL13.GL_TEXTURE5);
		Util.setRenderStyle(pID, style);
		GL11.glDepthMask(false);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexList.size());
		GL11.glDepthMask(true);
		getContext().unbindLights();
		getContext().unbindShadowData();

		textureAtlas.unbind();
		getFlowTexture().unbind();
		getColourTexture().unbind();
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);

		GL30.glBindVertexArray(0);

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	public abstract TextureEditable getFlowTexture();

	public abstract TextureEditable getColourTexture();

	@Override
	public void renderForPicking(Matrix4f matrix, Pickable object) {
		dealWithChange();
	}

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
		dealWithChange();
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
		if (vbooID == 0) {
			vbooID = GL15.glGenBuffers();
		}
		if (vbonID == 0) {
			vbonID = GL15.glGenBuffers();
		}
		if (changed) {
			vertexList.clear();
			posBuffer.clear();
			offsetBuffer.clear();
			normBuffer.clear();
			recreate();
			changed = false;
			int verts = vertexList.size();
			if (vertexList.size() == 0) {
				return;
			}
			if (posBuffer.capacity() != verts * 4) {
				posBuffer = BufferUtils.createFloatBuffer(verts * 4);
			}
			if (offsetBuffer.capacity() != verts * 4) {
				offsetBuffer = BufferUtils.createFloatBuffer(verts * 4);
			}
			if (normBuffer.capacity() != verts * 3) {
				normBuffer = BufferUtils.createFloatBuffer(verts * 3);
			}
			for (int i = 0; i < vertexList.size(); i += 3) {
				VertexLiquid vm0, vm1, vm2;
				vm0 = vertexList.get(i);
				vm1 = vertexList.get(i + 1);
				vm2 = vertexList.get(i + 2);
				posBuffer.put(vm0.x).put(vm0.y).put(vm0.z).put(vm0.tex);
				posBuffer.put(vm1.x).put(vm1.y).put(vm1.z).put(vm0.tex);
				posBuffer.put(vm2.x).put(vm2.y).put(vm2.z).put(vm0.tex);
				offsetBuffer.put(0).put(0).put(0).put(0);
				offsetBuffer.put(vm1.x - vm0.x).put(vm1.y - vm0.y).put(0).put(0);
				offsetBuffer.put(vm2.x - vm0.x).put(vm2.y - vm0.y).put(0).put(0);
				normBuffer.put(vm0.nx).put(vm0.ny).put(vm0.nz);
				normBuffer.put(vm1.nx).put(vm1.ny).put(vm1.nz);
				normBuffer.put(vm2.nx).put(vm2.ny).put(vm2.nz);
			}
			posBuffer.flip();

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbopID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, posBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);

			offsetBuffer.flip();

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbooID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, offsetBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);

			normBuffer.flip();

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normBuffer, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		}

	}

	@Override
	public void addVertex(VertexData vd) {
		if (vd instanceof VertexLiquid) {
			vertexList.add((VertexLiquid) vd);
		}
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void delete() {
	}

}
