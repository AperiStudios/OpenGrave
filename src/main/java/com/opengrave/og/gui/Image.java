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

import com.opengrave.og.resources.Texture;
import com.opengrave.og.util.Vector4f;

public class Image extends UIParent {
	boolean pickable = false;

	public Image(ElementData ed) {
		super(ed);
	}

	private int forceSizex = -1, forceSizey = -1;

	public void setForceSize(int x, int y) {
		forceSizex = x;
		forceSizey = y;
	}

	protected Vector4f colour = new Vector4f(1f, 1f, 1f, 1f);

	public Image setColour(Vector4f col) {
		colour = col;
		return this;
	}

	public Image setTexture(Texture texture) {
		this.texture = texture;
		return this;
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = height;
		if (forceSizex > 0) {
			this.width = forceSizex;
		}
		if (forceSizey > 0) {
			this.height = forceSizey;
		}
		setChanged();
	}

	@Override
	public void repopulateQuads() {
		UIQuad q = new UIQuad().setPos(0, 0, width, height).setColour(colour).setTexture(0, 1, 0, 1, textureIndex);
		addQuad(q);
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return pickable;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

}
