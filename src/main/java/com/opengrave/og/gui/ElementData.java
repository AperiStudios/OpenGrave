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

import java.util.HashMap;

import com.opengrave.og.util.Vector4f;

public class ElementData {

	public HashMap<String, String> attributes = new HashMap<String, String>();

	public enum PositionTypeX {
		FIXED, LEFT, CENTER, RIGHT
	}

	public enum PositionTypeY {
		FIXED, TOP, CENTER, BOTTOM
	}

	public String id = "";
	public int x = 0, y = 0;
	public int minimum_width = -1, minimum_height = -1;
	public int maximum_width = -1, maximum_height = -1;
	public Vector4f defaultColour = new Vector4f(0f, 0f, 0f, 1f), activeColour = new Vector4f(0f, 0f, 0f, 1f), disabledColour = new Vector4f(0f, 0f, 0f, 1f),
			textColour = new Vector4f(0f, 0f, 0f, 1f);

	public PositionTypeX positionTypeX = PositionTypeX.FIXED;
	public PositionTypeY positionTypeY = PositionTypeY.FIXED;
	public boolean hidden = false;

	public ElementData() {

	}

	public ElementData(ElementData ed) {
		this.activeColour = ed.activeColour;
		this.defaultColour = ed.defaultColour;
		this.disabledColour = ed.disabledColour;
	}

}
