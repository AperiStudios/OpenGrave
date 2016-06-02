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

import com.opengrave.common.world.CommonLocation;
import com.opengrave.og.Util;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

/**
 * Keep accurate track of a Location. World tiles are counted with the Long
 * variables 'worldx' and 'worldy'. 'minorx' and 'minory' are Floats which must
 * always be between 0 (inclusive) and 1 (exclusive) An exact location is always
 * worldx + minorx, worldy + minory - This way we have a uniform, accurate
 * location which does not lose track of the smaller increments that could be
 * passed to it.
 * 
 * These can also be used as relative to area start.
 * 
 * @author triggerhapp
 * 
 */
public class Location extends CommonLocation {

	public Location(Vector3f v) {
		this.minorx = v.x;
		this.minory = v.y;
		this.z = v.z;
		this.clean();
	}

	public Location() {
	}

	public Location(long x, long y, float z) {
		this.worldx = x;
		this.worldy = y;
		this.z = z;
		clean();
	}

	public Location(Vector4f v) {
		this.minorx = v.x;
		this.minory = v.y;
		this.z = v.z;
		this.clean();
	}

	public Location(CommonLocation l) {
		minorx = l.getMinorX();
		minory = l.getMinorY();
		worldx = l.getTileX();
		worldy = l.getTileY();
		z = l.getZ();
		layer = l.getLayer();
		scaleX = l.getScaleX();
		scaleY = l.getScaleY();
		scaleZ = l.getScaleZ();
		angleX = l.getAngleX();
		angleY = l.getAngleY();
		angleZ = l.getAngleZ();
	}

	public void add(Vector3f delta) {
		minorx += delta.x;
		minory += delta.y;
		z += delta.z;
		clean();
	}

	public Vector3f toVector3() {
		return new Vector3f(getFullXAsFloat(), getFullYAsFloat(), getZ());
	}

	public Vector3f minus(Location loc) {
		long xdiff = getTileX() - loc.getTileX(), ydiff = getTileY() - loc.getTileY();
		float xmdiff = minorx - loc.minorx, ymdiff = minory - loc.minory;
		float h = z - loc.z;

		Location tmp = new Location();
		tmp.setLocation(xdiff, xmdiff, ydiff, ymdiff);
		tmp.setZ(h);
		tmp.clean();
		return tmp.toVector3();
	}

	public Location addTogether(Vector3f v) {
		Location next = new Location();
		next.minorx = this.minorx + v.x;
		next.minory = this.minory + v.y;
		next.z = this.z + v.z;
		next.clean();
		return next;
	}

	public Vector3f getRotate() {
		return new Vector3f(angleX, angleY, angleZ);
	}

	public Vector3f getScale() {
		return new Vector3f(scaleX, scaleY, scaleZ);
	}

	private Vector4f Vector4f(Vector3f v3, float w) {
		return new Vector4f(v3.x, v3.y, v3.z, w);
	}

	public Vector4f toVector4() {
		return new Vector4f(getFullXAsFloat(), getFullYAsFloat(), getZ(), 1f);
	}

	public float distanceFromEye(RenderView context) {
		Location camLoc = context.getCam().getLocation();
		Matrix4f proj = context.getProjectionMatrix();
		Vector4f loc = Vector4f(this.minus(camLoc), 1f);
		Vector4f cameraSpace = proj.mult4(loc, null);
		return cameraSpace.z;
	}

	public Matrix4f getMatrix(RenderView context) {
		return Util.createMatrixFor(this, null, null, context);
	}

	public void setRotate(Vector3f rot) {
		angleX = rot.x;
		angleY = rot.y;
		angleZ = rot.z;
	}

	public void setScale(Vector3f scale) {
		scaleX = scale.x;
		scaleY = scale.y;
		scaleZ = scale.z;
	}

	public void setTileXY(int x, int y) {
		this.worldx = x;
		this.worldy = y;
	}

}
