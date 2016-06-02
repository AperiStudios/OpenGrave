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

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.gui.callback.NumberRollerChangedEvent;
import com.opengrave.og.gui.callback.TextInputEvent;
import com.opengrave.og.gui.callback.TextInputEvent.Action;

public class NumberRoller extends UIParent implements EventListener {

	int cur, min, max;
	TextButton down, up;
	TextInput ta;

	public int getNumber() {
		return cur;
	}

	public NumberRoller(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
		this.cur = 1;
		this.min = -10000;
		this.max = 10000;
		down = new TextButton(ed);
		down.setString("<");
		up = new TextButton(ed);
		up.setString(">");
		ta = new TextInput(ed);
		ta.setString("" + cur);
		addChildEnd(up);
		addChildEnd(down);
		addChildEnd(ta);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onButtonPressed(ButtonPressedEvent event) {
		if (event.getButton().equals(down)) {
			cur--;
			if (cur < min) {
				cur = min;
			}
			ta.setString("" + cur);
			EventDispatcher.dispatchEvent(new NumberRollerChangedEvent(this));
		} else if (event.getButton().equals(up)) {
			cur++;
			if (cur > max) {
				cur = max;
			}
			ta.setString("" + cur);
			EventDispatcher.dispatchEvent(new NumberRollerChangedEvent(this));
			System.out.println(cur);
		}
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onTextInput(TextInputEvent event) {
		if (ta.equals(event.getInput())) {
			if (!event.isBackspace()) {
				event.setAction(Action.IGNORE);
				char k = event.getCharAdded();
				if (k >= 48 && k <= 58 || k == '-') {
					event.setAction(Action.INSERT);
				}
			}
			int i = -2;
			if (ta.getString().equals("-")) {
				return;
			}
			String next = event.getTextAfter();
			try {
				i = Integer.parseInt(next);
			} catch (NumberFormatException e) {
				return;
			}
			if (i >= min && i <= max) {
				cur = i;
				EventDispatcher.dispatchEvent(new NumberRollerChangedEvent(this));
			}
		}
	}

	@Override
	public void update(float delta) {

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
		return false;
	}

	@Override
	public boolean isFocusable() {
		return true;
	}

	/**
	 * Does NOT throw event
	 * 
	 * @param integer
	 */
	public void setNumber(int integer) {
		this.cur = integer;
		ta.setString("" + cur);

	}

	public void setRange(int lower, int higher) {
		this.min = lower;
		this.max = higher;
	}

}
