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
package com.opengrave.common;

import com.opengrave.og.util.Vector4f;

public class PopupMenuOption {
	String id;
	String label;
	String icon;
	String cs;
	private Vector4f col = new Vector4f(1f, 1f, 1f, 1f);

	public PopupMenuOption(String id, String icon, String label) {
		this.id = id;
		this.icon = icon;
		this.label = label;
	}

	public PopupMenuOption(PopupMenuOption other) {
		this.id = other.id;
		this.icon = other.icon;
		this.label = other.label;
		this.cs = other.cs;
		this.col = other.col;
	}

	public PopupMenuOption() {
		this.id = "noid";
		this.icon = "none";
		this.label = "Unlabeled";
	}

	public String getId() {
		return id;
	}

	public String getIcon() {
		return icon;
	}

	public String getLabel() {
		return label;
	}

	public PopupMenuOption setColour(float r, float g, float b) {
		col = new Vector4f(r, g, b, 1f);
		return this;
	}

	public Vector4f getColour() {
		return col;
	}

	public String getControl() {
		return cs;
	}

	public void setControl(String cs) {
		this.cs = cs;
	}

	public void setIcon(String string) {
		icon = string;
	}

	public void setLabel(String string) {
		label = string;
	}

	public void setId(String id) {
		this.id = id;
	}
}
