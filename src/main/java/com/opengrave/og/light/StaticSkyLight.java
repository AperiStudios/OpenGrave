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
package com.opengrave.og.light;

import com.opengrave.og.util.Vector3f;

public class StaticSkyLight extends Shadow2D {

	public StaticSkyLight(int size) {
		super(size);
	}

	@Override
	public void update(float delta) {
		// View = Matrix4f.rotate(delta * 0.001f, new Vector3f(0f, 0f, 1f), View, null);
	}

	@Override
	public float getIntensity() {
		return 15f;
	}

	@Override
	public Vector3f getDirection() {
		return new Vector3f(1f, 0f, 0f);
	}

}
