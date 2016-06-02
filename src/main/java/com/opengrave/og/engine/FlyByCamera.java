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

import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

/**
 * Changes should never be allowed on Camera while the render loop is running.
 * Events are run in a seperate thread and blocking the thread just for this is
 * an awful idea, so alterations to Camera are buffered until the next update()
 * 
 * @author triggerhapp
 * 
 */
public class FlyByCamera extends Camera {

	public int angle = 0;
	private int hangle = 60;
	private int nextangle = 0;
	private int nexthangle = 60;
	private double viewSize = 10, nextViewSize = 10;

	private int maxHAngle = 80;
	private int minHAngle = 60;
	private Location location = new Location(), nextLocation = new Location();

	public void update(float delta) {
		angle = nextangle;
		hangle = nexthangle;
		viewSize = nextViewSize;
		location = new Location(nextLocation);
	}

	public void setMoveVelocity(Vector3f worldDelta) {
		double angle = Math.toRadians(getAngle() + 270f);
		float angle2 = (float) Math.atan2(worldDelta.y, worldDelta.x);
		float x = (float) (Math.sin(angle + angle2) * worldDelta.length());
		float y = (float) (Math.cos(angle + angle2) * worldDelta.length());
		nextLocation.add(new Vector3f(x, y, worldDelta.z));
	}

	public void setAngleVelocity(int dx, int dy) {
		nextangle -= dx;
		nexthangle -= dy;
		if (nexthangle > maxHAngle) {
			nexthangle = maxHAngle;
		}
		if (nexthangle < minHAngle) {
			nexthangle = minHAngle;
		}
	}

	public void incrementViewSize(float f) {
		nextViewSize = nextViewSize + f;
	}

	public void capViewSize(float f, float g) {
		if (nextViewSize < f) {
			nextViewSize = f;
		}
		if (nextViewSize > g) {
			nextViewSize = g;
		}

	}

	public void setLocation(Location zoomTo) {
		nextLocation = new Location(zoomTo);
	}

	/**
	 * The location in the map the camera is POINTING TO
	 * 
	 * @return
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * The location in the map/world where the camera currently is looking FROM.
	 * 
	 * @return
	 */
	public Location getCameraLocation() {
		Location cameraLoc = new Location();
		float x, y, z;
		double hangle = Math.toRadians(getHAangle());
		double angle = Math.toRadians(getAngle());
		double scale = getViewSize();
		// Trig round 1! Using hangle and scale get xy-dist and z
		double xy = Math.cos(hangle) * scale;
		z = (float) (Math.sin(hangle) * scale);
		// Trig round 2 - Using angle and xy find x and y
		x = (float) (Math.sin(angle) * xy);
		y = (float) (Math.cos(angle) * xy);
		cameraLoc.setFullX(-x);
		cameraLoc.setFullY(-y);
		cameraLoc.setZ(-z);
		return cameraLoc.add(getLocation());
	}

	public int getHAangle() {
		return hangle;
	}

	public int getAngle() {
		return angle;
	}

	public double getViewSize() {
		return viewSize;
	}

	@Override
	public Matrix4f getViewMatrix() {
		Vector3f up = new Vector3f(0f, 0f, 1f), at = new Vector3f(0f, 0f, 0f), eye = new Vector3f();
		double hangle = Math.toRadians(getHAangle());
		double angle = Math.toRadians(getAngle());
		double scale = getViewSize();
		// Trig round 1! Using hangle and scale get xy-dist and z
		double xy = Math.cos(hangle) * scale;
		eye.z = (float) (Math.sin(hangle) * scale);
		// Trig round 2 - Using angle and xy find x and y
		eye.x = (float) (Math.sin(angle) * xy);
		eye.y = (float) (Math.cos(angle) * xy);

		return Matrix4f.lookAt(eye, at, up);
	}

	@Override
	public Matrix4f getProjectionMatrix(int width, int height) {
		float fov = 40f;
		return Matrix4f.proj(fov, width, height, 1f, 50f);
	}

	public void setHeightBounds(int min, int max) {
		this.minHAngle = min;
		this.maxHAngle = max;
	}

	public void setAngleHeight(int f) {
		nexthangle = f;
	}

	public void setViewSize(float f) {
		nextViewSize = f;
	}

}
