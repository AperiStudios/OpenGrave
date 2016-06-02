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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;

import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.RenderView;
import com.opengrave.og.states.BaseState;
import com.opengrave.og.terrain.TerrainLayerNode;
import com.opengrave.og.util.Vector4f;

public class Picking {

	static HashMap<Long, Pickable> allPickables;
	static long lastInt = 0;

	public static void pickRender(BaseState s) {
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		allPickables = new HashMap<Long, Pickable>();
		lastInt = 1;
		MainThread.set2D();
		s.renderGuiForPicking();
	}

	public static void pickCleanup() {
		lastInt = 0;
		allPickables = null;
		GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	public static PickingResult pick(int x, int y) {
		PickingResult res = new PickingResult();
		Util.checkErr();
		ByteBuffer pixel = BufferUtils.createByteBuffer(32);
		Util.checkErr();
		GL11.glReadPixels(x, y, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, pixel);
		Util.checkErr();
		int b = byteToInt(pixel.get()), g = byteToInt(pixel.get()), r = byteToInt(pixel.get());
		Util.checkErr();
		long chosenL = r + (g << 8) + (b << 16) + (0 << 24);
		res.worldLoc = null;
		res.picked = allPickables.get(chosenL);
		if (chosenL != 0) {
			// Get collision point, either null for GUI or world co-ords
			FloatBuffer buf = BufferUtils.createFloatBuffer(1);
			Util.checkErr();
			GL11.glReadPixels(x, y, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, buf);
			Util.checkErr();
			float dist = buf.get();
			// Infinite distance (1f) means nothing was rendered there.
			if (dist != 1f) {
				if (res.picked.getContext() != null) {
					RenderView context = res.picked.getContext();
					FloatBuffer a = BufferUtils.createFloatBuffer(4);
					Vector4f vec = Util.unproject(x, y, dist, context.getViewMatrix(), context.getProjectionMatrix(), context.totalx, context.totaly,
							context.width, context.height);
					// GLU.gluUnProject(x, y, dist, viewM, projM, viewport, a);
					res.worldLoc = new Location(vec);
					res.worldLoc = res.worldLoc.add(context.getCam().getLocation());
					if (res.picked instanceof TerrainLayerNode) {
						res.worldLoc.setLayer(((TerrainLayerNode) res.picked).layerNum);
					}
				}
			}
		}
		return res;

	}

	public static int byteToInt(byte f) {
		return f & 0xff;
	}

	public static void registerObject(int pid, RenderView context, Pickable t) {
		int r = (int) (lastInt % 256);
		int g = (int) ((lastInt >> 8) % 256);
		int b = (int) ((lastInt >> 16) % 256);
		int i = GL20.glGetUniformLocation(pid, "pickCol");
		if (i == -1) {
			return;
		}
		GL20.glUniform4f(i, r / 255f, g / 255f, b / 255f, 1f);
		allPickables.put(lastInt, t);
		lastInt++;
	}

}
