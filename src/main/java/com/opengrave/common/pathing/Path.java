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

import com.opengrave.common.world.CommonLocation;

public class Path {
	ArrayList<Line> linesList = new ArrayList<Line>();
	Point lastPoint = null, startPoint = null;

	public void addPoint(Point point) {
		if (lastPoint != null) {
			Line line = new Line(lastPoint, point, false);
			linesList.add(line);
		} else {
			startPoint = point;
		}
		lastPoint = point;
	}

	public ArrayList<Line> getLines() {
		return linesList;
	}

	public CommonLocation getLocation(double dist) {
		Point p = startPoint;
		for (Line l : linesList) {
			double size = l.getLength();
			p = l.getPoint(p);
			if (size > dist) {
				return new CommonLocation(l.getPointAtLengthFromStart(dist));
			} else {
				dist -= size;
			}
		}
		// Off the end!
		return new CommonLocation(p);
	}

	public double getDistanceOnPath(CommonLocation l) {
		Point point = new Point(l);
		int i = 0;
		Line line = linesList.get(0);
		double dist = 0;
		while (!line.isPointOnLine(point, 0.1)) {
			dist += line.getLength();
			i++;
			if (i < linesList.size()) {
				line = linesList.get(i);
			} else {
				return Double.NaN;
			}
		}
		return line.p1.getDistance(point) + dist;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getEndPoint() {
		Point p = startPoint;
		for (Line l : linesList) {
			p = l.getPoint(p);
		}
		return p;
	}

	public void setStartPoint(Point point) {
		this.startPoint = point;
	}

	public Line getLine(double dist) {
		Point p = startPoint;
		Line l = null;
		for (int i = 0; i < linesList.size(); i++) {
			l = linesList.get(i);
			double size = l.getLength();
			p = l.getPoint(p);
			if (size > dist) {
				return l;
			} else {
				dist -= size;
			}
		}
		// Off the end!
		return l;
	}
}
