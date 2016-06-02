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

public class PathNode implements Comparable<PathNode> {

	private PathNode parent;
	private int x;
	private int y;
	private int z;

	private int gScore, hScore;
	private PathFinder pathFinder;

	public PathNode(PathNode parent, int x, int y, int z, PathFinder pathFinder, int cost) {
		this.parent = parent;
		this.pathFinder = pathFinder;
		this.x = x;
		this.y = y;
		this.z = z;
		if (parent == null) {
			this.gScore = cost;
		} else {
			this.gScore = cost + parent.gScore;
		}
		this.hScore = getHeuristic();
	}

	public int getHeuristic() {
		return pathFinder.getHeuristic(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathNode other = (PathNode) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	public int getScore() {
		return gScore + hScore;
	}

	@Override
	public int compareTo(PathNode arg) {
		return this.getScore() - arg.getScore();
	}

	public PathNode getParent() {
		return parent;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public void set(PathNode node) {
		if (this.pathFinder != node.pathFinder) {
			return;
		}
		this.gScore = node.gScore;
		this.hScore = node.hScore;
		this.parent = node.parent;
		this.x = node.x;
		this.y = node.y;
		this.z = node.z;
	}

}
