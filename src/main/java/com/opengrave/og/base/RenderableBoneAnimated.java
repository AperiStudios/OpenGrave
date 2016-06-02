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
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.resources.Texture;
import com.opengrave.og.util.Matrix4f;

public abstract class RenderableBoneAnimated extends Renderable3D {

	protected Texture texture;
	private MaterialList matList;

	IntBuffer bone;
	int vertCount = -1;
	int vao_ID = 0, vbo_pos_ID = 0, vbo_norm_ID = 0, vbo_tex_ID = 0, vbo_weight_ID = 0, vbo_bone_ID = 0;
	public boolean visible = true;

	public abstract void setBonesUniform(int pID);

	public static void init() {
		// pickingboneshader = Resources.loadShader("pickingmodel.vs",
		// "pickingmodel.fs");
	}

	protected ArrayList<VertexAnimated> vertexList = new ArrayList<VertexAnimated>();
	public Matrix4f matrix = new Matrix4f();
	private int vbo_index_ID;
	private int indexCount;

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		// if (!visible) {
		// return;
		// }
		GL11.glDisable(GL11.GL_CULL_FACE); // TODO Enable Face Culling, and be
											// absolutely sure that all tris are
											// facing the correct way
		dealWithChange();

		int pID = Resources.loadShader("animmodel.vs", "animmodel.fs").getProgram();
		GL20.glUseProgram(pID);
		int texture = GL20.glGetUniformLocation(pID, "texture");
		int shadow = GL20.glGetUniformLocation(pID, "shadowMap");
		GL20.glUniform1i(texture, 0);
		GL20.glUniform1i(shadow, 1);
		GL30.glBindVertexArray(vao_ID);
		GL20.glBindAttribLocation(pID, 0, "in_Position");
		GL20.glBindAttribLocation(pID, 1, "in_Normal");
		GL20.glBindAttribLocation(pID, 2, "in_TextureCoord");
		GL20.glBindAttribLocation(pID, 3, "in_BoneID");
		GL20.glBindAttribLocation(pID, 4, "in_BoneWeight");

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		if (matList != null && matList.valid()) {
			matList.bind(pID, GL13.GL_TEXTURE0);
			Util.loadMaterials(matList, pID);

		}
		setBonesUniform(pID);

