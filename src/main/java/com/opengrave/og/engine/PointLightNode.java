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

import java.util.ArrayList;

import com.opengrave.og.Util;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

public class PointLightNode extends Node {

	private Location location = new Location();
	private Vector4f colour = new Vector4f(1f, 1f, 1f, 1f);
	private float power = 20f;
	private double distanceSq;
	private Vector4f thisLocation;

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(location, null, null, null);
	}

	@Override
	public void doUpdate(float delta) {
	}

	@Override
	public void doRender(Matrix4f parent) {
	}

	@Override
	public void doRenderShadows(Matrix4f parent, Shadow shadow) {
	}

	@Override
	public void doRenderForPicking(Matrix4f parent) {
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f parent) {
	}

	public Vector4f getColour() {
		return colour;
	}

	public void setColour(Vector4f colour) {
		this.colour = colour;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public float getPower() {
		return power;
	}

	public void setPower(float power) {
		this.power = power;
	}

	@Override
	public void getAllLights(ArrayList<PointLightNode> lights, Matrix4f matrix, Vector4f cameraLocation) {
		lights.add(this);
		thisLocation = matrix.mult4(new Vector4f(0f, 0f, 0f, 1f), null);
		thisLocation.sub(cameraLocation, thisLocation);
		distanceSq = Math.pow(thisLocation.x, 2) + Math.pow(thisLocation.y, 2) + Math.pow(thisLocation.z, 2);
	}

	public double getDepth() {
		return distanceSq;
	}

	public Location getPosition() {
		return new Location(thisLocation);
	}

}
