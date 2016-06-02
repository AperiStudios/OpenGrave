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
package com.opengrave.og.resources;

import java.io.*;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.opengrave.common.DebugExceptionHandler;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextureDead {

	public int width;
	public int height;
	private int id = -1;

	public int getTextureType() {
		return GL11.GL_TEXTURE_2D;
	}

	public void bind() {
		// GL11.glBindTexture(getTextureType(), id);
	}

	public void unbind() {
		// GL11.glBindTexture(getTextureType(), 0);
	}

	public static TextureDead create(String location) {
		InputStream in = null;
		File f = new File(Resources.cache, location);
		try {
			in = new FileInputStream(f.getAbsolutePath());
		} catch (FileNotFoundException e1) {
			System.out.println("Cannot open file " + f.getAbsolutePath());
			return null;
		}
		PNGDecoder decoder;
		TextureDead textureObject = new TextureDead();
		int texture = -1;
		try {
			decoder = new PNGDecoder(in);
			textureObject.width = decoder.getWidth();
			textureObject.height = decoder.getHeight();
			System.out.println("Width : " + decoder.getWidth() + " Height : " + decoder.getHeight());
			ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			buf.flip();
			texture = GL11.glGenTextures();
			textureObject.id = texture;
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
			// GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		} catch (IOException e) {
			new DebugExceptionHandler(e, location);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
		return textureObject;

	}

	public boolean isValid() {
		return id >= 0;
	}

}
