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

import org.lwjgl.opengl.GL11;

import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

public abstract class Node {

	protected Node parent = null;
	protected ArrayList<Node> children = new ArrayList<Node>();
	protected RenderView context;

	public boolean hasChild(Node node) {
		if (node == this) {
			return true;
		}
		for (Node node2 : children) {
			if (node2.hasChild(node)) {
				return true;
			}
		}
		return false;
	}

	public void renderOne(Matrix4f matrix, Node node, Matrix4f scale) {
		if (node == this) {
			Matrix4f scaledMatrix = matrix.mult(scale, null);
			doRender(scaledMatrix);
		}
		for (Node child : children) {
			if (child.hasChild(node)) {
				// Save calcs
				Matrix4f childMatrix = matrix.mult(child.getMatrix(), null);
				child.renderOne(childMatrix, node, scale);
			}
		}
	}

	public void render(Matrix4f matrix) {
		doRender(matrix);
		for (Node node : children) {
			Matrix4f childMatrix = matrix.mult(node.getMatrix(), null);
			node.render(childMatrix);
		}
	}

	public void renderShadows(Matrix4f matrix, Shadow shadow) {
		doRenderShadows(matrix, shadow);
		for (Node node : children) {
			Matrix4f childMatrix = matrix.mult(node.getMatrix(), null);
			node.renderShadows(childMatrix, shadow);
		}
	}

	public void renderForPicking(Matrix4f matrix) {
		doRenderForPicking(matrix);
		for (Node node : children) {
			Matrix4f childMatrix = matrix.mult(node.getMatrix(), null);
			node.renderForPicking(childMatrix);
		}
	}

	public void renderSemiTransparent(Matrix4f matrix) {
		Util.checkErr();
		doRenderSemiTransparent(matrix);
		if (MainThread.main.input.getLastHovered() != null) {
			Pickable lr = MainThread.main.input.getLastHovered().getRenderable();
			if (this instanceof BaseObject && lr instanceof BaseObject) {
				if (lr == this) {
					BaseObject notThis = (BaseObject) this;
					if (notThis.drawOutline) {
						// Setup for outline draw
						GL11.glDepthFunc(GL11.GL_LESS);
						GL11.glEnable(GL11.GL_DEPTH_TEST);
						GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_LINE);
						GL11.glLineWidth(10f);
						GL11.glCullFace(GL11.GL_FRONT);
						GL11.glEnable(GL11.GL_CULL_FACE);

						GL11.glEnable(GL11.GL_BLEND);
						GL11.glEnable(GL11.GL_LINE_SMOOTH);
						GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
						GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

						// Draw

						RenderStyle rs = notThis.getRenderStyle();
						notThis.setRenderStyle(RenderStyle.HALO);

						doRender(matrix);

						notThis.setRenderStyle(rs);

						// Return to correct state
						GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);
						GL11.glLineWidth(1f);
						GL11.glCullFace(GL11.GL_BACK);
						GL11.glDisable(GL11.GL_CULL_FACE);
						GL11.glEnable(GL11.GL_BLEND);
					}
				}
			}
		}
		Util.checkErr();
		for (Node node : children) {
			Matrix4f childMatrix = matrix.mult(node.getMatrix(), null);
			node.renderSemiTransparent(childMatrix);
			Util.checkErr();
		}
	}

	public void update(RenderView context, float delta) {
		this.context = context;
		doUpdate(delta);
		for (Node node : children) {
			node.update(context, delta);
		}
	}

	public void getAllLights(ArrayList<PointLightNode> lights, Matrix4f matrix, Vector4f cameraLocation) {
		for (Node node : children) {
			Matrix4f childMatrix = matrix.mult(node.getMatrix(), null);
			node.getAllLights(lights, childMatrix, cameraLocation);
		}
	}

	public abstract Matrix4f getMatrix();

	public abstract void doUpdate(float delta);

	public abstract void doRender(Matrix4f parent);

	public abstract void doRenderShadows(Matrix4f parent, Shadow shadow);

	public abstract void doRenderForPicking(Matrix4f parent);

	public abstract void doRenderSemiTransparent(Matrix4f parent);

	public void addChild(Node node) {
		node.parent = this;
		getRootNode().addNode(this, node);
	}

	public void removeChild(Node node) {
		getRootNode().removeNode(this, node);
	}

	public RootNode getRootNode() {
		if (parent != null) {
			return parent.getRootNode();
		}
		return null;
	}
}
