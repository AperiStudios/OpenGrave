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
package com.opengrave.og.states;

import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.common.world.Material;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.*;
import com.opengrave.og.gui.SceneView;
import com.opengrave.og.gui.TextArea;
import com.opengrave.og.light.StaticSkyLight;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.states.waitables.Loader;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class LoadingState extends BaseState {

	Thread t;
	TextArea ta;
	private Loader loader;
	SceneView view;
	FlyByCamera cam = new FlyByCamera();
	StaticObject obj;

	public LoadingState(Loader loader) {
		this.loader = loader;
	}

	@Override
	public void start() {
		screen = new GUIXML("gui/loading.xml").getGUI();
		view = (SceneView) screen.getElementById("loadingview");
		ta = (TextArea) screen.getElementById("message");

		RootNode node = new RootNode();
		MaterialList matList = new MaterialList();
		matList.addMaterial(new Material("tex/hglogo.png", "tex/coarse.png", new Vector4f()));
		CommonObject cobj = new CommonObject("", Type.Static, "mod/logo.dae:logo,logo2", matList, new Location());
		obj = (StaticObject) BaseObject.createObject(cobj);
		obj.setLocation(new Location());

		// obj.setMaterialList(matList);
		obj.setTransparent(true);
		obj.drawOutline = false;
		node.addChild(obj);
		RenderView rv = new RenderView(node, cam);
		rv.clearAreaBeforeDraw(true);
		node.setSkyLight(new StaticSkyLight(MainThread.config.getInteger("shadowSize", 1024)));
		view.setRenderView(rv);
		cam.setHeightBounds(0, 180);
		cam.setAngleHeight(10);
		cam.setViewSize(4f);

		loader.start();
		t = new Thread(loader, "Loading Thread " + loader);
		t.start();
	}

	@Override
	public void stop() {
	}

	@Override
	public void update(float delta) {
		// cam.setAngleVelocity(1, 0);
		Vector3f a = obj.getAngles();
		a.z += delta * 0.1f;
		obj.setAngle(a);
		if (loader.isDone()) {
			loader.finish();
		}
		synchronized (this) {
			ta.setString(loader.getStatusMessage());
		}
	}
}
