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

import com.opengrave.common.OGOutputStream;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.engine.Node;
import com.opengrave.og.engine.RenderView;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.util.Matrix4f;

public class TerrainLiquidLayerNode extends Node implements Pickable {

	public TerrainLiquidLayer layer;

	public TerrainLiquidLayerNode(TerrainLiquidLayer layer) {
		this.layer = layer;
	}

	@Override
	public RenderView getContext() {
		return context;
	}

	@Override
	public void doUpdate(float delta) {
		layer.setContext(context);
		layer.update(delta);
	}

	@Override
	public void doRender(Matrix4f parent) {
	}

	@Override
	public void doRenderShadows(Matrix4f parent, Shadow shadow) {
		layer.renderShadows(parent, shadow);
	}

	@Override
	public void doRenderForPicking(Matrix4f parent) {
		layer.renderForPicking(parent, this);
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f parent) {
		layer.render(parent);
	}

	public void save(OGOutputStream stream) {
		layer.save(stream);
	}

	@Override
	public Matrix4f getMatrix() {
		return new Matrix4f();
	}
}
