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

import java.util.ArrayList;

import com.opengrave.og.Util;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class DayCycleSkyLight extends Shadow2D {

	float dayCycle = 0f;
	int dayLength = 24;
	float seasonCycle = 0f;
	int seasonLength = 365; // Ditto on seasons
	ArrayList<Vector4f> sunLocation = new ArrayList<Vector4f>();
	ArrayList<Vector4f> moonLocation = new ArrayList<Vector4f>();
	private float intensity;

	public DayCycleSkyLight(int size) {
		super(size);
		Vector3f skyLightDirection = new Vector3f(0f, .5f, 1f);
		Vector3f lookAt = new Vector3f(0, 0, 0);
		Vector3f up = new Vector3f(0, 1, 0);
		View = Matrix4f.lookAt(skyLightDirection, lookAt, up);
		float lowestAngle = 25f;
		float angle = lowestAngle;
		float angleincr = (180f - (lowestAngle * 2f)) / 10f;
		addSunLocation(angle, 5f);
		for (int i = 1; i < 11; i++) {
			float intensity = 6f * (1f - (Math.abs(6 - i) / 6f)) + 9f;
			addSunLocation(angle, intensity);
			angle += angleincr;
		}
		angle = 180f - lowestAngle;
		addSunLocation(angle, 5f);
		addSunLocation(angle, 5f);
		for (int i = 1; i < 11; i++) {
			addSunLocation(angle, 5f);
			angle -= angleincr;
		}
		addSunLocation(angle, 5f);

	}

	private void addSunLocation(float angle, float f) {
		double x, y;
		x = Math.cos(Util.degreesToRadians(angle));
		y = Math.sin(Util.degreesToRadians(angle));

		sunLocation.add(new Vector4f((float) x, 0.1f, (float) y, f));
	}

	@Override
	public void update(float delta) {
		dayCycle += delta * 0.001f;
		while (dayCycle >= dayLength) {
			dayCycle -= dayLength;
			seasonCycle++;
		}
		if (seasonCycle >= seasonLength) {
			seasonCycle -= seasonLength;
		}
		Vector3f skyLightDirection = getDirection();
		Vector3f lookAt = new Vector3f(0f, 0f, 0f);
		Vector3f up = new Vector3f(0, 1, 0);
		View = Matrix4f.lookAt(skyLightDirection, lookAt, up);
	}

	public Vector3f getDirection() {
		int sun1 = (int) dayCycle, sun2 = sun1 + 1;
		if (sun2 >= sunLocation.size()) {
			sun2 = 0;
		}
		Vector4f sunPos1 = sunLocation.get(sun1), sunPos2 = sunLocation.get(sun2);
		float interp = dayCycle - sun1;
		intensity = getIntensity(interp, sunPos1, sunPos2);
		return getVector(interp, sunPos1, sunPos2);
	}

	private float getIntensity(float interp, Vector4f sunPos1, Vector4f sunPos2) {
		return sunPos1.w * (1f - interp) + sunPos2.w * interp;
	}

	private Vector3f getVector(float interp, Vector4f sunPos1, Vector4f sunPos2) {
		return new Vector3f(sunPos1.x * (1f - interp) + sunPos2.x * interp, sunPos1.y * (1f - interp) + sunPos2.y * interp, sunPos1.z * (1f - interp)
				+ sunPos2.z * interp);
	}

	@Override
	public float getIntensity() {
		return intensity;
	}

}
