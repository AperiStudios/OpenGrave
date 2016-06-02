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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.MenuInfo;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.config.BinaryNodeException;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.config.BinarySave;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.packet.fromclient.ClientSaveFilePacket;
import com.opengrave.common.packet.fromclient.PlayerCharacterChosen;
import com.opengrave.common.packet.fromserver.PlayerAddCharacterOption;
import com.opengrave.common.packet.fromserver.PlayerCharacterSpawn;
import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.common.world.Material;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.*;
import com.opengrave.og.gui.ElementData;
import com.opengrave.og.gui.PopupMenu;
import com.opengrave.og.gui.SceneView;
import com.opengrave.og.gui.callback.PopupOptionChosen;
import com.opengrave.og.light.StaticSkyLight;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.util.Vector4f;

public class CharacterCreateState extends BaseState implements EventListener {
	GameState preparedGameState;
	ArrayList<BinaryParent> characterData = new ArrayList<BinaryParent>();
	private GameState nextState;
	private SceneView view;
	private StaticObject obj;
	FlyByCamera cam = new FlyByCamera();
	private MenuInfo menuStatic;

	public CharacterCreateState(GameState preparedGameState) {
		this.preparedGameState = preparedGameState;
	}

	@Override
	public void start() {
		GUIXML screenFile = new GUIXML("gui/loading.xml");
		screen = screenFile.getGUI();
		view = (SceneView) screen.getElementById("loadingview");

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

		EventDispatcher.addHandler(this);
		ClientSaveFilePacket packet = new ClientSaveFilePacket();
		packet.file = getSaveFile();
		menuStatic = new MenuInfo();
		menuStatic.addOptions("main", new PopupMenuOption("character-new", "none", "Create new"));
		MainThread.sendPacket(packet);
		pop();
	}

	public void pop() {
		PopupMenu pm = new PopupMenu(new ElementData());
		pm.setMenuOptions("main", 0, menuStatic, MainThread.lastW / 2, MainThread.lastH / 2, this);
		MainThread.getGameState().screen.showPopup(pm);
	}

	private BinaryParent getSaveFile() {
		File f = new File(MainThread.cache, MainThread.USERNAME + ".save");
		if (!f.isFile()) {
			return new BinaryParent();
		}
		BinarySave file;
		try (OGInputStream in = new OGInputStream(new FileInputStream(f))) {
			file = new BinarySave(in);
		} catch (FileNotFoundException e) {
			new DebugExceptionHandler(e);
			return new BinaryParent();
		} catch (IOException e1) {
			new DebugExceptionHandler(e1);
			return new BinaryParent();
		}
		return file.getRootNode();
	}

	@Override
	public void stop() {
	}

	@Override
	public void update(float delta) {
		MainThread.changeState(nextState);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onCharacterAdd(PlayerAddCharacterOption packet) {
		characterData.add(packet.characterData);
		menuStatic.removeOptions("main");
		ArrayList<PopupMenuOption> list = new ArrayList<PopupMenuOption>();
		int i = 0;
		for (BinaryParent bp : characterData) {
			try {
				list.add(new PopupMenuOption("charchoose-" + i, "none", bp.getString("name")));
			} catch (BinaryNodeException e) {
				e.printStackTrace();
			}
			menuStatic.addOptions("main", list);
			i++;
			pop();
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onMenuChoose(PopupOptionChosen event) {
		if (!isActive()) {
			return;
		}
		if (event.getReference() == this) {
			if (event.getId().startsWith("charchoose-")) {
				String[] split = event.getId().split("-");
				if (split.length != 2) {
					return;
				}
				Integer i = null;
				try {
					i = Integer.parseInt(split[1]);
				} catch (NumberFormatException nfe) {
					return;
				}
				PlayerCharacterChosen chosen = new PlayerCharacterChosen();
				chosen.choice = i;
				MainThread.sendPacket(chosen);
			} else if (event.getId().equalsIgnoreCase("cancel")) {
				MainThread.changeState(new MenuState());
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onCharacterSpawn(PlayerCharacterSpawn packet) {
		nextState = preparedGameState;
	}

}
