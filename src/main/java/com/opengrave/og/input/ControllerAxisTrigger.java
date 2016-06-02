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

import net.java.games.input.Controller;

import com.opengrave.og.util.Vector4f;

public class ControllerAxisTrigger implements ControlDescription {
	private int axis;
	private String label;
	private Vector4f col1;
	private Vector4f col2;

	public ControllerAxisTrigger(int axis, String label, Vector4f col1, Vector4f col2) {
		this.axis = axis;
		this.label = label;
		this.col1 = col1;
		this.col2 = col2;
	}

	public boolean isPressed(Controller controller) {
		// return controller.getAxisValue(axis) > 0f;
		return controller.getComponents()[axis].getPollData() > 0f;
	}

	public int getAxis() {
		return axis;
	}

	@Override
	public int getIcon1() {
		return 1;
	}

	@Override
	public int getIcon2() {
		return 0;
	}

	@Override
	public Vector4f getColour1() {
		return col1;
	}

	@Override
	public Vector4f getColour2() {
		return col2;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void putColour1(Vector4f c) {
		col1 = c;
	}

	@Override
	public void putColour2(Vector4f c) {
		col2 = c;
	}

	@Override
	public void putLabel(String string) {
		label = string;
	}

	@Override
	public void putIcon1(int textureIndex) {
	}

	@Override
	public void putIcon2(int textureIndex) {
	}

}
