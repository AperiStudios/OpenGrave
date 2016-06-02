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

public class PathingNode implements Comparable<PathingNode> {
	PathingNode parent = null;
	Point point;
	double gScore = Double.NaN, hScore = Double.NaN;

	public PathingNode(Point point, PathingNode parent, double gScore, double hScore) {
		this.parent = parent;
		this.point = point;
		this.gScore = gScore;
		this.hScore = hScore;
	}

	public double getScore() {
		return gScore + hScore;
	}

	@Override
	public int compareTo(PathingNode arg) {
		if (this.getScore() == arg.getScore()) {
			return 0;
		} else if (this.getScore() > arg.getScore()) {
			return 1;
		} else {
			return -1;
		}
	}

	public Point getPoint() {
		return point;
	}

	public double getGScore() {
		return gScore;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PathingNode))
			return false;
		PathingNode other = (PathingNode) obj;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		return true;
	}

}
