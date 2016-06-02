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
package com.opengrave.og.light;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import com.opengrave.og.Util;
import com.opengrave.og.resources.Framebuffer;
import com.opengrave.og.resources.Resources;

public class Depth2DFramebuffer implements Framebuffer {

	private int framebuffer, framebufferSize;
	private Texture2DShadowMap shadowMap;

	private int count, vao_ID;

	public Depth2DFramebuffer(int size) {
		framebufferSize = size;
		framebuffer = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
		shadowMap = new Texture2DShadowMap(framebufferSize);

		shadowMap.bindToFrameBuffer();

		// GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		GL11.glDrawBuffer(GL11.GL_NONE);
		Util.checkErr();
		GL11.glReadBuffer(GL11.GL_NONE);
		int i = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (i != GL30.GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Framebuffer creation failed with code: " + i);
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		// TESTING STUFF
		count = 2;
		FloatBuffer pos = BufferUtils.createFloatBuffer(3 * 3 * count);
		FloatBuffer tex = BufferUtils.createFloatBuffer(2 * 3 * count);
		pos.put(0.75f).put(0.75f).put(0f);
		pos.put(0.75f).put(0.25f).put(0f);
		pos.put(0.25f).put(0.75f).put(0f);
		pos.put(0.25f).put(0.25f).put(0f);

		pos.put(0.75f).put(0.25f).put(0f);
		pos.put(0.25f).put(0.75f).put(0f);
		pos.flip();
		tex.put(1f).put(1f);
		tex.put(1f).put(0f);
		tex.put(0f).put(1f);
		tex.put(0f).put(0f);
		tex.put(1f).put(0f);

		tex.put(0f).put(1f);
		tex.flip();
		vao_ID = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vao_ID);
		int vbo_pos_ID = GL15.glGenBuffers();
		int vbo_tex_ID = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_pos_ID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, pos, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_tex_ID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, tex, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
	}

	@Override
	public void bindDraw() {
		Util.checkErr();

		for (int i = GL13.GL_TEXTURE0; i < GL13.GL_TEXTURE31; i++) {
			GL13.glActiveTexture(i);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
			GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);
		}
		Util.checkErr();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
		Util.checkErr();
		GL11.glDisable(GL11.GL_BLEND);
		Util.checkErr();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LESS);
		GL11.glDepthMask(true);
		Util.checkErr();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		Util.checkErr();
		GL11.glViewport(0, 0, framebufferSize, framebufferSize);

	}

	@Override
	public void unbindDraw() {
		GL11.glFlush();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	@Override
	public void unbindImageTexture() {
		// nop

	}

	public void dumpTestingImage() {
		// TESTING
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		int pID = Resources.loadShader("test.vs", "test.fs").getProgram();
		GL20.glUseProgram(pID);
		int shadow = GL20.glGetUniformLocation(pID, "shadowMap");
		GL20.glUniform1i(shadow, 0);
		GL30.glBindVertexArray(vao_ID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		shadowMap.bind(GL13.GL_TEXTURE0);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, count * 3);
		shadowMap.unbind();
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		// END TESTING
	}

	@Override
	public void bindDepthTexture(int texNo) {
		shadowMap.bind(texNo);
	}

	@Override
	public void unbindDepthTexture() {
		shadowMap.unbind();

	}

	@Override
	public void bindImageTexture(int texNo) {

	}

}
