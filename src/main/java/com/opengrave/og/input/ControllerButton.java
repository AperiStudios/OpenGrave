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
package com.opengrave.og.input;

import com.opengrave.og.util.Vector4f;

public class ControllerButton implements ControlDescription {
	private String label;
	private int icon1;
	private int icon2;
	private Vector4f col1;
	private Vector4f col2;
	private int index;

	public ControllerButton(int buttonId, String label, int icon1, int icon2, Vector4f col1, Vector4f col2) {
		this.index = buttonId;
		this.label = label;
		this.icon1 = icon1;
		this.icon2 = icon2;
		this.col1 = col1;
		this.col2 = col2;
	}

	public int getIndex() {
		return index;
	}

	public int getIcon1() {
		return icon1;
	}

	public int getIcon2() {
		return icon2;
	}

	public String getLabel() {
		return label;
	}

	public Vector4f getColour1() {
		return col1;
	}

	public Vector4f getColour2() {
		return col2;
	}

	public void putColour1(Vector4f colour) {
		col1 = colour;
	}

	public void putColour2(Vector4f colour) {
		col2 = colour;
	}

	public void putIcon1(int index) {
		icon1 = index;
	}

	public void putIcon2(int index) {
		icon2 = index;
	}

	public void putLabel(String string) {
		label = string;

	}
}
