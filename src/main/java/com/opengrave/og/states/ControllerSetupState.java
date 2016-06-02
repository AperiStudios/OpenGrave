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

import java.util.HashMap;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.MainThread;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.ElementData.PositionTypeX;
import com.opengrave.og.gui.ElementData.PositionTypeY;
import com.opengrave.og.input.*;
import com.opengrave.og.util.Vector4f;

public class ControllerSetupState extends BaseState implements EventListener {

	private ControllerLayout controllerLayout;
	TextArea ta;
	private Controller cont;
	public HashMap<Integer, ControllerAxisFinder> found;
	private int axis1;
	private int axis2;
	TextButton tb;
	private boolean sticks = true;

	public ControllerSetupState(ControllerLayout cl, Controller cont) {
		this.controllerLayout = cl;
		this.cont = cont;
		EventDispatcher.addHandler(this);
		// setScreenAll();
		setScreenFindAxis();
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onKeyboard(KeyboardRawHeldEvent event) {
		if (!isActive()) {
			return;
		}
		System.out.println(event.getKey());
		if (!sticks) {
			return;
		}
		if (event.getCharacter() == 256) {
			// Esc ape
			axis1 = -1;
			axis2 = -1;
			found = null;
			Component[] comp = cont.getComponents();
			for (int c = 0; c < cont.getComponents().length; c++) {
				Component button = comp[c];
				if (controllerLayout.hasPair(c)) {
					continue;
				}
				if (button.getIdentifier() instanceof Identifier.Axis) {
					controllerLayout.addSingle(c, button.getName(), new Vector4f(.8f, .8f, .8f, 1f), new Vector4f(1f, 1f, 1f, 1f));
				} else if (button.getIdentifier() instanceof Identifier.Key || button.getIdentifier() instanceof Identifier.Button) {
					System.out.println(button.getIdentifier().getName());
					controllerLayout.addButton(c, button.getName(), 0, 0, new Vector4f(.8f, .8f, .8f, 1f), new Vector4f(1f, 1f, 1f, 1f));
				}
			}
			setScreenAll();
		} else if (event.getCharacter() == 257) {
			// Enter
			axis1 = -1;
			axis2 = -1;
			found = null;
			setScreenFindAxis();

		}
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onJoyStickWiggle(JoystickRawAxisEvent event) {
		if (!isActive()) {
			return;
		}
		if (found == null) {
			return;
		}
		if (event.getPad().equals(cont)) {
			int i = event.getAxisIndex();
			if (!found.containsKey(i)) {
				found.put(i, new ControllerAxisFinder(event.getAxisIndex()));
			}
			found.get(i).set(event.getValue());
		}
		if (checkAxis()) {
			setScreenFoundAxis();
			System.out.println("Test");
		}

	}

	int hatCount = 1;

	private void setScreenFoundAxis() {
		screen = new UnorderedContainer(new ElementData());
		ElementData ed = new ElementData();
		ed.positionTypeX = PositionTypeX.CENTER;
		ed.positionTypeY = PositionTypeY.CENTER;
		ta = new TextArea(ed);
		screen.addChildEnd(ta);

		if (controllerLayout.hasPair(axis1, axis2)) {
			ta.setString("We already have this stick,\n Press Enter to find another stick or Escape to continue on to buttons");
		} else {
			Component[] comp = cont.getComponents();

			if (comp[axis2].getName().contains("y") && comp[axis1].getName().contains("x")) {
				controllerLayout.addPair(axis2, axis1, "" + hatCount, new Vector4f(.8f, .8f, .8f, 1f), new Vector4f(0f, 0f, 0f, 1f));
			} else {
				controllerLayout.addPair(axis1, axis2, "" + hatCount, new Vector4f(.8f, .8f, .8f, 1f), new Vector4f(0f, 0f, 0f, 1f));
			}
			hatCount++;
			ta.setString("We've found and added this stick.\nPress Enter to find another stick or Escape to continue to buttons");
		}
		axis1 = -1;
		axis2 = -1;
		found = null;
	}

	private boolean checkAxis() {
		axis1 = -1;
		axis2 = -1;
		for (ControllerAxisFinder caf : found.values()) {
			if (caf.acceptable()) {
				if (axis1 == -1) {
					axis1 = caf.index;
				} else if (axis2 == -1) {
					axis2 = caf.index;
					return true;
				}
			}
		}
		return false;
	}

	private void setScreenFindAxis() {
		axis1 = -1;
		axis2 = -1;
		screen = new UnorderedContainer(new ElementData());
		ElementData ed = new ElementData();
		ed.positionTypeX = PositionTypeX.CENTER;
		ed.positionTypeY = PositionTypeY.CENTER;

		ta = new TextArea(ed);
		ta.setString("Your pad is unknown to us,\nFirst off, we need to know which axis belong to which sticks.\nPlease wiggle only one stick, without pressing any triggers\nPress escape if there are no more sticks to configure\n("
				+ cont.getName() + ")");
		found = new HashMap<Integer, ControllerAxisFinder>();

		screen.addChildEnd(ta);
	}

	public void setScreenAll() {
		sticks = false;
		screen = new UnorderedContainer(new ElementData());
		ElementData ed = new ElementData();
		ed.positionTypeX = PositionTypeX.CENTER;
		ed.positionTypeY = PositionTypeY.CENTER;

		ta = new TextArea(ed);
		ta.setString("We've got the basics down now, Press buttons on your pad to light them up\nClick on it with your mouse to change the label, colour, etc.");
		screen.addChildEnd(ta);

		ed = new ElementData();
		ed.positionTypeY = PositionTypeY.CENTER;
		ed.maximum_height = 32;
		ed.minimum_height = 32;

		HorizontalContainer hz = new HorizontalContainer(ed);
		for (ControllerAxisPairs pair : controllerLayout.getPairs()) {
			ed = new ElementData();
			ed.minimum_height = 64;
			ed.maximum_height = 64;
			ed.minimum_width = 64;
			ed.maximum_width = 64;
			// AxisInput axis = new AxisInput(ed);
			AxisInput axis = new AxisInput(ed);
			axis.setControlInfo(cont, pair.getAxis1());
			axis.setControlInfo2(cont, pair.getAxis2());
			// axis.setCustomisation(controllerLayout);
			// axis.set(controllerLayout.getComponent(pair.getAxis1()));
			hz.addChildEnd(axis);
		}
		for (ControllerAxisTrigger trigger : controllerLayout.getTriggers()) {
			ed = new ElementData();
			ed.minimum_height = 64;
			ed.maximum_height = 64;
			ed.minimum_width = 64;
			ed.maximum_width = 64;
			// AxisInput axis = new AxisInput(ed);
			ImageInput axis = new ImageInput(ed);
			axis.setControlInfo(cont, trigger.getAxis());
			axis.setCustomisation(controllerLayout);
			axis.set(controllerLayout.getComponent(trigger.getAxis()));

			hz.addChildEnd(axis);
		}
		screen.addChildEnd(hz);

		ed = new ElementData();
		ed.positionTypeY = PositionTypeY.BOTTOM;
		ed.maximum_height = 150;

		hz = new HorizontalContainer(ed);
		for (ControllerButton button : controllerLayout.getButtons()) {
			ed = new ElementData();
			ed.minimum_height = 64;
			ed.minimum_width = 64;
			ImageInput ii = new ImageInput(ed);
			ii.setTextureIndex(1);
			ii.set(controllerLayout.getComponent(button.getIndex()));
			ii.setControlInfo(cont, button.getIndex());
			ii.setCustomisation(controllerLayout);

			hz.addChildEnd(ii);
		}
		screen.addChildEnd(hz);
		ed = new ElementData();
		ed.positionTypeX = PositionTypeX.RIGHT;
		ed.positionTypeY = PositionTypeY.BOTTOM;
		ed.maximum_height = 16;
		ed.maximum_width = 50;
		tb = new TextButton(ed);
		tb.setString("Save and Continue");
		screen.addChildEnd(tb);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onClick(MouseButtonRenderableEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.isConsumed()) {
			return;
		}
		if (tb.isThis(event.getObject())) {
			controllerLayout.save(cont.getName());
			if (InputMain.getControlIcon("move_x_positive") == null && InputMain.getControlIcon("move_y_positive") == null) {
				// For arguments sake, lets say this means no controls have been set up
				int s = controllerLayout.getButtons().size();
				ControllerAxisPairs cp = controllerLayout.getPair(0);
				if (s >= 6) {
					InputMain.getControlBinding("move_1").addInput(new PadBinding(controllerLayout.getName(), controllerLayout.getButton(0).getIndex()));
					InputMain.getControlBinding("move_2").addInput(new PadBinding(controllerLayout.getName(), controllerLayout.getButton(1).getIndex()));
					InputMain.getControlBinding("move_3").addInput(new PadBinding(controllerLayout.getName(), controllerLayout.getButton(2).getIndex()));
					InputMain.getControlBinding("move_4").addInput(new PadBinding(controllerLayout.getName(), controllerLayout.getButton(3).getIndex()));
					InputMain.getControlBinding("move_back").addInput(new PadBinding(controllerLayout.getName(), controllerLayout.getButton(4).getIndex()));
					InputMain.getControlBinding("move_next").addInput(new PadBinding(controllerLayout.getName(), controllerLayout.getButton(5).getIndex()));
				}
				if (cp == null) { // No Pairs. We're flying blind guys
					if (s >= 10) {
						// Take a pot-luck guess that we can use 4 buttons as directions
						InputMain.getControlBinding("move_x_positive").addInput(
								new PadBinding(controllerLayout.getName(), controllerLayout.getButton(6).getIndex()));
						InputMain.getControlBinding("move_x_negative").addInput(
								new PadBinding(controllerLayout.getName(), controllerLayout.getButton(7).getIndex()));
						InputMain.getControlBinding("move_y_positive").addInput(
								new PadBinding(controllerLayout.getName(), controllerLayout.getButton(8).getIndex()));
						InputMain.getControlBinding("move_y_negative").addInput(
								new PadBinding(controllerLayout.getName(), controllerLayout.getButton(9).getIndex()));

					}
				} else {
					InputMain.getControlBinding("move_x_positive").addInput(new PadBinding(controllerLayout.getName(), cp.getAxis1()));
					InputMain.getControlBinding("move_y_positive").addInput(new PadBinding(controllerLayout.getName(), cp.getAxis2()));
				}

			}
			// And either way, we now need to bind the controls. Above is only base minimal
			MainThread.changeState(new BindingState());
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
