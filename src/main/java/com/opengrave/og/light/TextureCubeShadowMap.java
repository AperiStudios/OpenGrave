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

import org.lwjgl.opengl.*;

import com.opengrave.og.Util;
import com.opengrave.og.resources.Texture;

public class TextureCubeShadowMap implements Texture {

	int texture = 0;
	// int texture2 = 0;
	int lastTexNum = 0;

	public TextureCubeShadowMap(int size) {
		texture = GL11.glGenTextures();
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		Util.checkErr();
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		Util.checkErr();
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		Util.checkErr();
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		Util.checkErr();
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_INTENSITY);
		// GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP,
		// GL12.GL_TEXTURE_BASE_LEVEL, 0);
		// GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP,
		// GL12.GL_TEXTURE_MAX_LEVEL, 0);

		for (int i = 0; i < 6; i++) {
			GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL11.GL_DEPTH_COMPONENT, size, size, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT,
					(FloatBuffer) null);
		}
		Util.checkErr();
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);

		// texture2 = GL11.glGenTextures();
		// GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture2);
		// GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0,GL11.GL_RGB, size, size,
		// 0,GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (FloatBuffer)null);
		// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
		// GL11.GL_NEAREST);
		// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
		// GL11.GL_NEAREST);
		// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
		// GL12.GL_CLAMP_TO_EDGE);
		// GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
		// GL12.GL_CLAMP_TO_EDGE);
		// GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

	}

	public void bindToFrameBuffer(int direction) {
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + direction, texture, 0);
		Util.checkErr();
		// GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER,
		// GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture2, 0);
		// Util.checkErr();

	}

	@Override
	public int getTextureType() {
		return GL13.GL_TEXTURE_CUBE_MAP;
	}

	@Override
	public void bind(int t) {
		if (t == 0 || texture == 0) {
			lastTexNum = 0;
			return;
		}
		lastTexNum = t;
		GL13.glActiveTexture(t);
		Util.checkErr();
		GL11.glBindTexture(getTextureType(), texture);
		Util.checkErr();
	}

	@Override
	public void unbind() {
		if (lastTexNum == 0) {
			return;
		}
		Util.checkErr();
		GL13.glActiveTexture(lastTexNum);
		Util.checkErr();
		GL11.glBindTexture(getTextureType(), 0);
		Util.checkErr();
	}

	@Override
	public boolean isValid() {
		return true;
	}

}
