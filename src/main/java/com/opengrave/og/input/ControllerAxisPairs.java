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

import com.opengrave.og.util.Vector2f;
import com.opengrave.og.util.Vector4f;

public class ControllerAxisPairs implements ControlDescription {
	int xId, yId;
	private String label;
	private Vector4f col1, col2;

	public ControllerAxisPairs(int xId, int yId, String label, Vector4f col1, Vector4f col2) {
		this.xId = xId;
		this.yId = yId;
		this.label = label;
		this.col1 = col1;
		this.col2 = col2;
	}

	public Vector2f getValue(Controller controller, float deadzone) {
		Vector2f ret = new Vector2f();

		ret.x = controller.getComponents()[xId].getPollData();
		ret.y = controller.getComponents()[yId].getPollData();
		if (ret.length() > deadzone) {
			return ret;
		}
		return new Vector2f(0f, 0f);
	}

	public boolean hasAxis(int axis1) {
		return xId == axis1 || yId == axis1;
	}

	public int getAxis1() {
		return xId;
	}

	public int getAxis2() {
		return yId;
	}

	public float getValueWithDeadZone(Controller controller, int axis, float deadzone) {
		if (axis == xId) {
			return getValue(controller, deadzone).x;
		} else if (axis == yId) {
			return getValue(controller, deadzone).y;
		}
		return 0f;
	}

	@Override
	public int getIcon1() {
		return 4;
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
	public void putIcon1(int index) {
	}

	@Override
	public void putIcon2(int textureIndex) {
	}

}
