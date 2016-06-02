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
package com.opengrave.common.pathing;

import com.opengrave.common.world.CommonLocation;
import com.opengrave.og.util.Vector4f;

public class Point {

	double x, y;
	int z;

	public Point() {
	}

	public Point(double x, double y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}

	public Point(CommonLocation location) {
		this.x = location.getTileX() + location.getMinorX();
		this.y = location.getTileY() + location.getMinorY();
		this.z = location.getLayer();
	}

	public Point(Vector4f v) {
		this.x = v.x;
		this.y = v.y;
		this.z = (int) v.z; // TODO take object floor instead of vector loc
	}

	public double getY() {
		return y;
	}

	public double getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public void add(Point point) {
		x += point.x;
		y += point.y;
	}

	public void divide(int size) {
		x /= size;
		y /= size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Point))
			return false;
		Point other = (Point) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	public boolean isPointNear(Point p, double dist) {
		if (getDistance(p) < dist) {
			return true;
		}
		return false;
	}

	public double getDistance(Point other) {
		return Math.sqrt(Math.pow(Math.abs(x - other.x), 2) + Math.pow(Math.abs(y - other.y), 2));
	}

	public double getAngle(Point point) {
		double xd = x - point.x;
		double yd = y - point.y;
		return Math.atan2(yd, xd);
	}

	public Point minus(Point point) {
		Point p = new Point();
		p.x = this.x - point.x;
		p.y = this.y - point.y;
		p.z = this.z - point.z;
		return p;
	}

	@Override
	public String toString() {
		return "( x : " + x + " y : " + y + " layer : " + z + " )";
	}

}
