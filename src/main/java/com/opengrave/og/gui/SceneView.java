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
package com.opengrave.og.gui;

import com.opengrave.og.MainThread;
import com.opengrave.og.engine.RenderView;

public class SceneView extends UIElement {

	RenderView rv;

	public RenderView getRenderView() {
		return rv;
	}

	public void setRenderView(RenderView rv) {
		this.rv = rv;
	}

	public SceneView(ElementData ed) {
		super(ed);
	}

	@Override
	public void repopulateQuads() {

	}

	@Override
	public void render(int totalx, int totaly) {
		super.render(totalx, totaly);
		int totalH = MainThread.lastH;

		if (rv != null) {
			rv.render(totalx, totalH - totaly - height, width, height);
		}

	}

	@Override
	public void renderForPicking(int totalx, int totaly) {
		int totalH = MainThread.lastH;
		if (rv == null) {
			return;
		}
		rv.renderForPicking(totalx, totalH - totaly - height, width, height);
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return true;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void update(float delta) {
		if (rv == null) {
			return;
		}
		rv.update(delta);
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = height;
	}

}
