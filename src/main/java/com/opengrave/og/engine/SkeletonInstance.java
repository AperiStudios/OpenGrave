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

import java.util.ArrayList;

import com.opengrave.og.Util;
import com.opengrave.og.base.RenderableLines;
import com.opengrave.og.base.RenderablePoints;
import com.opengrave.og.base.VertexPoint;
import com.opengrave.og.engine.gait.Bone;
import com.opengrave.og.engine.gait.Skeleton;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public class SkeletonInstance extends Node {
	Location location = new Location();
	RenderableLines renderable;
	RenderablePoints points;
	Skeleton skele;
	private boolean visible;

	public SkeletonInstance(AnimatedObject object) {
		renderable = new RenderableLines();
		points = new RenderablePoints();
		this.skele = object.getRenderable().getSkeleton();
	}

	public SkeletonInstance(Skeleton skele) {
		renderable = new RenderableLines();
		points = new RenderablePoints();
		this.skele = skele;
	}

	public void setSkeleton(AnimatedObject object) {
		this.skele = object.getRenderable().getSkeleton();

	}

	public void setSkeleton(Skeleton skele) {
		this.skele = skele;
	}

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(location, null, null, null);
	}

	@Override
	public void doUpdate(float delta) {
		// Don't change the skele. Assume it's being controlled by an AnimatedObject
		// Also this may be called before or after the Animated Object, so change the lines in render. Not ideal
		// but this is intended only for testing, not production
		renderable.setContext(this.context);
		points.setContext(this.context);
		ArrayList<String> tex = new ArrayList<String>();
		tex.add("tex/guicross.png");
		points.setTexture(Resources.loadTextures(tex));
	}

	@Override
	public void doRender(Matrix4f parent) {
		if (!visible) {
			return;
		}
		renderable.clearLines();
		points.clearPoints();
		if (skele == null || skele.root == null) {
			return;
		}
		addBoneLines(skele.root);
		renderable.render(parent, RenderStyle.NORMAL);
		points.render(parent, RenderStyle.NORMAL);
	}

	private void addBoneLines(Bone bone) {
		if (bone == null) {
			return;
		}
		VertexPoint point1, point2;
		Vector3f vec3 = bone.getWorldPosition();
		point1 = new VertexPoint(vec3.x, vec3.y, vec3.z, 1f, 0f, 0f, 1f, 4f, 0f);
		points.addVertex(point1);
		for (Bone child : bone.getChildren()) {
			vec3 = child.getWorldPosition();
			point2 = new VertexPoint(vec3.x, vec3.y, vec3.z, 0f, 1f, 0f, 1f, 1f, 0f);
			renderable.addVertex(point1);
			renderable.addVertex(point2);

			addBoneLines(child);
		}

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

	public void setVisible(boolean checked) {
		visible = checked;
	}

	public void setLocation(Location location) {
		this.location = new Location(location);

	}

}
