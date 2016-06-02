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

import java.util.ArrayList;
import java.util.UUID;

import com.opengrave.common.CharacterData;
import com.opengrave.common.event.*;
import com.opengrave.common.packet.fromclient.SendChatPacket;
import com.opengrave.common.packet.fromserver.*;
import com.opengrave.common.pathing.Line;
import com.opengrave.common.world.CommonObject;
import com.opengrave.og.MainThread;
import com.opengrave.og.ServerConnection;
import com.opengrave.og.engine.*;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.gui.callback.TextInputEvent;
import com.opengrave.og.input.InputHeldEvent;
import com.opengrave.og.input.MouseButtonRenderableEvent;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.resources.ShaderProgram;
import com.opengrave.og.terrain.TerrainWorld;
import com.opengrave.og.util.Vector3f;

public class GameState extends BaseState implements EventListener {

	private TerrainWorld tw;
	TextInput i;
	StaticObject model;
	ParticleObject part;
	ShaderProgram test;
	SceneView view;
	FlyByCamera cam;
	RootNode rootNode;
	public ObjectStorageNode objects;
	Button quit;

	ArrayList<TerrainWorld> worldList = new ArrayList<TerrainWorld>();

	public GameState() {
		prestart(); // Activate it early to allow packets to be grabbed
		EventDispatcher.addHandler(this);
		rootNode = new RootNode();
		cam = new FlyByCamera();
		objects = new ObjectStorageNode();
		rootNode.addChild(objects);
	}

	@Override
	public void start() {
		MainThread.main.checkInNow();
		GUIXML screenFile = new GUIXML("gui/game.xml");
		screen = screenFile.getGUI();
		quit = (Button) screen.getElementById("quit");
		view = (SceneView) screen.getElementById("gameview");
		view.setRenderView(new RenderView(rootNode, cam));
	}

	@Override
	public void stop() {
		MainThread.getConnection().killConnection();
		EventDispatcher.dispatchEvent(new ModUnloadAllEvent());
		ServerConnection sc = MainThread.getConnection();
		if (sc != null) {
			sc.stopServer();
		}
		screen.delete();
		screen = null;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onInputCaptured(InputHeldEvent event) {
		if (!isActive()) {
			return;
		}
		float ammount = event.getDelta() * event.getMagnitude() * 0.05f;
		if (event.getControl().getControlName().equalsIgnoreCase("move_y_positive")) {
			cam.setMoveVelocity(new Vector3f(0f, ammount, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("move_y_negative")) {
			cam.setMoveVelocity(new Vector3f(0f, -ammount, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("move_x_negative")) {
			cam.setMoveVelocity(new Vector3f(-ammount, 0f, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("move_x_positive")) {
			cam.setMoveVelocity(new Vector3f(ammount, 0f, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("look_x_positive")) {
			cam.setAngleVelocity((int) (ammount * 5), 0);
		} else if (event.getControl().getControlName().equalsIgnoreCase("look_x_negative")) {
			cam.setAngleVelocity((int) -(ammount * 5), 0);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButtonPress(ButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getButton().equals(quit)) {
			MainThread.changeState(new MenuState());
		}
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onButtonRenderable(MouseButtonRenderableEvent event) {
		if (!isActive()) {
			return;
		}
		System.out.println(event.getObject());
		if (event.getLocation() == null) {
			return;
		}
		if (event.getObject() instanceof BaseObject) {
			PopupMenu pm = new PopupMenu(new ElementData());
			// pm.setMenuOptions("none", 0, ((BaseObject) event.getObject()).getUUID(), event.getRX(), event.getRY());
			pm.setMenuOptions("none", 0, ((BaseObject) event.getObject()).getMenuInfo(), event.getRX(), event.getRY(), event.getObject());
			this.screen.showPopup(pm);
		}

		// TODO: Care about orders/tasks more
		// GatherOrder order = new GatherOrder();
		// order.setLocation(new CommonLocation(event.getLocation()));
		// NewOrderPacket packet = new NewOrderPacket();
		// packet.order = order;
		// HGMainThread.sendPacket(packet);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onWorldLoad(LoadWorldPacket packet) {
		if (!isActive()) {
			return;
		}
		final String worldName = packet.getWorldName();
		System.out.println("Loading World : " + worldName);
		if (!isWorldLoaded(worldName)) {
			MainThread.addToGLCommands(new Runnable() {

				@Override
				public void run() {
					TerrainWorld world = new TerrainWorld(worldName);
					synchronized (worldList) {
						worldList.add(world);
					}
					if (tw == null) {
						tw = world;
						rootNode.addChild(tw);
					}
				}

			});

		}
	}

	private boolean isWorldLoaded(String worldName) {

		synchronized (worldList) {
			for (TerrainWorld world : worldList) {
				if (world.getFileName().equals(worldName)) {
					return true;
				}
			}
		}
		return false;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onTextInput(TextInputEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getInput().equals(i)) {
			if (event.isEndLine()) {
				SendChatPacket cp = new SendChatPacket();
				cp.origin = "";
				cp.message = event.getInput().getString();
				MainThread.sendPacket(cp);
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onObjectSpawn(ObjectsSpawnPacket packet) {
		if (!isActive()) {
			return;
		}
		for (CommonObject obj : packet.obj) {
			System.out.println("Spawning object " + obj.getUUID());
			BaseObject bObj = objects.createObject(obj);
			bObj.startAnimation("idle", 1f, false);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onObjectPathSet(ObjectPathSetPacket packet) {
		if (!isActive()) {
			return;
		}
		BaseObject obj = objects.getObject(packet.uuid);
		System.out.println("Setting Path to object " + packet.uuid);
		obj.setPath(packet.path);
		obj.tick(0, 0); // New path, assume we're at the start of it
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onObjectPathProgress(ObjectMovementOnPathPacket packet) {
		if (!isActive()) {
			return;
		}
		BaseObject obj = objects.getObject(packet.uuid);
		if (obj.getPath() == null) {
			return;
		}
		System.out.println("Moving object " + packet.uuid);
		// TODO Animated to this over time.
		// obj.setLocation();
		double distNow = obj.getPath().getDistanceOnPath(obj.getLocation());
		Line line = obj.getPath().getLine(distNow);
		int i = line.getPoint(0).getZ();
		obj.setSurface(tw.getSurface(i));
		if (Double.isNaN(distNow)) {
			obj.tick(packet.progress, packet.progress);
		} else {
			obj.tick(distNow, packet.progress);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onObjectLook(ObjectLookTowardsPacket packet) {
		if (!isActive()) {
			return;
		}
		BaseObject obj = objects.getObject(packet.id);
		obj.lookAt(packet.point);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onObjectDespawn(ObjectsDespawnPacket packet) {
		if (!isActive()) {
			return;
		}
		for (UUID uuid : packet.idList) {
			System.out.println("Despawning object " + uuid);
			objects.removeObject(uuid);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onObjectAnimated(ObjectAnimationSetPacket packet) {
		if (!isActive()) {
			return;
		}
		BaseObject co = objects.getObject(packet.uuid);
		if (co == null) {
			return;
		}
		// TODO Animation speed, ticks etc.
		co.startAnimation(packet.animName, 1f, true);

	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onObjectNewOptions(ObjectReplaceOptionsPacket packet) {
		if (!isActive()) {
			return;
		}
		BaseObject obj = objects.getObject(packet.uuid);
		if (obj == null) {
			return;
		}
		obj.getCommonObject().setMenuInfo(packet.mi);
	}

	@Override
	public void update(float delta) {
	}

	public ArrayList<CharacterData> getCharacters() {
		return null;
	}
}
