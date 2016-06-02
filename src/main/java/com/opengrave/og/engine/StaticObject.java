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

import org.lwjgl.opengl.GL11;

import com.opengrave.common.world.CommonObject;
import com.opengrave.og.base.Renderable3D;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Matrix4f;

public class StaticObject extends BaseObject {

	boolean transparent = false;

	public StaticObject(CommonObject cobj) {
		super(cobj);
	}

	public void setRenderable(Renderable3D rend) {
		renderable = rend;
	}

	@Override
	public void doUpdate(float delta) {

	}

	@Override
	public void renderableLabelChanged(String s) {
		renderable = Resources.getStaticModel(s).getRenderable();
	}

	@Override
	public void doRender(Matrix4f matrix) {
		if (transparent) {
			return;
		}
		renderable.setMaterialList(matList);
		renderable.setContext(context);
		renderable.render(matrix, style);
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f matrix) {
		if (!transparent) {
			return;
		}
		renderable.setMaterialList(matList);
		renderable.setContext(context);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GL11.glDepthMask(false);
		renderable.render(matrix, style);
		GL11.glDepthMask(true);
		context.prepare3DTransparent();
	}

	@Override
	public void doRenderForPicking(Matrix4f matrix) {
		renderable.setContext(context);
		renderable.renderForPicking(matrix, this);
	}

	@Override
	public void doRenderShadows(Matrix4f matrix, Shadow shadow) {
		renderable.setContext(context);
		renderable.renderShadows(matrix, shadow);
	}

	@Override
	public String getType() {
		return "static";
	}

	@Override
	public void startAnimation(String name, float speed, boolean once) {
	}

	@Override
	public void stopAnimation(String name) {
	}

	@Override
	public RenderView getContext() {
		return context;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return renderable.getBoundingBox();
	}

	public void setTransparent(boolean b) {
		transparent = b;
	}
}
