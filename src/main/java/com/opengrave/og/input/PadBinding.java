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

public class PadBinding extends InputBinding {
	private int index;
	private String pad;

	public PadBinding(String pad, int index) {
		this.pad = pad;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "{ Pad : " + pad + " : " + index + " }";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PadBinding) {
			PadBinding pad = (PadBinding) obj;
			if (!pad.pad.equals(this.pad)) {
				return false;
			}
			if (pad.index != index) {
				return false;
			}
			return true;
		}
		return false;
	}

	public String getPadName() {
		return pad;
	}
}
