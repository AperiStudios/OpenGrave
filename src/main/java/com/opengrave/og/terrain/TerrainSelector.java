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

import com.opengrave.og.Util;
import com.opengrave.og.base.RenderablePoints;
import com.opengrave.og.base.VertexPoint;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.Node;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix4f;

public class TerrainSelector extends Node {

	RenderablePoints points = new RenderablePoints();

	public float gridSize = 1f;
	public Location location = new Location();

	public TerrainSelector() {
		points.addVertex(new VertexPoint(0f, 0f, 0f, 1f, 1f, 1f, 1f, 1f, 0f));
	}

	public void setLocation(Location l) {
		this.location = l;
	}

	public void setGridSize(float size) {
		this.gridSize = size;
	}

	@Override
	public Matrix4f getMatrix() {
		Location l2 = new Location(location.roundUpToGrid(gridSize));
		return Util.createMatrixFor(l2, null, null, context);
	}

	@Override
	public void doUpdate(float delta) {
		points.setContext(context);
	}

	@Override
	public void doRender(Matrix4f parent) {
		points.render(parent, RenderStyle.NORMAL);
	}

	@Override
	public void doRenderShadows(Matrix4f parent, Shadow shadow) {

	}

	@Override
	public void doRenderForPicking(Matrix4f parent) {

	}

	@Override
	public void doRenderSemiTransparent(Matrix4f parent) {

	}
}
