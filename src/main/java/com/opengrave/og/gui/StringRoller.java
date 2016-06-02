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

import java.util.ArrayList;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.gui.callback.StringRollerChangedEvent;

public class StringRoller extends UIParent implements EventListener {

	boolean first = true;
	ArrayList<String> stringList = new ArrayList<String>();
	int index = 0;
	TextButton down, up;
	TextInput ta;

	public StringRoller(ElementData ed) {
		super(ed);

		EventDispatcher.addHandler(this);
		this.index = 0;
		down = new TextButton(ed);
		down.setString("<");
		up = new TextButton(ed);
		up.setString(">");
		ta = new TextInput(ed);
		ta.setString("");
		addChildEnd(up);
		addChildEnd(down);
		addChildEnd(ta);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onButtonPressed(ButtonPressedEvent event) {
		if (event.getButton().equals(down)) {
			index--;
			if (index < 0) {
				index = 0;
			}
			ta.setString("" + stringList.get(index));
			EventDispatcher.dispatchEvent(new StringRollerChangedEvent(this));
		} else if (event.getButton().equals(up)) {
			index++;
			if (index >= stringList.size()) {
				index = stringList.size() - 1;
			}
			ta.setString("" + stringList.get(index));
			EventDispatcher.dispatchEvent(new StringRollerChangedEvent(this));
		}
	}

	@Override
	public void update(float delta) {
		if (first) {
			if (ed.attributes.containsKey("valuelist")) {
				for (String value : ed.attributes.get("valuelist").split(",")) {
					stringList.add(value);
				}
			}
			if (stringList.size() == 0) {
				stringList.add("No options");
			}
			ta.setString(stringList.get(0));
			first = false;
		}
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = height;
		ta.setLocation(35, 0);
		ta.setSize(width - 70, 30, width - 70, 30);
		down.setLocation(0, 0);
		down.setSize(30, 30, 30, 30);
		up.setLocation(width - 30, 0);
		up.setSize(30, 30, 30, 30);
		setChanged();
	}

	@Override
	public void repopulateQuads() {
		UIQuad q = new UIQuad().setPos(0, 0, width, height).setColour(1f, 1f, 1f, 1f);
		addQuad(q);
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return true;
	}

	@Override
	public boolean isFocusable() {
		return true;
	}

	public String getString() {
		return stringList.get(index);
	}

	public void setStringIndex(int i) {
		if (i < 0 || i >= stringList.size()) {
			return;
		}
		index = i;
		ta.setString(stringList.get(i));
		EventDispatcher.dispatchEvent(new StringRollerChangedEvent(this));
	}

}
