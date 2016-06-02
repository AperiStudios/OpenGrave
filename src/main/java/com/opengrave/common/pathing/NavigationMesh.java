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

public class NavigationMesh {

	// What a mesh.
	public ArrayList<Point> pointList = new ArrayList<Point>();
	public ArrayList<PathingArea> polygonList = new ArrayList<PathingArea>();
	public ArrayList<PathingEdge> joins = new ArrayList<PathingEdge>();
	public boolean changed = false;
	public boolean delayEdges = false;

	public NavigationMesh() {
	}

	public ArrayList<PathingEdge> getEdges(Line line) {
		ArrayList<PathingEdge> list = new ArrayList<PathingEdge>();
		for (PathingEdge edge : joins) {
			if (edge.contains(line)) {
				list.add(edge);
			}
		}
		return list;
	}

	public ArrayList<PathingEdge> getEdges(PathingArea poly) {
		ArrayList<PathingEdge> list = new ArrayList<PathingEdge>();
		for (PathingEdge edge : joins) {
			if (edge.contains(poly)) {
				list.add(edge);
			}
		}
		return list;
	}

	public ArrayList<PathingArea> getPolygonsAt(Point p) {
		ArrayList<PathingArea> list = new ArrayList<PathingArea>();
		for (PathingArea poly : polygonList) {
			if (poly.isPointInside(p)) {
				list.add(poly);
			}
		}
		return list;
	}

	public boolean lineOfSight(Point point, Point end, double size) {
		ArrayList<PathingArea> polys = getPolygonsAt(point);
		if (polys.size() <= 0) {
			return false;
		}
		if (lineOfSight(polys.get(0), null, point, end, size)) {
			return true;
		}
		// if DERPY MODE ACTIVATE. Much less CPU intensive. Great unless there's a completely perfect LOS across more than 1 poly
		// for(PathingArea area : polys){
		// if(area.isPointInside(end)){ return true; }
		// }
		return false;
	}

	public boolean lineOfSight(PathingArea pgon, Line exclude, Point start, Point end, double size) {
		if (pgon.isPointInside(end)) {
			return true;
		}
		Line lineOfSight = new Line(start, end, true);
		for (PathingEdge edge : getEdges(pgon)) {
			if (edge.getLine().equals(exclude)) {
				continue;
			}
			if (edge.getInnerLine(size).intersectsWith(lineOfSight)) {
				if (lineOfSight(edge.getNeighbour(pgon), edge.getLine(), start, end, size)) {
					return true;
				}
			}
		}
		// for (Line line : keepLines) {
		// Line miniLine = line.shortenEnd(size).shortenStart(size);
		// if (line.intersectsWith(lineOfSight)) {
		// for (PathingEdge edge : getEdges(line)) {
		// if (lineOfSight(edge.getNeighbour(pgon), line, start, end, size)) { return true; }
		// }
		// }
		// }
		return false;
	}

	public Point getPoint(int idx) {
		if (idx < pointList.size()) {
			return pointList.get(idx);
		}
		return null;
	}

	public void addPoint(Point pathingPoint) {
		if (pointList.contains(pathingPoint)) {
			return;
		}
		pointList.add(pathingPoint);
		changed = true;
	}

	public Point getNearestPoint(Point inPoint) {
		double distance = Double.MAX_VALUE;
		Point ret = null;
		for (int i = 0; i < pointList.size(); i++) {
			Point point = pointList.get(i);
			double nDist = point.getDistance(inPoint);
			if (nDist < distance) {
				ret = point;
				distance = nDist;
			}
		}
		return ret;
	}

	public void addPoly(ArrayList<Point> nextPoly) {
		PathingArea poly = new PathingArea(this, nextPoly);
		if (!poly.checkSimple()) {
			System.out.println("Given Polygon was not convex");
			return;
		}
		if (polygonList.contains(poly)) {
			return;
		}
		polygonList.add(poly);
		if (!delayEdges) {
			fixEdges();
		}
		changed = true;
	}

	public void fixEdges() {
		joins.clear();
		for (int i = 0; i < polygonList.size(); i++) {
			PathingArea poly = polygonList.get(i);
			ArrayList<Line> lines = poly.getLines();
			for (int j = i + 1; j < polygonList.size(); j++) {
				PathingArea poly2 = polygonList.get(j);
				PathingEdge edge = new PathingEdge(null, poly.getCenter().getDistance(poly2.getCenter()), poly, poly2);
				for (Line line : lines) {
					if (poly2.hasLine(line)) {
						if (edge.getLine() == null) {
							edge.setLine(line);
						} else {
							System.err.println("Cannot have two shared lines between polygonal areas");
						}
					}
				}
				if (edge.getLine() != null) {
					joins.add(edge);
				}
			}
		}
	}
}
