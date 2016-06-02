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
import java.util.Collections;

import com.opengrave.common.MenuInfo;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.MainThread;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.PopupOptionChosen;
import com.opengrave.og.input.*;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.util.Vector4f;

public class BindingState extends BaseState implements EventListener {
	int index = 0;
	private ScrollBox sb;
	private VerticalContainer vert;
	ArrayList<ControlBinding> cbList = new ArrayList<ControlBinding>();
	boolean movingUp = false, movingDown = false;
	boolean waitingPad = false, waitingKey = false, waitingMouse = false;
	private MenuInfo bindMenu;

	public BindingState() {
		EventDispatcher.addHandler(this);
		GUIXML mainMenuFile = new GUIXML("gui/bind.xml");
		screen = mainMenuFile.getGUI();
		sb = (ScrollBox) screen.getElementById("scroll");
		vert = (VerticalContainer) screen.getElementById("list");
		bindMenu = new MenuInfo();
		bindMenu.addOptions("main", new PopupMenuOption("keyopt", "none", "Keyboard"), new PopupMenuOption("mouseopt", "none", "Mouse Button"),
				new PopupMenuOption("padopt", "none", "Gamepad"));
		populateList();
	}

	private void populateList() {
		vert.removeAllChildren();
		cbList.clear();
		synchronized (InputMain.bindings) {
			cbList.addAll(InputMain.bindings);
		}
		Collections.sort(cbList);
		vert.getElementData().minimum_width = 300;
		vert.getElementData().maximum_width = 300;
		for (ControlBinding cb : cbList) {
			ElementData ed = new ElementData(vert.getElementData());
			ed.maximum_height = 32;
			ed.minimum_height = 32;
			ed.maximum_width = 500;
			ed.minimum_width = 500;
			HorizontalContainer hz = new HorizontalContainer(ed);

			ed = new ElementData(vert.getElementData());
			ed.maximum_height = 32;
			ed.maximum_width = 256;
			TextArea ta = new TextArea(ed);
			ta.setString(cb.getControlName());
			hz.addChildEnd(ta);
			hz.drawBackground(true);
			for (InputBinding ib : cb.getList()) {
				if (ib instanceof PadBinding) {
					PadBinding pb = (PadBinding) ib;
					if (InputMain.cl == null || !pb.getPadName().equalsIgnoreCase(InputMain.cl.getName())) {
						continue;
					}
				}
				ed = new ElementData(vert.getElementData());
				ImageInput ii = new ImageInput(ed);
				ii.set(ib);
				hz.addChildEnd(ii);

			}

			vert.addChildEnd(hz);
		}
		highlight();

	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onKeyPress(KeyboardRawPressEvent event) {
		if (!isActive()) {
			return;
		}
		if (!waitingKey) {
			return;
		}
		waitingKey = false;
		ControlBinding cb = cbList.get(index);
		cb.addInput(new KeyBinding(event.getKey()));
		event.setConsumed();
		screen.closePopup();
		populateList();
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onPadPress(JoystickRawChangeEvent event) {
		// Because it's a raw, alot of helper functions will be needed. Oh well
		if (event.isConsumed() || !isActive() || !waitingPad || InputMain.cl == null) {
			return;
		}
		if (event.getState()) {
			event.setConsumed();
			ControlBinding cb = cbList.get(index);
			cb.addInput(new PadBinding(InputMain.cl.getName(), event.getAxis()));
			waitingPad = false;
			screen.closePopup();
			populateList();
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onInputCaptured(InputChangeEvent event) {
		if (!isActive() || !event.getState() || event.isConsumed()) {
			return;
		}
		if (waitingPad || waitingMouse || waitingKey) {
			return;
		}
		int inc = 0;
		if (event.getControl().getControlName().equalsIgnoreCase("move_y_positive")) {
			if (event.getRawState() < 0f) {
				inc = -1;
			} else {
				inc = 1;
			}
			event.isConsumed();
		} else if (event.getControl().getControlName().equalsIgnoreCase("move_y_negative")) {
			if (event.getRawState() < 0f) {
				inc = 1;
			} else {
				inc = -1;
			}
			event.isConsumed();
		} else if (event.getControl().getControlName().equalsIgnoreCase("menu_1")) {
			if (screen.hasPopup()) {
				return;
			}
			event.setConsumed();
			PopupMenu pm = new PopupMenu(new ElementData());
			pm.setMenuOptions("main", 0, bindMenu, MainThread.lastW / 2, MainThread.lastH / 2, this);
			screen.showPopup(pm);
			return;
		} else if (event.getControl().getControlName().equalsIgnoreCase("menu_2")) {
			if (screen.hasPopup()) {
				return;
			}
			event.setConsumed();
			ControlBinding cb = cbList.get(index);
			cb.removeInput(null);
			populateList();
			return;
		} else if (event.getControl().getControlName().equalsIgnoreCase("menu_cancel")) {
			MainThread.changeState(new MenuState());
		} else if (event.getControl().getControlName().equalsIgnoreCase("menu_next")) {
			// Save
			System.out.println("Saved bindings");
			InputMain.saveCustomBindings();
		}
		undoHighlight();
		index += inc;
		if (index < 0) {
			index = cbList.size() - 1;
		}
		if (index >= cbList.size()) {
			index = 0;
		}
		highlight();
	}

	private void highlight() {
		UIElement a = vert.getChildren().get(index);
		Vector4f ac = screen.getElementData().activeColour;
		a.getElementData().defaultColour = new Vector4f(ac.x, ac.y, ac.z, ac.w);
		sb.scrollTo(a);
	}

	private void undoHighlight() {
		UIElement a = vert.getChildren().get(index);
		Vector4f dc = screen.getElementData().defaultColour;
		a.getElementData().defaultColour = new Vector4f(dc.x, dc.y, dc.z, dc.w);
	}

	// TODO This. All of this.
	// List out all bindings from InputMain.bindings (SYNC)
	// Allow alteration and removal of InputBindings for each ControlBinding
	// Allow insertion of new ControlBinding
	// Allow insertion of InputBinding to any ControlBinding
	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void update(float delta) {
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onMenuChosen(PopupOptionChosen event) {
		if (!isActive()) {
			return;
		}
		if (event.getId().equalsIgnoreCase("keyopt")) {
			TextArea ta = new TextArea(new ElementData(screen.getElementData()));
			ta.setString("Please press a keyboard key to bind to this action");
			UIPopup popup = new UIPopup(new ElementData(screen.getElementData()));
			ta.getElementData().minimum_width = 10;
			ta.drawBackground(true);
			popup.addChildEnd(ta);
			popup.setPosition(MainThread.lastW / 2, MainThread.lastH / 2);
			screen.showPopup(popup);
			waitingKey = true;
		} else if (event.getId().equalsIgnoreCase("padopt")) {
			TextArea ta = new TextArea(new ElementData(screen.getElementData()));
			ta.setString("Press the pad button, stick or trigger to bind to this action ");
			UIPopup popup = new UIPopup(new ElementData(screen.getElementData()));
			ta.drawBackground(true);

			popup.addChildEnd(ta);
			popup.setPosition(MainThread.lastW / 2, MainThread.lastH / 2);
			screen.showPopup(popup);
			waitingPad = true;
		} else if (event.getId().equalsIgnoreCase("mouseopt")) {
			TextArea ta = new TextArea(new ElementData(screen.getElementData()));
			UIPopup popup = new UIPopup(new ElementData(screen.getElementData()));
			ta.setString("Please press the mouse button to bind to this action");
			ta.drawBackground(true);

			popup.addChildEnd(ta);
			popup.setPosition(MainThread.lastW / 2, MainThread.lastH / 2);
			screen.showPopup(popup);
			waitingMouse = true;
		}
	}

}
