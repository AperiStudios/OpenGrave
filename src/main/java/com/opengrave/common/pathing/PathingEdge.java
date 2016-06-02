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
import java.util.Collection;
import java.util.HashMap;

public class PathingEdge {
	private Line line;
	private double cost;
	private ArrayList<PathingArea> polygons = new ArrayList<PathingArea>();
	private HashMap<Double, Line> innerLines = new HashMap<Double, Line>();

	public PathingEdge(Line line, double cost, PathingArea p1, PathingArea p2) {
		this.cost = cost;
		this.line = line;
		polygons.add(p1);
		polygons.add(p2);
	}

	public PathingArea getNeighbour(PathingArea pgon) {
		for (PathingArea poly : polygons) {
			if (poly.equals(pgon)) {
				continue;
			}
			return poly;
		}
		return null;
	}

	public Line getLine() {
		return line;
	}

	public double getCost() {
		return cost;
	}

	public boolean contains(PathingArea poly) {
		return polygons.contains(poly);
	}

	public boolean contains(Line line) {
		return line.equals(this.line);
	}

	public void setLine(Line line) {
		this.line = line;
	}

	public Collection<Line> getInnerLines() {
		return innerLines.values();
	}

	public Line getInnerLine(double size) {
		if (innerLines.containsKey(size)) {
			return innerLines.get(size);
		}
		Line l = new Line(line);
		l = l.shortenEnd(size);
		l = l.shortenStart(size);
		innerLines.put(size, l);
		return l;
	}
}
