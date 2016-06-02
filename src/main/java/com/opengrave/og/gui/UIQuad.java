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

import com.opengrave.og.util.Vector4f;

public class UIQuad {
	public String meta;
	int x1, x2, y1, y2; // Float?
	float r1, r2, r3, r4, g1, g2, g3, g4, b1, b2, b3, b4, a1, a2, a3, a4; // Corner
																			// colours
	float tx1 = 0f, tx2 = 1f, ty1 = 0f, ty2 = 1f, tz = 0f;

	public UIQuad setColour(float r, float g, float b, float a) {
		r1 = r;
		r2 = r;
		r3 = r;
		r4 = r;
		g1 = g;
		g2 = g;
		g3 = g;
		g4 = g;
		b1 = b;
		b2 = b;
		b3 = b;
		b4 = b;
		a1 = a;
		a2 = a;
		a3 = a;
		a4 = a;
		return this;
	}

	public UIQuad setTexture(float tx1, float tx2, float ty1, float ty2, int index) {
		this.tx1 = tx1;
		this.ty1 = ty1;
		this.tx2 = tx2;
		this.ty2 = ty2;
		this.tz = index;
		return this;
	}

	public UIQuad setPos(int x1, int y1, int x2, int y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		return this;
	}

	public UIQuad setColour(Vector4f colour) {
		r1 = colour.x;
		r2 = colour.x;
		r3 = colour.x;
		r4 = colour.x;
		g1 = colour.y;
		g2 = colour.y;
		g3 = colour.y;
		g4 = colour.y;
		b1 = colour.z;
		b2 = colour.z;
		b3 = colour.z;
		b4 = colour.z;
		a1 = colour.w;
		a2 = colour.w;
		a3 = colour.w;
		a4 = colour.w;
		return this;
	}

	public boolean isInside(int rx, int ry) {
		return rx >= x1 && rx <= x2 && ry >= y1 && ry <= y2;
	}

}
