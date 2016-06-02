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
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.og.Util;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class TextureAtlas implements Texture {

	int lastTexNum = 0;
	int depth = -1;
	int width = -1, height = -1;
	int id = -1;
	String token = "";

	@Override
	public int getTextureType() {
		return GL30.GL_TEXTURE_2D_ARRAY;
	}

	@Override
	public void bind(int t) {
		// GLSL Will assume the order if we don't Uniform it. I like that.
		// GL20.glUniform1i(i, 0);
		lastTexNum = t;
		GL13.glActiveTexture(t);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, id);
		Util.checkErr();
	}

	@Override
	public void unbind() {
		Util.checkErr();
		GL13.glActiveTexture(lastTexNum);
		Util.checkErr();
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		Util.checkErr();
	}

	@Override
	public boolean isValid() {
		return id >= 0;
	}

	protected static TextureAtlas create(ArrayList<String> files) {
		if (files.size() == 0) {
			// WAT
			files.add("blank");
		}
		TextureAtlas atlas = new TextureAtlas();
		atlas.id = GL11.glGenTextures();

		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, atlas.id);
		Util.checkErr();
		atlas.depth = getPowerOfTwo(files.size());
		while (files.size() < atlas.depth) {
			files.add("blank");
		}
		// System.out.println("Texture Atlas id : " + atlas.id + " size : "
		// + files.size() + " end size : " + atlas.depth);
		atlas.width = 1;
		atlas.height = 1;
		for (int i = 0; i < files.size(); i++) {
			if (files.get(i).equalsIgnoreCase("blank") || files.get(i).equalsIgnoreCase("none")) {
				continue;
			}
			PNGDecoder firstImage = getImageFile(files.get(i));
			if (firstImage == null) {
				return null;
			}
			atlas.width = firstImage.getWidth();
			atlas.height = firstImage.getHeight();
			break;
		}
		Util.checkErr();
		// ARBTextureStorage.glTexStorage3D(atlas.getTextureType(), 1,
		// GL11.GL_RGBA, atlas.width, atlas.height, atlas.depth); // Throws an
		// error for GL_RGBA, it wants an internal format only.
		// Inversly. No GL_RGBAx values work.
		// Util.checkErr(false);
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * atlas.width * atlas.height * atlas.depth);
		for (String location : files) {
			if (location.equalsIgnoreCase("blank")) {
				byte b = (byte) 255;
				for (int count = 0; count < atlas.width * atlas.height; count++) {
					buf.put(b).put(b).put(b).put(b);
				}
				// System.out
				// .println("Added BLANK to atlas as number" + (index++));
				atlas.token = atlas.token + ":blank";
			} else if (location.equalsIgnoreCase("none")) {
				byte b = (byte) 0;
				for (int count = 0; count < atlas.width * atlas.height; count++) {
					buf.put(b).put(b).put(b).put(b);
				}
				// System.out
				// .println("Added BLANK to atlas as number" + (index++));
				atlas.token = atlas.token + ":blank";
			} else {

				PNGDecoder decoder = getImageFile(location);
				if (decoder == null) {
					byte b = (byte) 255;
					for (int count = 0; count < atlas.width * atlas.height; count++) {
						buf.put(b).put(b).put(b).put(b);
					}
				} else {
					if (decoder.getWidth() != atlas.width || decoder.getHeight() != atlas.height) {
						System.out.println("Non-standard image size. All images" + " in an atlas must have the same size");
						System.out.println("Offender : " + location);
						return null;
					}
					try {
						decoder.decode(buf, atlas.width * 4, Format.RGBA);
					} catch (IOException e) {
						new DebugExceptionHandler(e);
					}
				}
				// System.out.println("Added " + location +
				// " to atlas as number "
				// + (index++));
				atlas.token = atlas.token + ":" + location;
			}

		}
		buf.flip();
		GL12.glTexImage3D(GL30.GL_TEXTURE_2D_ARRAY, 0, GL11.GL_RGBA, atlas.width, atlas.height, atlas.depth, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		Util.checkErr();
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		Util.checkErr();
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		Util.checkErr();
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		Util.checkErr();

		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		Util.checkErr();
		// GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D_ARRAY);
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		Util.checkErr();
		return atlas;
	}

	public static PNGDecoder getImageFile(String fileName) {
		InputStream in = null;
		File f = new File(Resources.cache, fileName);
		try {
			in = new FileInputStream(f.getAbsolutePath());
		} catch (FileNotFoundException e1) {
			System.out.println("Cannot open file " + f.getAbsolutePath());
			return null;
		}
		PNGDecoder decoder;
		try {
			decoder = new PNGDecoder(in);
		} catch (IOException e) {
			new DebugExceptionHandler(e, fileName);
			return null;
		}
		return decoder;
	}

	private static int getPowerOfTwo(int x) {
		if (x < 0) {
			return 0;
		}
		x--;
		x |= x >> 1;
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		x |= x >> 16;
		return x + 1;
	}

	public void shrug() {

	}

	public int size() {
		return depth;
	}

	@Override
	public String toString() {
		return "(Texture - " + token + ")";
	}
}
