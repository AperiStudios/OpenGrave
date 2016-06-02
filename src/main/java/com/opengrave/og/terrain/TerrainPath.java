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

import com.opengrave.common.world.CommonLocation;
import com.opengrave.common.world.CommonWorld;
import com.opengrave.og.Util;
import com.opengrave.og.base.RenderableLines;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.Node;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix4f;

/**
 * Visualised lines to show a path
 * 
 * @author triggerhapp
 * 
 */
public class TerrainPath extends Node {

	RenderableLines lines;
	Location l = new Location();

	public TerrainPath() {
		lines = new RenderableLines();
	}

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(l, null, null, context);
	}

	@Override
	public void doUpdate(float delta) {
		lines.setContext(context);
	}

	@Override
	public void doRender(Matrix4f parent) {
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
	}

	public void set(CommonLocation location, CommonLocation location2, CommonWorld cw, TerrainWorld tw) {
		// PathFinderPolygon pathFinder = new PathFinderPolygonAStar(cw, location, location2, 0);
		// pathFinder.find();
		// if (pathFinder.getPath() != null) {
		// lines.setFromPath(pathFinder.getPath(), tw);
		// }
	}

	public void hide(boolean b) {
		lines.visible = !b;
	}

}
