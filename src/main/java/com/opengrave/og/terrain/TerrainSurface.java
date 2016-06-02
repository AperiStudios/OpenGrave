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
package com.opengrave.og.terrain;

import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.Surface;

public class TerrainSurface implements Surface {
	int h;
	private TerrainWorld w;

	public TerrainSurface(TerrainWorld w, int h) {

		this.w = w;
		this.h = h;
	}

	@Override
	public float getHeight(double x, double y) {
		Location l = new Location();
		l.setFullX((float) x);
		l.setFullY((float) y);
		l.setLayer(h);
		return w.getHeightAt(l);
	}

}
