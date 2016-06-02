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

import java.util.UUID;

import com.opengrave.common.MenuInfo;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.packet.fromclient.ObjectOptionChosenPacket;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.BaseObject;
import com.opengrave.og.gui.callback.PopupOptionChosen;
import com.opengrave.og.input.InputChangeEvent;
import com.opengrave.og.input.MouseButtonRenderableEvent;

public class PopupMenu extends Popup implements EventListener {
	private int localx, localy;
	private UUID uuid;
	private String pagecontext;
	private int page;
	private MenuInfo info;
	private Object reference;
	private boolean relevant = true;;

	public PopupMenu(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
	}

	public void setRelevant(boolean b) {
		this.relevant = b;
	}

	public String getPageContext() {
		return pagecontext;
	}

	/*
	 * public void setMenuOptions(String context, int page, UUID id, int x, int y) {
	 * // TODO Remove. Fall back on MenuInfo only
	 * GameState gs = (GameState) HGMainThread.getGameState();
	 * BaseObject obj = gs.objects.getObject(id);
	 * ArrayList<PopupMenuOption> list = obj.createMenuList(context, page);
	 * this.pagecontext = context;
	 * this.page = page;
	 * this.uuid = id;
	 * this.localx = x;
	 * this.localy = y;
	 * for (PopupMenuOption pmo : list) {
	 * PopupMenuBit pmb = new PopupMenuBit(new ElementData(ed));
	 * pmb.setMenuOptions(pmo, "");
	 * pmb.ed.defaultColour = pmo.getColour();
	 * this.addChildEnd(pmb);
	 * }
	 * }
	 */

	public void setMenuOptions(String context, int page, MenuInfo object, int x, int y, Object reference) {
		this.pagecontext = context;
		this.page = page;
		this.info = object;
		this.localx = x;
		this.localy = y;
		this.reference = reference;
		for (PopupMenuOption pmo : info.getList(context, page)) {
			PopupMenuBit pmb = new PopupMenuBit(new ElementData(ed));
			pmb.setMenuOptions(pmo, pmo.getControl());
			pmb.ed.defaultColour = pmo.getColour();
			this.addChildEnd(pmb);
		}
	}

	@Override
	public void repopulateQuads() {
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return false;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		if (this.width != width || this.height != height || this.childrenChanged) {
			synchronized (children) {
				this.childrenChanged = false;
				int wholeWidth = children.size() * PopupMenuBit.sizex;
				int startx = localx - (wholeWidth / 2);
				int starty = localy - (PopupMenuBit.sizey / 2);
				if (startx + wholeWidth > width) {
					startx = width - wholeWidth;
				}
				if (startx < 0) {
					startx = 0;
				}
				if (starty + PopupMenuBit.sizey > height) {
					starty = height - PopupMenuBit.sizey;
				}
				if (starty < 0) {
					starty = 0;
				}
				int count = 0;
				for (UIElement ele : children) {
					ele.setLocation(startx + (count * PopupMenuBit.sizex), starty);
					ele.setSize(PopupMenuBit.sizex, PopupMenuBit.sizey, PopupMenuBit.sizex, PopupMenuBit.sizey);
					count++;
				}
			}
		}
		this.width = width;
		this.height = height;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onInputTaken(InputChangeEvent event) {
		if (event.getState() == false || event.isConsumed() || !relevant) {
			return;
		}
		String input = event.getControl().getControlName();
		if (parent == null) {
			return;
		}
		synchronized (parent.children) {
			synchronized (children) {
				for (UIElement ele : children) {
					if (ele instanceof PopupMenuBit) {
						PopupMenuBit pmb = (PopupMenuBit) ele;
						if (pmb.getInputString().equalsIgnoreCase(input)) {
							// This is the one.
							buttonChosen(pmb);
							event.setConsumed();
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButtonRenderable(MouseButtonRenderableEvent event) {
		if (!relevant) {
			return;
		}
		if (event.getObject() instanceof PopupMenuBit) {
			if (parent == null) {
				return;
			}
			synchronized (parent.children) {
				synchronized (children) {
					PopupMenuBit pmb = (PopupMenuBit) event.getObject();
					int index = children.indexOf(pmb);
					if (index == -1) {
						return;
					}
					buttonChosen(pmb);
				}
			}
		}
	}

	public void buttonChosen(PopupMenuBit pmb) {
		String command = pmb.getMenuOptions().getId();
		if (command.startsWith("menu:")) {
			String[] split = command.split(":");
			String menu = split[1];
			int number = 0;
			if (split.length > 2) {
				try {
					number = Integer.parseInt(split[2]);
				} catch (NumberFormatException nfe) {
				}
			}
			PopupMenu nextMenu = new PopupMenu(new ElementData(parent.getElementData()));
			nextMenu.setMenuOptions(menu, number, info, localx, localy, reference);
			parent.showPopup(nextMenu);
		} else if (command.equals("cancel")) {
			EventDispatcher.dispatchEvent(new PopupOptionChosen(this, command, reference, pmb));// Still send a cancel. It might have other uses
		} else {
			if (uuid == null) {
				// Client GUI
				EventDispatcher.dispatchEvent(new PopupOptionChosen(this, command, reference, pmb));
			} else {
				// BaseObject
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onChosen(PopupOptionChosen event) {
		if (event.isConsumed() || !relevant) {
			return;
		}
		if (!event.getReference().equals(reference)) {
			return;
		}
		if (event.getId().equals("cancel")) {
			parent.closePopup();
		}
		if (event.getReference() instanceof BaseObject) { // Send to Server as info about an object
			ObjectOptionChosenPacket oocp = new ObjectOptionChosenPacket();
			oocp.id = ((BaseObject) event.getReference()).getUUID();
			oocp.option = event.getId();
			MainThread.sendPacket(oocp);
		}
	}
}
