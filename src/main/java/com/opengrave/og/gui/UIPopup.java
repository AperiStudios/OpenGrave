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

import com.opengrave.og.MainThread;

public class UIPopup extends Popup {

	private int localx;
	private int localy;

	public UIPopup(ElementData ed) {
		super(ed);
	}

	public void setPosition(int x, int y) {
		localx = x;
		localy = y;
	}

	@Override
	public void repopulateQuads() {
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return false;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		// Assume one child, as a container or a single UI item
		synchronized (children) {
			if (children.size() == 0) {
				return;
			}
			UIElement c = children.get(0);
			c.setSize(width, height, mwidth, mheight);
			this.width = c.width;
			this.height = c.height;
			int tx = localx - (c.width / 2);
			int ty = localy - (c.height / 2);
			if (tx + c.width > MainThread.lastW) {
				tx = MainThread.lastW - c.width;
			}
			if (ty + c.height > MainThread.lastH) {
				ty = MainThread.lastH - c.height;
			}
			if (tx < 0) {
				tx = 0;
			}
			if (ty < 0) {
				ty = 0;
			}
			c.x = tx;
			c.y = ty;
		}
	}

}
