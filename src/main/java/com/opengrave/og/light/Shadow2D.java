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

import com.opengrave.og.engine.RenderView;
import com.opengrave.og.resources.Framebuffer;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public abstract class Shadow2D implements Shadow {

	ArrayList<Vector3f> lightDirection = new ArrayList<Vector3f>();
	Depth2DFramebuffer framebuffer;
	Matrix4f projection = new Matrix4f();
	Matrix4f View = new Matrix4f();

	public Shadow2D(int size) {
		framebuffer = new Depth2DFramebuffer(size);
		float left = 15f;
		float right = -15f;
		float bottom = -15f;
		float top = 15f;
		float near = -20f;
		float far = 80f;
		projection = Matrix4f.ortho(left, right, top, bottom, near, far);
		Vector3f skyLightDirection = new Vector3f(0f, .5f, 1f);
		Vector3f lookAt = new Vector3f(0, 0, 0);
		Vector3f up = new Vector3f(0, 0, 1);
		View = Matrix4f.lookAt(skyLightDirection, lookAt, up);
		// lightDirection.add(new Vector3f());
	}

	@Override
	public Matrix4f getMVP(Matrix4f Model, RenderView context) {
		// return projection * View * model;
		// Why can this not be so simple to read?
		Matrix4f MVP = new Matrix4f();
		projection.mult(View, MVP);
		MVP.mult(Model, MVP);
		return MVP;
	}

	public Framebuffer getFramebuffer() {
		return framebuffer;
	}

	public abstract void update(float delta);

	public abstract float getIntensity();

	public abstract Vector3f getDirection();

}
