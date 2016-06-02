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

import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public class BoundingBoxCamera extends Camera {

	Matrix4f ortho = new Matrix4f();
	BaseObject object;
	Location location = new Location();

	public void setObject(BaseObject obj) {
		// ortho = Util.ortho(box.getLowest().x, box.getHighest().x,
		// box.getHighest().z, box.getLowest().z, box.getLowest().y,
		// box.getHighest().y);
		this.object = obj;
	}

	@Override
	public Matrix4f getViewMatrix() {
		if (object == null) {
			return new Matrix4f();
		}
		Vector3f eye = new Vector3f(object.getBoundingBox().getHighest().x, 0f, 0f);
		Vector3f location = new Vector3f(0f, 0f, 0f);
		Vector3f up = new Vector3f(0f, 0f, 1f);
		return Matrix4f.lookAt(eye, location, up);
	}

	@Override
	public void update(float delta) {
		if (object == null) {
			return;
		}
		BoundingBox box = object.getBoundingBox();
		ortho = Matrix4f.ortho(box.getLowest().x, box.getHighest().x, box.getHighest().z, box.getLowest().z, box.getLowest().y, box.getHighest().y);

	}

	@Override
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = new Location(location);
	}

	@Override
	public Matrix4f getProjectionMatrix(int width, int height) {
		return ortho;
	}

}
