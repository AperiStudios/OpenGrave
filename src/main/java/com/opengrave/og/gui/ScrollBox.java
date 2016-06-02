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
package com.opengrave.og.gui;

import org.lwjgl.opengl.GL11;

import com.opengrave.og.MainThread;
import com.opengrave.og.util.Vector4f;

public class ScrollBox extends UIParent {
	int scrolly;

	public ScrollBox(ElementData ed) {
		super(ed);
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = height;
		synchronized (children) {
			children.get(0).setSize(width, height, -1, -1);// No max size
		}
	}

	@Override
	public void repopulateQuads() {
		UIQuad q = new UIQuad().setPos(0, 0, width, height).setColour(1f, 1f, 1f, 0.7f);
		addQuad(q);
	}

	/**
	 * Special cases Ho!
	 */
	@Override
	public void render(int totalx, int totaly) {
		synchronized (children) {
			UIElement e = children.get(0);

			// GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			// GL11.glEnable(GL11.GL_DEPTH_TEST);
			// GL11.glDepthFunc(GL11.GL_LESS);
			// Render box at location
			// location2d = new Vector3f(totalx, totaly, 0);
			// render(null, RenderStyle.NORMAL);
			// GL11.glDepthFunc(GL11.GL_EQUAL);
			// Render contents minus scroll ammount
			if (width < 0 || height < 0) {
				return;
			}
			GL11.glScissor(totalx, MainThread.lastH - (totaly + height), width, height);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			e.render(totalx, (int) (totaly - scrolly));
			// GL11.glDepthFunc(GL11.GL_LESS);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);

		}
	}

	public void renderForPicking(int totalx, int totaly) {
		synchronized (children) {
			UIElement e = children.get(0);

			// GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			// GL11.glEnable(GL11.GL_DEPTH_TEST);
			// GL11.glDepthFunc(GL11.GL_LESS);
			// Render box at location
			// location2d = new Vector3f(totalx, totaly, 0);
			// render(null, RenderStyle.NORMAL);
			// GL11.glDepthFunc(GL11.GL_EQUAL);
			// Render contents minus scroll ammount
			if (width < 0 || height < 0) {
				return;
			}
			GL11.glScissor(x, MainThread.lastH - (totaly + height), width, height);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);

			e.renderForPicking(totalx, (int) (totaly - scrolly));
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
		}
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return true;
	}

	public void scrollTo(UIElement a) {
		if (this.containsChildSomewhere(a)) { // contained somewhere in here.
			scrollTo(getPos(a));
		}
	}

	public void scrollTo(Vector4f vec) {
		int w = (int) (vec.z - vec.x);
		int h = (int) (vec.w - vec.y);
		if (w > width) {
			// scrollx = vec.x
		}
		if (h > height) {
			scrolly = (int) vec.y;
		} else {
			if (scrolly < vec.y) {
				if (scrolly + height > vec.w) {
					return;
				} else {
					scrolly = (int) (vec.w - height);
				}
			} else {
				scrolly = (int) vec.y;
			}
		}
		synchronized (children) {
			UIElement c = children.get(0);
			if (scrolly > c.height - height) {
				scrolly = c.height - height;
			}
			if (scrolly < 0) {
				scrolly = 0;
			}
		}

	}

	public Vector4f getPos(UIElement ele) {
		Vector4f vec = new Vector4f(0f, 0f, 0f, 0f);
		UIParent p = ele.parent;
		UIElement e = ele;
		while (p != this) {
			vec.x = vec.x + e.x;
			vec.y = vec.y + e.y;
			e = p;
			p = p.parent;
		}
		vec.z = vec.x + ele.width;
		vec.w = vec.y + ele.height;
		return vec;
	}

}
