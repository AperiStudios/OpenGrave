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

import com.opengrave.og.light.Shadow;
import com.opengrave.og.light.Shadow2D;
import com.opengrave.og.util.Matrix4f;

public class RootNode extends Node {

	ArrayList<DelayedAction> delayedNodes = new ArrayList<DelayedAction>();
	private Shadow2D skyLight;

	@Override
	public void doUpdate(float delta) {
		synchronized (delayedNodes) {
			for (DelayedAction delay : delayedNodes) {
				delay.execute();
			}
		}
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

	@Override
	public Matrix4f getMatrix() {
		return new Matrix4f();
	}

	public Shadow2D getSkyLight() {
		return skyLight;
	}

	public void setSkyLight(Shadow2D skyLight) {
		this.skyLight = skyLight;
	}

	@Override
	public RootNode getRootNode() {
		return this;
	}

	public void addNode(Node node, Node node2) {
		synchronized (delayedNodes) {
			delayedNodes.add(new DelayedNodeAdd(node, node2));
		}
	}

	public void removeNode(Node node, Node node2) {
		synchronized (delayedNodes) {
			delayedNodes.add(new DelayedDelete(node, node2));
		}
	}
}
