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
package com.opengrave.og.terrain;

import java.util.ArrayList;

import com.opengrave.common.pathing.*;
import com.opengrave.common.world.CommonLocation;
import com.opengrave.og.Util;
import com.opengrave.og.base.*;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.Node;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix4f;

public class TerrainPolygonMesh extends Node {

	Location l = new Location();
	RenderablePoints points = new RenderablePoints();
	RenderableLines lines = new RenderableLines();
	Renderable3DStatic tris = new Renderable3DStatic();
	private PathingArea lastMouseOver = null;
	private NavigationMesh lastMesh;
	private Path lastPath;
	private TerrainWorld world;

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(l, null, null, context);
	}

	public void setFrom(NavigationMesh mesh, TerrainWorld world) {
		this.world = world;
		if (mesh == lastMesh) {
			if (!mesh.changed) {
				return;
			}
		}
		lastMesh = mesh;
		points.clearPoints();
		lines.clearLines();
		tris.clearMesh();
		for (PathingArea pgon : mesh.polygonList) {
			addPolygonMesh(pgon, 0f, 1f, 0f, 1f);
			for (Polygon minigon : pgon.getAllInnerPoly()) {
				addPolygonMesh(minigon, 1f, 1f, 0f, 1f);
			}
		}
		for (PathingEdge edge : mesh.joins) {
			for (Line line : edge.getInnerLines()) {
				addPoint(line.getPoint(0), 1f, 0f, 0f, 1f);
				addPoint(line.getPoint(1), 1f, 0f, 0f, 1f);
			}
		}
		for (Point p : mesh.pointList) {
			addPoint(p, 0f, 0f, 1f, 1f);
		}
		if (lastMouseOver != null) {
			addPolygon(lastMouseOver, 0f, 1f, 0f, 0.7f);
			for (PathingEdge edge : mesh.getEdges(lastMouseOver)) {
				addLine(edge.getLine(), 1f, 0f, 0f, 0.5f);
				addPolygon(edge.getNeighbour(lastMouseOver), 1f, 1f, 0f, 0.7f);
			}
		}
		if (lastPath != null) {
			for (Line line : lastPath.getLines()) {
				addLine(line, 0f, 0f, 1f, 1f);
			}
		}
		mesh.changed = false;
		world = null;
	}

	private float getZFor(Point p) {
		return world.getHeightAt(locationFor(p, p.getZ()));
	}

	private Location locationFor(Point p, int layer) {
		Location l = new Location();
		l.setFullX((float) p.getX());
		l.setFullY((float) p.getY());
		l.setLayer(layer);
		return l;
	}

	private void addPoint(Point p, float r, float g, float b, float a) {
		points.addVertex(new VertexPoint((float) p.getX(), (float) p.getY(), getZFor(p), r, g, b, a, 3f, 0));
	}

	private void addPolygonMesh(Polygon pgon, float f, float g, float h, float i) {
		for (Line line : pgon.getLines()) {
			addLine(line, f, g, h, i);
		}
	}

	private void addPolygon(PathingArea pgon, float r, float g, float b, float a) {
		ArrayList<Point> points = pgon.getPoints();
		for (int i = 1; i < points.size() - 1; i++) {
			addTriPoint(points.get(0), r, g, b, a, pgon.getLayer());
			addTriPoint(points.get(i), r, g, b, a, pgon.getLayer());
			addTriPoint(points.get(i + 1), r, g, b, a, pgon.getLayer());
		}
	}

	private void addTriPoint(Point point, float r, float g, float b, float a, int layer) {
		Vertex3D v = new Vertex3D();
		v.x = (float) point.getX();
		v.y = (float) point.getY();
		v.z = getZFor(point);
		// v.r = r;
		// v.g = g;
		// v.b = b;
		// v.a = a;
		tris.addVertex(v);
	}

	public void addLine(Line line, float r, float g, float b, float a) {
		Point p1 = line.getPoint(0);
		Point p2 = line.getPoint(1);
		VertexPoint vp1 = new VertexPoint((float) p1.getX(), (float) p1.getY(), getZFor(p1), r, g, b, a, 3f, 0);
		VertexPoint vp2 = new VertexPoint((float) p2.getX(), (float) p2.getY(), getZFor(p2), r, g, b, a, 3f, 0);
		lines.addVertex(vp1);
		lines.addVertex(vp2);
	}

	@Override
	public void doUpdate(float delta) {
		lines.setContext(context);
		points.setContext(context);
		tris.setContext(context);
	}

	@Override
	public void doRender(Matrix4f parent) {
		points.render(parent, RenderStyle.NORMAL);
		lines.render(parent, RenderStyle.NORMAL);
	}

	@Override
	public void doRenderShadows(Matrix4f parent, Shadow shadow) {

	}

	@Override
	public void doRenderForPicking(Matrix4f parent) {

	}

	@Override
	public void doRenderSemiTransparent(Matrix4f parent) {
		tris.render(parent, RenderStyle.NORMAL);

	}

	public void hide(boolean b) {
		points.visible = !b;
		lines.visible = !b;
		tris.visible = !b;
	}

	public void setMouseLocation(CommonLocation location) {
		if (lastMesh != null) {
			if (location == null) {
				lastMouseOver = null;
				lastMesh.changed = true;
				return;
			}
			double x = location.getTileX() + location.getMinorX();
			double y = location.getTileY() + location.getMinorY();
			Point p = new Point(x, y, location.getLayer());
			ArrayList<PathingArea> list = lastMesh.getPolygonsAt(p);
			if (list.size() > 0) {
				lastMouseOver = list.get(0);
				lastMesh.changed = true;
			} else {
				lastMouseOver = null;
			}
		}
	}

	public void addPath(Path path) {
		lastPath = path;
	}

}
