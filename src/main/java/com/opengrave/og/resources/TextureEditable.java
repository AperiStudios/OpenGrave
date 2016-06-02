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

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import com.opengrave.og.Util;
import com.opengrave.og.util.Vector4f;

public class TextureEditable implements Texture {

	int id = 0, size = 0;
	int lastTexNum = 0;
	ArrayList<TextureEditableDeferedChanges> changeList = new ArrayList<TextureEditableDeferedChanges>();
	ByteBuffer mainBuf;

	public void setColourAt(int x, int y, Vector4f col) {
		TextureEditableDeferedChanges newChange = new TextureEditableDeferedChanges(x, y, col);
		synchronized (changeList) {
			int index = changeList.indexOf(newChange);
			if (index != -1) {
				TextureEditableDeferedChanges a = changeList.get(index);
				a.setColour(col);
				return;
			}
			changeList.add(newChange);
		}
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

	public TextureEditable(int size, float r, float g, float b, float a) {

		this.size = getPowerOfTwo(size);
		id = GL11.glGenTextures();
		mainBuf = BufferUtils.createByteBuffer(4 * this.size * this.size);
		for (int count = 0; count < this.size * this.size; count++) {
			addColour(r, g, b, a);
		}
		mainBuf.flip();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, this.size, this.size, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, mainBuf);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public int getTextureType() {
		return GL11.GL_TEXTURE_2D;
	}

	public void applyChangeToMainBuffer(TextureEditableDeferedChanges change) {
		mainBuf.position((change.getX() + change.getY() * size) * 4);
		addColour(change.getColour());
		mainBuf.position(0);
	}

	private void addColour(Vector4f colour) {
		addColour(colour.x, colour.y, colour.z, colour.w);
	}

	public void addColour(float r, float g, float b, float a) {
		mainBuf.put((byte) (255 * b)).put((byte) (255 * g)).put((byte) (255 * r)).put((byte) (255 * a));
	}

	@Override
	public void bind(int t) {
		GL13.glActiveTexture(t);
		synchronized (changeList) {
			if (changeList.size() < 10) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

				for (TextureEditableDeferedChanges change : changeList) {
					applyChangeToMainBuffer(change);
					ByteBuffer buf = BufferUtils.createByteBuffer(4);
					buf.put((byte) (255 * change.getColour().z)).put((byte) (255 * change.getColour().y)).put((byte) (255 * change.getColour().x))
							.put((byte) (255 * change.getColour().w));
					buf.flip();
					GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, change.getX(), change.getY(), 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);
				}
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			} else {
				// Replace the whole image. There's bound to be better ways
				// around this...
				for (TextureEditableDeferedChanges change : changeList) {
					applyChangeToMainBuffer(change);
				}
				mainBuf.position(0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
				Util.checkErr();
				GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, size, size, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, mainBuf);
				Util.checkErr();
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				Util.checkErr();

			}
			changeList.clear();
		}
		Util.checkErr();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		lastTexNum = t;
	}

	@Override
	public void unbind() {
		GL13.glActiveTexture(lastTexNum);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	public int getTextureInt() {
		return id;
	}

	public Vector4f getColourAt(int x, int y) {
		Vector4f col = new Vector4f();
		mainBuf.position((x + y * size) * 4);
		col.x = mainBuf.get();
		col.y = mainBuf.get();
		col.z = mainBuf.get();
		col.w = mainBuf.get();
		synchronized (changeList) {
			for (TextureEditableDeferedChanges change : changeList) {
				if (change.getX() == x && change.getY() == y) {
					col = change.getColour();
				}
			}
		}
		return col;

	}

	public void dumpChanges(ArrayList<TextureEditableDeferedChanges> colChange) {
		synchronized (changeList) {
			changeList.addAll(colChange);
		}
	}

}
