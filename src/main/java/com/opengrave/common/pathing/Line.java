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

public class Line {
	boolean strictOrder = false;
	Point p1, p2;

	public Line(Point p1, Point p2, boolean strict) {
		if (strict) {
			if (p1.x < p2.x) {
				this.p1 = p2;
				this.p2 = p1;
			} else if (p1.x == p2.x && p1.y < p2.y) {
				this.p1 = p2;
				this.p2 = p1;
			} else {
				this.p1 = p1;
				this.p2 = p2;
			}
		} else {
			this.p1 = p1;
			this.p2 = p2;
		}
		this.strictOrder = strict;
	}

	public Line(Line line) {
		this.strictOrder = line.strictOrder;
		this.p1 = new Point(line.p1);
		this.p2 = new Point(line.p2);
	}

	public boolean intersectsWith(Line line) {
		double s1_x, s1_y, s2_x, s2_y;
		s1_x = p2.x - p1.x;
		s1_y = p2.y - p1.y;
		s2_x = line.p2.x - line.p1.x;
		s2_y = line.p2.y - line.p1.y;

		double s, t;
		s = (-s1_y * (p1.x - line.p1.x) + s1_x * (p1.y - line.p1.y)) / (-s2_x * s1_y + s1_x * s2_y);
		t = (s2_x * (p1.y - line.p1.y) - s2_y * (p1.x - line.p1.x)) / (-s2_x * s1_y + s1_x * s2_y);

		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
			return true;
		}

		return false; // No collision

	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Line))
			return false;
		Line other = (Line) obj;
		if (p1 == null || p2 == null || other.p1 == null || other.p2 == null) {
			return false;
		}
		if (p1.x == other.p1.x && p2.x == other.p2.x && p1.y == other.p1.y && p2.y == other.p2.y) {
			return true;
		}
		if (p1.x == other.p2.x && p2.x == other.p1.x && p1.y == other.p2.y && p2.y == other.p1.y) {
			return true;
		}
		return false;
	}

	public boolean isPointOnLine(Point l, double epsilon) {
		if (l.x - Math.max(p1.x, p2.x) > epsilon || Math.min(p1.x, p2.x) - l.x > epsilon || l.y - Math.max(p1.y, p2.y) > epsilon
				|| Math.min(p1.y, p2.y) - l.y > epsilon) {
			return false;
		}
		if (Math.abs(p2.x - p1.x) < epsilon)
			return Math.abs(p1.x - l.x) < epsilon || Math.abs(p2.x - l.x) < epsilon;
		if (Math.abs(p2.y - p1.y) < epsilon)
			return Math.abs(p1.y - l.y) < epsilon || Math.abs(p2.y - l.y) < epsilon;

		double x = p1.x + (l.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y);
		double y = p1.y + (l.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x);

		return Math.abs(l.x - x) < epsilon || Math.abs(l.y - y) < epsilon;
	}

	public Point getPoint(int i) {
		if (i == 0) {
			return p1;
		} else {
			return p2;
		}
	}

	public Point getPoint(Point p) {
		if (p.equals(p1)) {
			return p2;
		}
		return p1;
	}

	public Line shortenEnd(double size) {
		Line line = new Line(new Point(p1), new Point(p2), strictOrder);
		double dx = line.p2.x - line.p1.x;
		double dy = line.p2.y - line.p1.y;
		double len = Math.sqrt(dx * dx + dy * dy);
		double scale = (len - size) / len;
		dx *= scale;
		dy *= scale;
		line.p2.x = line.p1.x + dx;
		line.p2.y = line.p1.y + dy;
		return line;
	}

	public Line shortenStart(double size) {
		Line line = new Line(new Point(p1), new Point(p2), strictOrder);
		double dx = line.p1.x - line.p2.x;
		double dy = line.p1.y - line.p2.y;
		double len = Math.sqrt(dx * dx + dy * dy);
		double scale = (len - size) / len;
		dx *= scale;
		dy *= scale;
		line.p1.x = line.p2.x + dx;
		line.p1.y = line.p2.y + dy;
		return line;
	}

	public double getLength() {
		return p1.getDistance(p2);
	}

	public Point getPointAtLengthFromStart(double dist) {
		Line l = new Line(this);
		l = l.shortenEnd(getLength() - dist);
		return l.getPoint(1);

	}
}
