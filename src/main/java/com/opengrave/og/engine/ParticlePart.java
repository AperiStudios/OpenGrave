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
package com.opengrave.og.engine;

import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class ParticlePart {

	private Location location = new Location();
	private Vector3f direction = new Vector3f(0f, 0f, 0f), gravity = new Vector3f(0f, 0f, 0f);
	private Vector4f colour = new Vector4f(0f, 0f, 0f, 0f);
	private boolean shown = false;
	private float distanceFrom = 0f, scale = 32f;
	private float nextDelta = 0f;

	public void hide() {
		shown = false;
	}

	public void spawn(Location start, Vector3f direction, Vector3f gravity, Vector4f colour) {
		location = new Location(start);
		this.direction = direction;
		shown = true;
		this.gravity = gravity;
		this.colour = colour;
	}

	public void update(float delta) {
		if (!shown) {
			return;
		}
		delta = nextDelta; // Avoid fast-forwaring on lag
		float deltaMinor = delta * 0.01f;
		Vector3f changeBy = new Vector3f(gravity.x * deltaMinor, gravity.y * deltaMinor, gravity.z * deltaMinor);
		direction.add(changeBy, direction);
		Vector3f directionByTime = new Vector3f(direction.x * deltaMinor, direction.y * deltaMinor, direction.z * deltaMinor);
		location.add(directionByTime);
	}

	public float getDepth() {
		return distanceFrom;
	}

	public void setDistanceFrom(RenderView context) {
		if (!shown) {
			return;
		}
		distanceFrom = location.distanceFromEye(context);
	}

	public Location getPosition() {
		return location;
	}

	public Vector4f getColour() {
		return colour;
	}

	public Vector3f getScaleData() {
		return new Vector3f(scale, distanceFrom, 0f);
	}

	public void setNextDelta(float delta) {
		nextDelta = delta;
	}
}
