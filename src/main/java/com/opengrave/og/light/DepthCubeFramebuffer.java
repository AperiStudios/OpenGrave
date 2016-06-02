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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.opengrave.og.Util;

public class DepthCubeFramebuffer {

	private int framebuffer, framebufferSize;
	private TextureCubeShadowMap shadowMap;

	public DepthCubeFramebuffer(int size) {
		framebufferSize = size;
		framebuffer = GL30.glGenFramebuffers();
		// System.out.println("Creating Pointlight Framebuffer : " +
		// framebuffer);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
		shadowMap = new TextureCubeShadowMap(framebufferSize);

		// shadowMap.bindToFrameBuffer();
		shadowMap.bindToFrameBuffer(0);
		// GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		GL11.glDrawBuffer(GL11.GL_NONE);
		Util.checkErr();
		GL11.glReadBuffer(GL11.GL_NONE);
		int i = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (i != GL30.GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Framebuffer creation failed with code: " + i);
		}
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		Util.checkErr();
	}

	public void bindDraw(int direction) {
		Util.checkErr();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
		shadowMap.bindToFrameBuffer(direction);
		Util.checkErr();

		Util.checkErr();
		GL11.glDisable(GL11.GL_BLEND);
		Util.checkErr();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LESS);
		GL11.glDepthMask(true);
		Util.checkErr();
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		Util.checkErr();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glViewport(0, 0, framebufferSize, framebufferSize);

	}

	public void dumpTestingImage() {

	}

	public void unbindDraw() {
		GL11.glFlush();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	public void bindDepthTexture(int texNo) {
		shadowMap.bind(texNo);
	}

	public void unbindDepthTexture() {
		shadowMap.unbind();

	}

}
