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
import com.opengrave.og.gui.callback.TextInputEvent;
import com.opengrave.og.gui.callback.UIElementFocusEvent;
import com.opengrave.og.gui.callback.TextInputEvent.Action;
import com.opengrave.og.input.KeyboardRawPressEvent;

public class TextInput extends BaseText implements EventListener {

	public TextInput(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onKeyPress(KeyboardRawPressEvent eventK) {
		if (eventK.isConsumed()) {
			return;
		}

		if (!eventK.getState()) {
			return;
		}
		if (this.equals(eventK.getElement())) {
			char keycode = eventK.getCharacter();
			if (keycode == 0) {
				return;
			}
			TextInputEvent event = new TextInputEvent(this, keycode);
			if (event.isEndLine()) {
				event.setAction(TextInputEvent.Action.DELETEALL);
			} else if (event.isBackspace()) {
				event.setAction(TextInputEvent.Action.DELETE);
			}
			EventDispatcher.dispatchEvent(event);
			eventK.setConsumed();
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onTextInput(TextInputEvent event) {
		if (event.isConsumed()) {
			return;
		}
		if (event.getInput() == this) {
			event.setConsumed();
			if (event.getAction() == Action.DELETE) {
				String s = getString();
				if (s.length() > 0) {
					s = s.substring(0, s.length() - 1);
				}
				setString(s);
			} else if (event.getAction() == Action.DELETE) {
				setString("");
			} else if (event.getAction() == Action.INSERT) {
				setString(getString() + event.getCharAdded());
			}
		}
	}

	// verride
	// public boolean onClick(int x, int y, int relative_x, int relative_y,
	// int button, boolean state) {
	// return false;
	// }

	@Override
	public void repopulateQuads() {
		UIQuad q = new UIQuad();
		q.setPos(0, 0, width, height).setTexture(0, 0, 0, 0, 0).setColour(this.focus ? getElementData().activeColour : getElementData().defaultColour);
		addQuad(q);
		super.repopulateQuads();
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = getFont().fontheight;

	}

	@Override
	protected boolean shouldRenderForPicking() {
		return true;
	}

	@Override
	public boolean isFocusable() {
		return true;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onFocus(UIElementFocusEvent event) {
		if (event.getElement() == this) {
			if (!focus) {
				focus = true;
				setChanged();
			}
			setChanged();
		} else {
			if (focus) {
				focus = false;
				setChanged();
			}
		}
	}

}
