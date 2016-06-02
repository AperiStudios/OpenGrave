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

public class ColourButton extends Button {

	Image i;

	public ColourButton(ElementData ed) {
		super(ed);
		i = new Image(ed);
		addChildEnd(i);
	}

	public ColourButton setColour(Vector4f c) {
		i.setColour(c);
		setChanged();
		return this;
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = height;
		i.setLocation(2, 2);
		i.setSize(width - 4, height - 4, width - 4, height - 4);
		setChanged();
	}

	public ColourButton setTexture(Texture tex) {
		i.setTexture(tex);
		return this;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

}
