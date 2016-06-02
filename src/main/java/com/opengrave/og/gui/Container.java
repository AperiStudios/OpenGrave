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

public abstract class Container extends UIParent {
	boolean drawBack = false;

	public Container(ElementData ed) {
		super(ed);
	}

	public void drawBackground(boolean b) {
		drawBack = b;
	}

	@Override
	public void repopulateQuads() {
		if (drawBack) {
			addQuad(new UIQuad().setColour(ed.defaultColour).setPos(0, 0, width, height));
		}
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return false;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}
}
