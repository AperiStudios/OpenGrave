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
package com.opengrave.og.engine;

import com.opengrave.common.world.CommonAreaLoc;
import com.opengrave.og.Util;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.util.Matrix4f;

public class EmptyNode extends Node {

	public EmptyNode(Location loc) {
		matrix = Util.createMatrixFor(loc, null, null, null);
	}

	public EmptyNode(CommonAreaLoc loc) {
		Location l = new Location();
		l.setTileXY(loc.getX() * 63, loc.getY() * 63);
		matrix = Util.createMatrixFor(l, null, null, null);
	}

	private Matrix4f matrix;

	@Override
	public Matrix4f getMatrix() {
		return matrix;
	}

	@Override
	public void doUpdate(float delta) {

	}

	@Override
	public void doRender(Matrix4f parent) {

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
