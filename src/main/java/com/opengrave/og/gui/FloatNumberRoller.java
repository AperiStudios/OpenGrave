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
import com.opengrave.og.gui.callback.FloatNumberRollerChangedEvent;
import com.opengrave.og.gui.callback.TextInputEvent;
import com.opengrave.og.gui.callback.TextInputEvent.Action;

public class FloatNumberRoller extends UIParent implements EventListener {

	float cur, min, max, inc = 0.1f;
	TextButton down, up;
	TextInput ta;

	public float getNumber() {
		return cur;
	}

	public FloatNumberRoller(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
		this.cur = 0f;
		this.min = Float.NEGATIVE_INFINITY;
		this.max = Float.POSITIVE_INFINITY;
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
			cur -= inc;
			if (cur < min) {
				cur = min;
			}
			ta.setString("" + cur);
			EventDispatcher.dispatchEvent(new FloatNumberRollerChangedEvent(this));
		} else if (event.getButton().equals(up)) {
			cur += inc;
			if (cur > max) {
				cur = max;
			}
			ta.setString("" + cur);
			EventDispatcher.dispatchEvent(new FloatNumberRollerChangedEvent(this));
		}
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onTextInput(TextInputEvent event) {
		if (ta.equals(event.getInput())) {
			if (!event.isBackspace()) {
				event.setAction(Action.IGNORE);
				char k = event.getCharAdded();
				if (k >= 48 && k <= 58 || k == '-' || k == '.') {
					event.setAction(Action.INSERT);
				}
			}
			float f = Float.NaN;
			if (ta.getString().equals("-") || ta.getString().equals(".")) {
				return;
			}
			String next = event.getTextAfter();
			try {
				f = Float.parseFloat(next);
			} catch (NumberFormatException e) {
				return;
			}
			if (f >= min && f <= max) {
				cur = f;
				EventDispatcher.dispatchEvent(new FloatNumberRollerChangedEvent(this));
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
		ta.setLocation(15, 0);
		ta.setSize(width - 30, height, width - 30, height);
		down.setLocation(0, 0);
		down.setSize(15, height, 15, height);
		up.setLocation(width - 15, 0);
		up.setSize(15, height, 15, height);
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

	public void setNumber(float number) {
		this.cur = number;
		ta.setString("" + cur);
	}

}
