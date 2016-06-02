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

import com.opengrave.og.base.Pickable;
import com.opengrave.og.resources.Font;

public class TextButton extends Button implements TextInterface {

	TextArea ta;

	public TextButton(ElementData ed) {
		super(ed);
		ta = new TextArea(ed);
		addChildEnd(ta);

	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = height;
		ta.setSize(width, height, mwidth, mheight); // We can rely on TA to resize up to fit
		if (ta.width > this.width && ta.width < mwidth) {
			this.width = ta.width;
		}
		if (ta.height > this.height && ta.height < mheight) {
			this.height = ta.height;
		}
		if (this.height < ta.height) {
			this.height = ta.height;
			ta.y = 0;

		} else {
			int diff = this.height - ta.height;
			ta.y = diff / 2;
		}
		if (this.width < ta.width) {
			ta.x = 0;
		} else {
			int diff = this.width - ta.width;
			ta.x = diff / 2;
		}
		setChanged();
	}

	@Override
	public boolean isThis(Pickable object) {
		return object == this || object == ta;
	}

	@Override
	public boolean isFocusable() {
		return true;
	}

	@Override
	public String getString() {
		return ta.getString();
	}

	@Override
	public void setString(String s) {
		setChanged();
		ta.setString(s);
	}

	@Override
	public void setFont(Font f) {
		ta.setFont(f);
	}
}
