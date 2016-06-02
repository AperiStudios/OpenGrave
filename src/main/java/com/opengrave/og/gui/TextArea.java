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

import java.util.ArrayList;

public class TextArea extends BaseText implements TextInterface {

	public TextArea(ElementData ed) {
		super(ed);
	}

	ArrayList<String> strings = new ArrayList<String>();

	public void addText(String line) {
		changed = true;
		if (line.contains("\n")) {
			return;
		}
		// TODO Customisable size
		if (strings.size() > 200) {
			strings.remove(0);
		}
		strings.add(line);
		setStrings();
	}

	public void clearText() {
		changed = true;
		strings.clear();
		setStrings();
	}

	public void setStrings() {
		StringBuilder sb = new StringBuilder();
		for (String s : strings) {
			sb.append("\n").append(s);
		}
		setString(sb.toString());
	}

	// verride
	// public boolean onClick(int x, int y, int relative_x, int relative_y,
	// int button, boolean state) {
	// return false;
	// }

	@Override
	public void update(float delta) {

	}

	/**
	 * Ignore incomming.
	 */
	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		// this.width = width;
		// this.height = height;
		// int myHeight = (strings.size() + 1) * getFont().fontheight;
		if (getFont() == null) {
			return;
		}
		this.height = (strings.size() + 1) * getFont().fontheight;
		this.width = getTextMinWidth();
		// if (height < myHeight) {
		// this.height = myHeight;
		// }
		setChanged();
	}

	@Override
	public boolean isFocusable() {
		return true;
	}

}
