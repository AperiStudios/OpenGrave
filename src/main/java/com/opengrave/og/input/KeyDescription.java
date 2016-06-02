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

public class KeyDescription implements ControlDescription {

	private String label;

	public KeyDescription(String label) {
		this.label = label;
	}

	@Override
	public int getIcon1() {
		return -1;
	}

	@Override
	public int getIcon2() {
		return 0;
	}

	@Override
	public Vector4f getColour1() {
		return new Vector4f(.8f, .8f, .8f, 1f);
	}

	@Override
	public Vector4f getColour2() {
		return new Vector4f(0f, 0f, 0f, 1f);
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void putColour1(Vector4f c) {
	}

	@Override
	public void putColour2(Vector4f c) {
	}

	@Override
	public void putLabel(String string) {
	}

	@Override
	public void putIcon1(int textureIndex) {
	}

	@Override
	public void putIcon2(int textureIndex) {
	}

}
