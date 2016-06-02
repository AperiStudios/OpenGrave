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
package com.opengrave.og.base;

import java.util.ArrayList;

import com.opengrave.og.engine.RenderView;
import com.opengrave.og.gui.CheckButton;
import com.opengrave.og.gui.ImageInput;
import com.opengrave.og.gui.ImageInputInner;
import com.opengrave.og.gui.PopupMenuBit;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.resources.ShaderProgram;
import com.opengrave.og.util.Matrix4f;

public abstract class Renderable {

	static ShaderProgram shader;
	// protected Location location = new Location();
	// public Vector3f location = new Vector3f(0,0,0);
	protected boolean changed = true;
	protected RenderView context;

	public static void init() {
		shader = Resources.loadShader("picking.vs", "picking.fs");
		Renderable2D.init();
		Renderable3D.init();
		RenderableMultitex.init();
		RenderableBoneAnimated.init();
		// Checkbox tick and cross
		ArrayList<String> textures = new ArrayList<String>();
		textures.add("blank");
		textures.add("tex/guitick.png");
		textures.add("tex/guicross.png");
		CheckButton.textureStatic = Resources.loadTextures(textures);
		// Popup menu card background
		textures = new ArrayList<String>();
		textures.add("none");
		textures.add("tex/guicard.png");
		PopupMenuBit.textureStatic = Resources.loadTextures(textures);
		// Icon shapes for gamepad buttons
		textures = new ArrayList<String>();
		textures.add("tex/buttonround.png");
		textures.add("tex/buttontrigger.png");
		textures.add("tex/buttondpad.png");
		textures.add("tex/buttonroundlong.png");
		textures.add("tex/buttonstick.png");
		ImageInput.textureStatic = Resources.loadTextures(textures);
		// Icon inner shapes for gamepad buttons
		textures = new ArrayList<String>();
		textures.add("none");
		textures.add("tex/buttonicon1.png");
		textures.add("tex/buttonicon2.png");
		textures.add("tex/buttonicon3.png");
		textures.add("tex/buttonicon4.png");
		textures.add("tex/buttonicon5.png");
		ImageInputInner.textureStatic = Resources.loadTextures(textures);
	}

	// public void setLocation(Location l){
	// location = l;
	// }
	// public Location getLocation(){
	// return location;
	// }

	public void setChanged() {
		changed = true;
	}

	public abstract void render(Matrix4f matrix, RenderStyle style);

	public abstract void renderForPicking(Matrix4f matrix, Pickable object);

	public abstract void renderShadows(Matrix4f matrix, Shadow shadow);

	public void setContext(RenderView context) {
		this.context = context;
	}

	public RenderView getContext() {
		return context;
	}

	public abstract void dealWithChange();

	public abstract void recreate();

	public abstract void addVertex(VertexData vd);

	public abstract void update(float delta);

	public abstract void delete();
}
