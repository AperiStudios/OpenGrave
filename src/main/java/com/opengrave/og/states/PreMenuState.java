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

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.common.world.Material;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.*;
import com.opengrave.og.gui.SceneView;
import com.opengrave.og.gui.TextArea;
import com.opengrave.og.input.*;
import com.opengrave.og.light.StaticSkyLight;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.util.Vector4f;

public class PreMenuState extends BaseState implements EventListener {
	Thread t;
	TextArea ta;
	SceneView view;
	FlyByCamera cam = new FlyByCamera();
	StaticObject obj;
	boolean done = false;
	private ControllerController cc;

	public PreMenuState(ControllerController cc) {
		EventDispatcher.addHandler(this);
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
		cam.setAngleVelocity(90, 0);
		cam.setViewSize(4f);
		// Image i = new Image(new ElementData(screen.getElementData()));
		// i.textureIndex = 0;
		// i.setTexture(Resources.defaultFont.texture);
		// screen.addChildEnd(i);

		ta.setString("Press any key or button to start");
		this.cc = cc;
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onKeyPress(KeyboardRawHeldEvent event) {
		if (!isActive()) {
			return;
		}
		if (done) {
			return;
		}
		if (event.isConsumed()) {
			return;
		}
		event.setConsumed();
		done = true;
		InputMain.CONTROL_WITH_GPAD = false;
		InputMain.CONTROL_WITH_MKB = true;
		MainThread.changeState(new MenuState());
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onJoystick(JoystickRawAxisEvent event) {
		if (!isActive()) {
			return;
		}
		if (done) {
			return;
		}
		if (!event.isButton()) {
			return;
		}
		if (event.getValue() < .5f) {
			return;
		}
		done = true;
		InputMain.CONTROL_WITH_GPAD = true;
		InputMain.CONTROL_WITH_MKB = true; // TODO false - for now it's needed
											// for certain menus
		InputMain.c = event.getPad();
		ControllerLayout cl = cc.getController(event.getPad().getName());
		InputMain.cl = cl;
		if (cl == null) {
			// NO PAD CONFIG. FARK
			cl = new ControllerLayout(event.getPad().getName());
			MainThread.changeState(new ControllerSetupState(cl, event.getPad()));
		} else {
			MainThread.changeState(new MenuState());
		}
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void update(float delta) {

	}

}
