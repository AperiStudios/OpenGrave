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

public class ControllerAxisFinder {
	public int index = -1;
	public float minVal = 0f, maxVal = 0f;

	public ControllerAxisFinder(int index) {
		this.index = index;
	}

	public void set(float value) {
		if (value < minVal) {
			minVal = value;
		}
		if (value > maxVal) {
			maxVal = value;
		}

	}

	public boolean acceptable() {
		return maxVal > 0.8f && minVal < -0.8f;
	}
}
