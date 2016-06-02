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

public class PathingArea extends Polygon {
	int layer = 0;

	public class InsetTooSmallException extends Exception {
		private static final long serialVersionUID = 8762987646102778932L;

	}

	private HashMap<Double, Polygon> innerPolygon = new HashMap<Double, Polygon>();
	private NavigationMesh mesh;

	public PathingArea(NavigationMesh mesh, ArrayList<Point> points) {
		super(points);
		this.mesh = mesh;
	}

	public Polygon getInnerPoly(double size) {
		if (innerPolygon.containsKey(size)) {
			// return innerPolygon.get(size);
		}
		ArrayList<PathingEdge> edges = mesh.getEdges(this); // Get all real
															// edges
		ArrayList<Point> newPoints = new ArrayList<Point>();
		Point a, b = points.get(points.size() - 1), c = points.get(0);
		Polygon pgon = null;
		try {
			for (int i = 0; i <= points.size(); i++) {
				a = b;
				b = c;
				if (i < points.size() - 1) {
					c = points.get(i + 1);
				} else {
					c = points.get((i + 1) - points.size());
				}
				Line l1 = new Line(new Point(a), new Point(b), true);
				Line l2 = new Line(new Point(b), new Point(c), true);
				boolean edge1 = false, edge2 = false;
				for (PathingEdge edge : edges) {
					if (edge.contains(l1)) {
						edge1 = true;
					}
					if (edge.contains(l2)) {
						edge2 = true;
					}
				}
				insetCorner(newPoints, new Point(a), new Point(b), new Point(c), size, edge1, edge2);
			}
			pgon = new Polygon(newPoints);
			if (!pgon.checkSimple()) { // Insetting broke it
				pgon = new Polygon();
			}
		} catch (InsetTooSmallException e) {
			pgon = new Polygon();
		}
		// newPoints.add(insetCorner(new Point(b), new Point(c), new
		// Point(start), size, true,false));

		innerPolygon.put(size, pgon);
		return pgon;
	}

	/**
	 * Inset a corner with toggles to not inset either or both edges. If both
	 * edges are toggled then an extra vertex is added to avoid the corner. Not
	 * perfect but a damn sight better than previous versions
	 * 
	 * @param newPoints
	 * @param a
	 * @param b
	 * @param c
	 * @param size
	 * @param side1
	 * @param side2
	 */
	private void insetCorner(ArrayList<Point> newPoints, Point a, Point b, Point c, double size, boolean side1, boolean side2)
			throws PathingArea.InsetTooSmallException {
		Point c1 = new Point(c), a1 = new Point(a);
		double bx1 = b.x, bx2 = b.x, by1 = b.y, by2 = b.y, dx1, dy1, dist1, dx2, dy2, dist2, insetX, insetY;
		dx1 = b.x - a.x;
		dy1 = b.y - a.y;
		dist1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
		dx2 = c.x - b.x;
		dy2 = c.y - b.y;
		dist2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);
		if (dist1 < size * 2 || dist2 < size * 2) {
			side1 = false;
			side2 = false;
			// throw new PathingArea.InsetTooSmallException();
		}
		if (!side1 || (side1 && side2)) {
			insetX = dy1 / dist1 * size;
			a1.x += insetX;
			bx1 += insetX;
			insetY = -dx1 / dist1 * size;
			a1.y += insetY;
			by1 += insetY;
		}
		if (!side2 || (side1 && side2)) {
			insetX = dy2 / dist2 * size;
			c1.x += insetX;
			bx2 += insetX;
			insetY = -dx2 / dist2 * size;
			c1.y += insetY;
			by2 += insetY;
		}
		if (side1 && side2) {
			newPoints.add(intersect(a, b.x, b.y, bx2, by2, c1, b.getZ()));
			newPoints.add(intersect(a1, bx1, by1, b.x, b.y, c, b.getZ()));

			return;
		}
		if (bx1 == bx2 && by1 == by2) {
			newPoints.add(new Point(bx1, by1, b.getZ()));
		}
		Point p = intersect(a1, bx1, by1, bx2, by2, c1, b.getZ());
		if (!isPointInside(p)) {
			throw new PathingArea.InsetTooSmallException();
		}
		newPoints.add(p);
	}

	private Point intersect(Point a, double bx1, double by1, double bx2, double by2, Point c, int layer) {
		double distAB, theCos, theSin, newX, ABpos;

		if (a.x == bx1 && a.y == by1 || bx2 == c.x && by2 == c.y)
			return null;

		bx1 -= a.x;
		by1 -= a.y;
		bx2 -= a.x;
		by2 -= a.y;
		c.x -= a.x;
		c.y -= a.y;

		// Discover the length of segment A-B.
		distAB = Math.sqrt(bx1 * bx1 + by1 * by1);

		// (2) Rotate the system so that point B is on the positive X axis.
		theCos = bx1 / distAB;
		theSin = by1 / distAB;
		newX = bx2 * theCos + by2 * theSin;
		by2 = by2 * theCos - bx2 * theSin;
		bx2 = newX;
		newX = c.x * theCos + c.y * theSin;
		c.y = c.y * theCos - c.x * theSin;
		c.x = newX;

		// Fail if the lines are parallel.
		if (by1 == c.y)
			return null;

		// (3) Discover the position of the intersection point along line A-B.
		ABpos = c.x + (bx2 - c.x) * c.y / (c.y - by2);

		// (4) Apply the discovered position to line A-B in the original
		// coordinate system.
		return new Point(a.x + ABpos * theCos, a.y + ABpos * theSin, layer);
	}

	public Collection<Polygon> getAllInnerPoly() {
		return innerPolygon.values();
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}
}
