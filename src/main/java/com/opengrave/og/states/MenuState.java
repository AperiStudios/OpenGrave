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

import com.opengrave.common.MenuInfo;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.ServerData;
import com.opengrave.common.event.*;
import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.common.world.Material;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.*;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.gui.callback.PopupOptionChosen;
import com.opengrave.og.light.StaticSkyLight;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.states.waitables.ExplorerLoader;
import com.opengrave.og.states.waitables.GameLoader;
import com.opengrave.og.util.Vector4f;

public class MenuState extends BaseState implements EventListener {

	Image loginStatusImage;
	BaseText loginStatusText;
	Button buttonPlay, buttonTerrainEdit, buttonSettings, buttonQuit, buttonModelTest, buttonJoin, buttonCrash, buttonIK, buttonExp;
	private MenuInfo menuStatic;
	private SceneView view;
	private TextArea ta;
	private StaticObject obj;
	FlyByCamera cam = new FlyByCamera();

	@Override
	public void start() {
		MainThread.main.checkInNow();
		// Automatically update status
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
		cam.setViewSize(4f);
		cam.setAngleVelocity(90, 0);
		menuStatic = new MenuInfo();
		menuStatic.addOptions("main", new PopupMenuOption("start", "none", "Start").setColour(0f, 1f, 0f),
				new PopupMenuOption("join", "none", "Join Friend").setColour(0f, .3f, 1f), new PopupMenuOption("settings", "none", "Settings"),
				new PopupMenuOption("explorer", "none", "Mod Explorer"));
		menuStatic.setCancel(MenuInfo.Cancel.Last, "Exit", new Vector4f(1f, 0f, 0f, 1f));
		// screen = mainMenuFile.getGUI();
		/*
		 * loginStatusImage = (Image) screen.getElementById("loginstatusimage");
		 * loginStatusText = (BaseText) screen.getElementById("loginstatustext");
		 * buttonPlay = (Button) screen.getElementById("startbutton");
		 * buttonTerrainEdit = (Button) screen.getElementById("terrainbutton");
		 * buttonSettings = (Button) screen.getElementById("settingbutton");
		 * buttonQuit = (Button) screen.getElementById("exitbutton");
		 * buttonModelTest = (Button) screen.getElementById("modelbutton");
		 * buttonIK = (Button) screen.getElementById("ikbutton");
		 * buttonJoin = (Button) screen.getElementById("joinbutton");
		 * buttonCrash = (Button) screen.getElementById("crashbutton");
		 * buttonExp = (Button) screen.getElementById("explorerbutton");
		 * if (!HGMainThread.config.getBoolean("debugmenu", false)) {
		 * buttonModelTest.hide(true);
		 * buttonTerrainEdit.hide(true);
		 * buttonCrash.hide(true);
		 * buttonExp.hide(true);
		 * }
		 */
		PopupMenu pm = new PopupMenu(new ElementData());
		pm.setMenuOptions("main", 0, menuStatic, MainThread.lastW / 2, MainThread.lastH / 2, this);
		MainThread.getGameState().screen.showPopup(pm);
	}

	@SuppressWarnings("null")
	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButtonPress(ButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		Button b = event.getButton();
		if (b == buttonPlay) {
			MainThread.changeServerConnection(new ServerData());
			MainThread.changeState(new LoadingState(new GameLoader(ProfileState.state.mods)));
		} else if (b == buttonJoin) {
			// HGMainThread.changeServerConnection("aperistudios.co.uk", 4242);
			MainThread.changeState(new ServerListState());
		} else if (b == buttonTerrainEdit) {
			MainThread.changeState(new TEditState());
		} else if (b == buttonModelTest) {
			MainThread.changeState(new ModelState());
		} else if (b == buttonSettings) {
			MainThread.changeState(new SettingState());
		} else if (b == buttonQuit) {
			MainThread.running = false;
		} else if (b == buttonCrash) {
			// Intentionally bad code. This will not get caught in the easiest way, but is enough to prove bug catching works correctly (Unexpected exceptions especially)
			Object a = null;
			a.getClass();
		} else if (b == buttonIK) {
			MainThread.changeState(new IKState());
		} else if (b == buttonExp) {
			MainThread.changeState(new LoadingState(new ExplorerLoader(ProfileState.state.mods)));
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void clientAuthStatusChanged(ClientAuthStatusEvent event) {
		if (!isActive()) {
			return;
		}
		Vector4f loginColour = new Vector4f();
		if (ProfileState.state.isOnline) {

			loginStatusText.setString("Online Mode");
		} else {
			loginColour = new Vector4f(1f, 0f, 0f, 1f);
			loginStatusText.setString("Offline Mode");
		}
		if (ProfileState.state.standing == ProfileState.Standing.UNKNOWN) {
			loginColour = new Vector4f(1f, 0.4f, 0f, 1f);
		} else if (ProfileState.state.standing == ProfileState.Standing.GOOD) {
			loginColour = new Vector4f(0f, 1f, 0f, 1f);
		} else if (ProfileState.state.standing == ProfileState.Standing.CHEATER) {
			loginColour = new Vector4f(1f, 0f, 0f, 1f);
		}
		loginStatusImage.setColour(loginColour);

	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onMenuChosen(PopupOptionChosen event) {
		if (!isActive()) {
			return;
		}
		if (event.getReference() == this) {
			String id = event.getId();
			if (id.equalsIgnoreCase("cancel")) {
				MainThread.running = false;
			} else if (id.equalsIgnoreCase("start")) {
				MainThread.changeServerConnection(new ServerData());
				MainThread.changeState(new LoadingState(new GameLoader(ProfileState.state.mods)));
			} else if (id.equalsIgnoreCase("join")) {
				MainThread.changeState(new ServerListState());
			} else if (id.equalsIgnoreCase("settings")) {
				MainThread.changeState(new SettingState());
			} else if (id.equalsIgnoreCase("explorer")) {
				MainThread.changeState(new LoadingState(new ExplorerLoader(ProfileState.state.mods)));
			}
		}
	}

	@Override
	public void stop() {
	}

	@Override
	public void update(float delta) {

	}
}
