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
package com.opengrave.og.light;

import java.util.ArrayList;

import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.RenderView;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public class CubeData {

	public CubeData(int face, Vector3f look, Vector3f up) {
		this.face = face;
		this.look = look;
		this.up = up;
	}

	public static ArrayList<CubeData> data = new ArrayList<CubeData>();
	public int face;
	public Vector3f look;
	public Vector3f up;

	/*
	 * Create a 45 degree FOV view out of the light with given light position
	 * Creates two Matrices, one for the projection (Should this be stored?)
	 * second for the "lookat" which decides the direction the light faces
	 */
	public Matrix4f getMatrix(Location lightpos, RenderView context) {
		Vector3f loc = lightpos.toVector3();

		Matrix4f proj = Matrix4f.proj(90, MainThread.SHADOWSIZE, MainThread.SHADOWSIZE, 1f, 50f);

		Matrix4f look = Util.lookDir(loc, this.look, this.up);
		return proj.mult(look, null);
	}
}
