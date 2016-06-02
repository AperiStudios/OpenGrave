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

import java.util.ArrayList;

import com.opengrave.common.world.MovableObject;

public class PathFinderPolygonAStar implements PathFinderPolygon {

	Path path = null;
	double size = 0.4f;
	private NavigationMesh mesh;
	private PathingNode start;
	private PathingNode end;
	boolean foundEnd = false, impossible = false;
	BinaryHeap<PathingNode> openList = new BinaryHeap<PathingNode>();
	ArrayList<PathingNode> closedList = new ArrayList<PathingNode>();

	public PathFinderPolygonAStar(NavigationMesh mesh, Point end) {
		this.mesh = mesh;

		ArrayList<PathingArea> endAreas = mesh.getPolygonsAt(end);
		if (endAreas.size() == 0) {
			impossible = true;
		}
		this.end = new PathingNode(end, null, Double.NaN, 0);
	}

	@Override
	public Path getPath() {
		return path;
	}

	@Override
	public void makePath(Point start) {
		if (impossible) {
			return;
		}
		ArrayList<PathingArea> startAreas = mesh.getPolygonsAt(start);
		if (startAreas.size() == 0) {
			return;
		}
		this.start = new PathingNode(start, null, 0, start.getDistance(end.getPoint()));
		int loopcount = 0;
		openList.add(this.start);
		Path path = new Path();
		while (loopcount < 1000 && openList.size() > 0) {
			PathingNode point = openList.takeBest();
			ArrayList<PathingArea> polyList = mesh.getPolygonsAt(point.getPoint());
			if (mesh.lineOfSight(point.getPoint(), end.getPoint(), size)) {
				// This point can have a direct line to endpoint
				// Don't bail right away, check if there's any shorter distance for a few more rounds.
				if (loopcount < 990) {
					loopcount = 990;
				}
				double newScore = point.getGScore() + point.getPoint().getDistance(end.getPoint());
				if (Double.isNaN(end.gScore) || end.gScore > newScore) {
					end.parent = point;
					end.gScore = newScore;
					end.hScore = 0;
				}
				// end.parent = point;
				foundEnd = true;
				// break;

			}
			for (PathingArea mainpoly : polyList) { // if size > 0.0 then this should be singular, but let's not bet on it
				addAllPointInPoly(point, mainpoly); // Check for paths to other points of same poly
				for (PathingEdge edge : mesh.getEdges(mainpoly)) {
					PathingArea poly = edge.getNeighbour(mainpoly);
					addAllPointInPoly(point, poly);
				}
			}
			closedList.add(point);
		}
		if (foundEnd) {
			addNode(path, end);
			this.path = path;
			return;
		}
	}

	private void addNode(Path path, PathingNode node) {
		if (node.parent != null) {
			addNode(path, node.parent);
		}
		path.addPoint(node.getPoint());
	}

	public void addAllPointInPoly(PathingNode parent, PathingArea poly) {
		Polygon minigon = poly.getInnerPoly(size);
		for (Point p : minigon.getPoints()) {
			addPoint(p, parent);
		}
	}

	public void addPoint(Point point, PathingNode parent) {
		// First - check lineOfSight between points
		if (!mesh.lineOfSight(point, parent.getPoint(), size)) {
			return;
		}
		double gScore = parent.getGScore() + point.getDistance(parent.getPoint()); // Distance from start to new point, including detours
		double hScore = point.getDistance(end.getPoint()); // Direct distance to end.
		PathingNode tempNode = new PathingNode(point, parent, gScore, hScore);
		int idx = openList.indexOf(tempNode);
		if (idx != -1) { // It's in the open list
			PathingNode node = openList.get(idx);
			if (node.gScore > gScore) {
				node.parent = parent;
				node.gScore = gScore;
				openList.integrityChanged(node);
			}
			return;
		}
		idx = closedList.indexOf(tempNode);
		if (idx != -1) { // Is this redundant? We'll have to see
			PathingNode node = closedList.get(idx);
			if (node.gScore > gScore) {
				node.parent = parent;
				node.gScore = gScore;
			}
			return;
		}
		openList.add(tempNode);
	}

	@Override
	public boolean isComplete(MovableObject obj) {
		return end.getPoint().equals(new Point(obj.getLocation()));
	}
}