		getContext().setMatrices(pID, matrix);
		getContext().bindShadowData(GL13.GL_TEXTURE2);
		getContext().bindLights(pID, GL13.GL_TEXTURE3);
		Util.setRenderStyle(pID, style);
		// GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertCount);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo_index_ID);
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		getContext().unbindLights();
		getContext().unbindShadowData();
		if (matList != null && matList.valid()) {
			matList.unbind();
		}
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);

		GL20.glUseProgram(0);
		GL30.glBindVertexArray(0);

		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
		// if (!visible) {
		// return;
		// }
		dealWithChange();

		int pID = Resources.loadShader("animmodel.vs", "animmodel.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vao_ID);
		GL20.glBindAttribLocation(pID, 0, "in_Position");
		GL20.glBindAttribLocation(pID, 3, "in_BoneID");
		GL20.glBindAttribLocation(pID, 4, "in_BoneWeight");

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		setBonesUniform(pID);
		getContext().setShadowMatrices(pID, matrix, shadow);
		// GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertCount);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo_index_ID);
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);

	}

	@Override
	public void renderForPicking(Matrix4f matrix, Pickable object) {
		dealWithChange();

		int pID = Resources.loadShader("animmodel.vs", "pickingmodel.fs").getProgram();
		GL20.glUseProgram(pID);
		GL30.glBindVertexArray(vao_ID);
		GL20.glBindAttribLocation(pID, 0, "in_Position");
		GL20.glBindAttribLocation(pID, 3, "in_BoneID");
		GL20.glBindAttribLocation(pID, 4, "in_BoneWeight");

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		setBonesUniform(pID);
		// Util.setShadowMatrices(pID, shadow, location, scale, rotation,
		// matrix);
		Picking.registerObject(pID, getContext(), object);
		getContext().setMatrices(pID, matrix);
		// GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertCount);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo_index_ID);
		GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);

	}

	@Override
	public void dealWithChange() {
		if (vao_ID == 0) {
			vao_ID = GL30.glGenVertexArrays();
		}
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
		if (vbo_weight_ID == 0) {
			vbo_weight_ID = GL15.glGenBuffers();
		}
		if (vbo_bone_ID == 0) {
			vbo_bone_ID = GL15.glGenBuffers();
		}

		if (changed) {
			// vertexList.clear();
			recreate();
			changed = false;
			indexCount = vertexList.size();
			IntBuffer indexList = BufferUtils.createIntBuffer(indexCount);
			ArrayList<VertexAnimated> verts = new ArrayList<VertexAnimated>();
			for (VertexAnimated thisVert : vertexList) {
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
			FloatBuffer bone = BufferUtils.createFloatBuffer(vertCount * 4);
			FloatBuffer weight = BufferUtils.createFloatBuffer(vertCount * 4);
			// box.clear();
			for (int i = 0; i < verts.size(); i++) {
				VertexAnimated v1 = verts.get(i);
				// box.addVector3f(v1.getPos());
				pos.put(v1.x).put(v1.y).put(v1.z);
				norm.put(v1.nx).put(v1.ny).put(v1.nz);
				tex.put(v1.tx).put(v1.ty).put(v1.tz);
				bone.put(v1.influence.getBoneId(0)).put(v1.influence.getBoneId(1)).put(v1.influence.getBoneId(2)).put(v1.influence.getBoneId(3));
				weight.put(v1.influence.getWeight(0)).put(v1.influence.getWeight(1)).put(v1.influence.getWeight(2)).put(v1.influence.getWeight(3));
			}

			pos.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_pos_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pos, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

			norm.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_norm_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, norm, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, true, 0, 0);

			tex.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_tex_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tex, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);

			bone.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_bone_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bone, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, 0, 0);

			weight.flip();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_weight_ID);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, weight, GL15.GL_STATIC_DRAW);
			GL20.glVertexAttribPointer(4, 4, GL11.GL_FLOAT, false, 0, 0);
			/*
			 * changed = false;
			 * 
			 * vertCount = vertexList.size(); FloatBuffer pos =
			 * BufferUtils.createFloatBuffer(vertCount * 3); FloatBuffer norm =
			 * BufferUtils.createFloatBuffer(vertCount * 3); FloatBuffer tex =
			 * BufferUtils.createFloatBuffer(vertCount * 3); FloatBuffer bone =
			 * BufferUtils.createFloatBuffer(vertCount * 4); FloatBuffer weight
			 * = BufferUtils.createFloatBuffer(vertCount * 4); for
			 * (VertexAnimated va : vertexList) {
			 * pos.put(va.x).put(va.y).put(va.z);
			 * norm.put(va.nx).put(va.ny).put(va.nz);
			 * tex.put(va.tx).put(va.ty).put(va.tz);
			 * bone.put(va.influence.getBoneId(0))
			 * .put(va.influence.getBoneId(1)) .put(va.influence.getBoneId(2))
			 * .put(va.influence.getBoneId(3));
			 * weight.put(va.influence.getWeight(0))
			 * .put(va.influence.getWeight(1)) .put(va.influence.getWeight(2))
			 * .put(va.influence.getWeight(3)); }
			 * 
			 * pos.flip(); GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_pos_ID);
			 * GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pos,
			 * GL15.GL_STATIC_DRAW); GL20.glVertexAttribPointer(0, 3,
			 * GL11.GL_FLOAT, false, 0, 0);
			 * 
			 * norm.flip(); GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,
			 * vbo_norm_ID); GL15.glBufferData(GL15.GL_ARRAY_BUFFER, norm,
			 * GL15.GL_STATIC_DRAW); GL20.glVertexAttribPointer(1, 3,
			 * GL11.GL_FLOAT, false, 0, 0);
			 * 
			 * tex.flip(); GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_tex_ID);
			 * GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tex,
			 * GL15.GL_STATIC_DRAW); GL20.glVertexAttribPointer(2, 3,
			 * GL11.GL_FLOAT, false, 0, 0);
			 * 
			 * bone.flip(); GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,
			 * vbo_bone_ID); GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bone,
			 * GL15.GL_STATIC_DRAW); GL20.glVertexAttribPointer(3, 4,
			 * GL11.GL_FLOAT, false, 0, 0);
			 * 
			 * weight.flip(); GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,
			 * vbo_weight_ID); GL15.glBufferData(GL15.GL_ARRAY_BUFFER, weight,
			 * GL15.GL_STATIC_DRAW); GL20.glVertexAttribPointer(4, 4,
			 * GL11.GL_FLOAT, false, 0, 0);
			 */
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		}
		GL30.glBindVertexArray(0);
	}

	public ArrayList<VertexAnimated> getVertexList() {
		return vertexList;
	}

	@Override
	public void addVertex(VertexData vd) {
		changed = true;
		if (vd instanceof VertexAnimated) {
			vertexList.add((VertexAnimated) vd);
		} else {
			throw (new RuntimeException("Wrong Vertex Type"));
		}
	}

	public void setMatrix(Matrix4f matrix) {
		// Wrong place to do it. Translates will all be wrong
		// matrix.scale(new Vector3f(0.05f,0.05f,0.05f));
		this.matrix = matrix;
	}

	public void setMaterialList(MaterialList mlist) {
		this.matList = mlist;
	}
}
