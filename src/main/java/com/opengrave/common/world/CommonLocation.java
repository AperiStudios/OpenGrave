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
package com.opengrave.common.world;

import com.opengrave.common.pathing.PathNode;
import com.opengrave.common.pathing.Point;
import com.opengrave.og.engine.Location;

public class CommonLocation {
	protected long worldx = 0, worldy = 0;
	protected float minorx = 0, minory = 0;
	protected float z = 0;
	protected int layer = -1;
	protected float angleX = 0f, angleY = 0f, angleZ = 0f;
	protected float scaleX = 1f, scaleY = 1f, scaleZ = 1f;

	public CommonLocation(CommonLocation l) {
		setLocation(l);
	}

	public CommonLocation() {
	}

	public CommonLocation(Point point) {
		this.worldx = (int) point.getX();
		this.worldy = (int) point.getY();
		this.minorx = (float) (point.getX() - worldx);
		this.minory = (float) (point.getY() - worldy);
		this.layer = point.getZ();
	}

	public void setLocation(CommonLocation l) {
		this.worldx = l.worldx;
		this.worldy = l.worldy;
		this.minorx = l.minorx;
		this.minory = l.minory;
		this.layer = l.layer;
		this.z = l.z;
		this.scaleX = l.scaleX;
		this.scaleY = l.scaleY;
		this.scaleZ = l.scaleZ;
		this.angleX = l.angleX;
		this.angleY = l.angleY;
		this.angleZ = l.angleZ;
	}

	/**
	 * World x and y must be world-based co-ordinates of the location minorx and
	 * minory must be between 0 and 1, always positive. To help with that, this
	 * function will clean up any incorrect minor values into correct values
	 */
	protected void clean() {
		while (minorx >= 1f) {
			minorx -= 1;
			worldx++;
		}
		while (minorx < 0f) {
			minorx += 1;
			worldx--;
		}
		while (minory >= 1f) {
			minory -= 1;
			worldy++;
		}
		while (minory < 0f) {
			minory += 1;
			worldy--;
		}
	}

	public CommonLocation clone() {
		CommonLocation l = new CommonLocation();
		l.minorx = minorx;
		l.minory = minory;
		l.worldx = worldx;
		l.worldy = worldy;
		l.z = z;
		l.layer = layer;
		l.scaleX = scaleX;
		l.scaleY = scaleY;
		l.scaleZ = scaleZ;
		l.angleX = angleX;
		l.angleY = angleY;
		l.angleZ = angleZ;
		return l;
	}

	public boolean equals(Object o) {
		if (o instanceof CommonLocation) {
			CommonLocation l = (CommonLocation) o;
			return l.minorx == minorx && l.minory == minory && l.worldx == worldx && l.worldy == worldy && l.z == z && l.layer == layer;
		}
		return false;
	}

	public float getXRoundUp(float size) {
		float half = size / 2f, last = 0f;
		while (minorx >= last + half) {
			last += size;
		}
		return last;
	}

	public float getYRoundUp(float size) {
		float half = size / 2f, last = 0f;
		while (minory >= last + half) {
			last += size;
		}
		return last;
	}

	public long getTileX() {
		return worldx;
	}

	public long getTileY() {
		return worldy;
	}

	public float getFullXAsFloat() {
		return worldx + minorx;
	}

	public float getFullYAsFloat() {
		return worldy + minory;
	}

	public void setLocation(long i, float f, long j, float g) {
		worldx = i;
		minorx = f;
		worldy = j;
		minory = g;
		clean();
	}

	@Override
	public String toString() {
		return "(x: " + worldx + "->" + minorx + " y: " + worldy + "->" + minory + ")";

	}

	public Location add(Location l) {
		Location next = new Location();
		next.worldx = l.worldx + worldx;
		next.worldy = l.worldy + worldy;
		next.minorx = l.minorx + minorx;
		next.minory = l.minory + minory;
		next.clean();
		return next;

	}

	public float getMinorY() {
		return minory;
	}

	public float getMinorX() {
		return minorx;
	}

	public void setFullX(float number) {
		worldx = 0;
		minorx = number;
		clean();
	}

	public void setFullY(float number) {
		worldy = 0;
		minory = number;
		clean();
	}

	public int getLayer() {
		return layer;
	}

	public CommonLocation roundUpToGrid(float size) {
		CommonLocation l2 = new CommonLocation();
		l2.setLocation(this.getTileX(), this.getXRoundUp(size), this.getTileY(), this.getYRoundUp(size));
		l2.setZ(this.getZ());
		l2.setLayer(this.getLayer());
		return l2;
	}

	public void setLayer(int layer2) {
		layer = layer2;
	}

	public void set(PathNode node) {
		this.setFullX(node.getX() / 2f);
		this.setFullY(node.getY() / 2f);
		this.layer = node.getZ();
	}

	public void setZ(float f) {
		z = f;
	}

	public float getZ() {
		return z;
	}

	public void setRotate(float xrot, float yrot, float zrot) {
		this.angleX = xrot;
		this.angleY = yrot;
		this.angleZ = zrot;
	}

	public void setScale(float xscale, float yscale, float zscale) {
		this.scaleX = xscale;
		this.scaleY = yscale;
		this.scaleZ = zscale;
	}

	public float getAngleX() {
		return angleX;
	}

	public void setAngleX(float angleX) {
		this.angleX = angleX;
	}

	public float getAngleY() {
		return angleY;
	}

	public void setAngleY(float angleY) {
		this.angleY = angleY;
	}

	public float getAngleZ() {
		return angleZ;
	}

	public void setAngleZ(float angleZ) {
		this.angleZ = angleZ;
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	public float getScaleZ() {
		return scaleZ;
	}

	public void setScaleZ(float scaleZ) {
		this.scaleZ = scaleZ;
	}

	public void setTileX(long x) {
		this.worldx = x;
	}

	public void setTileY(long y) {
		this.worldy = y;
	}
}
