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
import java.util.Collections;

public class Polygon {
	protected ArrayList<Point> points = new ArrayList<Point>();
	private ArrayList<Line> lines = new ArrayList<Line>();
	private Point center = null;
	double maxX = Double.NaN, minX = Double.NaN, maxY = Double.NaN, minY = Double.NaN;

	public Polygon() {

	}

	public Polygon(ArrayList<Point> points) {
		this.points = points;
		checkWind();
		makeBoundingBox();
		makeLines();
		setCenter();
	}

	private void checkWind() {
		int i = 0;
		for (int j = 0; j < points.size(); j++) {
			int n = j + 1;
			if (n == points.size()) {
				n = 0;
			}
			i += (points.get(j).x * points.get(n).y - points.get(n).x * points.get(j).y);
		}
		if (i > 0) {
			Collections.reverse(points);
		}
	}

	private void setCenter() {
		center = new Point(0, 0, points.get(0).z);
		for (Point point : getPoints()) {
			center.add(point);

		}
		center.divide(getPoints().size());
	}

	public void makeBoundingBox() {
		maxX = Double.NaN;
		maxY = Double.NaN;
		minX = Double.NaN;
		minY = Double.NaN;
		for (Point point : points) {
			if (Double.isNaN(minX) || point.x < minX) {
				minX = point.x;
			}
			if (Double.isNaN(maxX) || point.x > maxX) {
				maxX = point.x;
			}
			if (Double.isNaN(minY) || point.y < minY) {
				minY = point.y;
			}
			if (Double.isNaN(maxY) || point.y > maxY) {
				maxY = point.y;
			}

		}
	}

	/*
	 * public void appendPoint(PathingPoint point) { points.add(point); if
	 * (points.size() > 2 && !checkSimple()) { points.remove(points.size() - 1);
	 * // Remove the last point if it // makes it non-simple }
	 * makeBoundingBox(); makeLines(); }
	 */

	/**
	 * Check if the polygon is simple and convex
	 * 
	 * @return
	 */
	public boolean checkSimple() {
		if (points.size() < 4) {
			return true;
		}
		boolean sign = false;
		int n = points.size();
		for (int i = 0; i < n; i++) {
			Point p1 = points.get((i + 2) % n);
			Point p2 = points.get((i + 1) % n);
			Point p3 = points.get(i);
			double dx1 = p1.x - p2.x;
			double dy1 = p1.y - p2.y;
			double dx2 = p3.x - p2.x;
			double dy2 = p3.y - p2.y;
			double zcross = dx1 * dy2 - dy1 * dx2;
			if (i == 0) {
				sign = zcross > 0;
			} else {
				if (sign != (zcross > 0)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isPointInside(Point l) {
		if (points.size() == 0) {
			return false;
		}
		if (l.x < minX || l.x > maxX || l.y < minY || l.y > maxY) {
			return false;
		} // Exclude outside boundraries. Quickest exclusion
		if (points.contains(l)) {
			return true;
		} // Include vertices. Quickest inclusion
		for (Line line : lines) {
			if (line.isPointOnLine(l, 0.001)) {
				return true;
			} // Include edges. Also returns true if it's within a tolerance of 0.001 of edge
		}
		int i = 0, j = points.size() - 1; // So it's not a dead give-away. Raytrace a line to find out
		boolean c = false;
		for (i = 0; i < points.size(); j = i++) {
			Point point1 = points.get(i);
			Point point2 = points.get(j);
			if (((point1.y > l.y) != (point2.y > l.y)) && (l.x < (point2.x - point1.x) * (l.y - point1.y) / (point2.y - point1.y) + point1.x)) {
				c = !c;
			}

		}
		return c;
	}

	public void makeLines() {
		lines.clear();
		for (int i = 0; i < points.size(); i++) {
			int i2 = i - 1;
			if (i2 < 0) {
				i2 = points.size() - 1;
			}
			lines.add(new Line(points.get(i), points.get(i2), true));
		}
	}

	public ArrayList<Point> getPoints() {
		ArrayList<Point> list = new ArrayList<Point>();
		for (Point i : points) {
			list.add(i);
		}
		return list;
	}

	public ArrayList<Line> getLines() {
		return lines;
	}

	public Point getCenter() {
		return center;
	}

	public boolean hasLine(Line line) {
		return lines.contains(line);
	}
}
