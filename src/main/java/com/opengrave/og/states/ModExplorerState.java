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
import java.util.HashMap;

import com.opengrave.common.ModSession;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.inventory.ItemMaterial;
import com.opengrave.common.inventory.ItemType;
import com.opengrave.common.inventory.ItemTypePart;
import com.opengrave.common.inventory.ItemTypeParts;
import com.opengrave.og.MainThread;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.CheckButtonPressedEvent;
import com.opengrave.og.gui.callback.StringRollerChangedEvent;
import com.opengrave.og.gui.callback.TextSelectedEvent;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.util.Vector4f;

public class ModExplorerState extends BaseState implements EventListener {
	public static enum View {
		MAT, ITEM, NPC, PC, ITEMPART
	};

	HashMap<CheckButton, String> buttons = new HashMap<CheckButton, String>();

	StringRoller roller;
	Scrollable choices, contents;

	private View currentView;

	@Override
	public void start() {
		EventDispatcher.addHandler(this);
		GUIXML mainMenuFile = new GUIXML("gui/explorer.xml");
		screen = mainMenuFile.getGUI();
		roller = (StringRoller) screen.getElementById("chooser");
		choices = (Scrollable) screen.getElementById("choices");
		contents = (Scrollable) screen.getElementById("contents");

		setViewing(View.MAT);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onViewSwitch(StringRollerChangedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getRoller().equals(roller)) {
			String s = event.getRoller().getString();
			if (s.equals("Show Materials")) {
				setViewing(View.MAT);
			} else if (s.equals("Show Items")) {
				setViewing(View.ITEM);
			} else if (s.equals("Show Classes")) {
				setViewing(View.NPC);
			} else if (s.equals("Show Player Classes")) {
				setViewing(View.PC);
			} else if (s.equals("Show Item Parts")) {
				setViewing(View.ITEMPART);
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButton(CheckButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		if (buttons.containsKey(event.getButton())) {
			fillContents(currentView, event.getButton().getString());
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onLink(TextSelectedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.link == null) {
			return;
		}
		String[] split = event.link.split(":", 2);
		if (split.length < 2) {
			return;
		}
		if (split[0].equalsIgnoreCase("ItemPart")) {
			roller.setStringIndex(4);
			fillContents(View.ITEMPART, split[1]);
		}
	}

	private void fillContents(View currentView, String string) {
		ModSession sess = MainThread.getSession();
		contents.removeAllChildren();
		TextArea ta = new TextArea(new ElementData(contents.getElementData()));
		contents.addChildEnd(ta);
		ta.setString("No data : " + currentView + " " + string);
		StringBuilder sb = new StringBuilder();
		if (currentView.equals(View.ITEM)) {
			ItemType i = MainThread.getSession().getItemType(string);
			sb.append(i.getItemName()).append(" : ").append(string).append("\n");
			sb.append("\n");
			sb.append("Mod : ").append(i.getModName()).append("\n");
			if (i instanceof ItemTypeParts) {
				sb.append("Type : Item made of Parts\n");
				sb.append("\n");
				sb.append("Parts:\n");
				ItemTypeParts ip = (ItemTypeParts) i;
				HashMap<String, ArrayList<ItemTypePart>> parts = ip.getPartsLists();
				for (String key : parts.keySet()) {
					sb.append(key).append(" : ");
					for (ItemTypePart part : parts.get(key)) {
						sb.append("#00f@<ItemPart:").append(i.getID()).append(":").append(key).append(":").append(part.getName()).append(">")
								.append(part.getName()).append("@#000, ");
					}
					sb.append("\n");
				}

			} else {
				sb.append("Type : Simple Item\n");
			}

		} else if (currentView.equals(View.MAT)) {
		} else if (currentView.equals(View.NPC)) {
		} else if (currentView.equals(View.PC)) {
		} else if (currentView.equals(View.ITEMPART)) {
			for (ItemType it : sess.getItemTypes()) {
				if (it instanceof ItemTypeParts) {

					ItemTypeParts itp = (ItemTypeParts) it;
					for (String position : itp.getPartsLists().keySet()) {
						for (ItemTypePart part : itp.getPartsLists().get(position)) {
							String s = it.getID() + ":" + position + ":" + part.getName();
							if (string.equals(s)) {
								// We have it!
								part.fill(sb);
							}
						}
					}
				}
			}
		}
		ta.setString(sb.toString());
	}

	public void setViewing(View view) {
		contents.removeAllChildren();
		choices.removeAllChildren();
		currentView = view;
		ModSession sess = MainThread.getSession();
		// Re-populate choices
		if (view.equals(View.MAT)) {
			ElementData ed = new ElementData(choices.getElementData());
			CheckButtonGroupVertical group = new CheckButtonGroupVertical(ed);
			choices.addChildEnd(group);
			for (ItemMaterial iM : sess.getMaterials()) {
				ElementData edB = new ElementData(ed);
				float r = iM.getR() / 255f, g = iM.getG() / 255f, b = iM.getB() / 255f;
				edB.defaultColour = new Vector4f(r, g, b, 1f);
				edB.activeColour = edB.defaultColour;
				edB.textColour = new Vector4f(1f - r, 1f - g, 1f - b, 1f);
				edB.maximum_height = 24;

				CheckButton button = new CheckButton(edB);
				button.setString(iM.getName());

				group.addChildEnd(button);

				buttons.put(button, iM.getName());
			}
		} else if (view.equals(View.ITEM)) {
			ElementData ed = new ElementData(choices.getElementData());
			CheckButtonGroupVertical group = new CheckButtonGroupVertical(ed);
			choices.addChildEnd(group);
			for (ItemType it : sess.getItemTypes()) {
				ElementData edB = new ElementData(ed);
				edB.maximum_height = 24;
				CheckButton button = new CheckButton(edB);
				button.setString(it.getID());
				group.addChildEnd(button);

				buttons.put(button, it.getID());
			}
		} else if (view.equals(View.ITEMPART)) {
			ElementData ed = new ElementData(choices.getElementData());
			CheckButtonGroupVertical group = new CheckButtonGroupVertical(ed);
			choices.addChildEnd(group);
			ElementData edB = new ElementData(ed);
			edB.maximum_height = 24;

			for (ItemType it : sess.getItemTypes()) {
				if (it instanceof ItemTypeParts) {

					ItemTypeParts itp = (ItemTypeParts) it;
					for (String position : itp.getPartsLists().keySet()) {
						for (ItemTypePart part : itp.getPartsLists().get(position)) {
							CheckButton button = new CheckButton(edB);
							button.setString(it.getID() + ":" + position + ":" + part.getName());
							group.addChildEnd(button);
							buttons.put(button, it.getID());
						}
					}
				}

			}
		}
		((UIParent) screen).setAllChanged();
	}

	@Override
	public void stop() {
	}

	@Override
	public void update(float delta) {
	}

}
