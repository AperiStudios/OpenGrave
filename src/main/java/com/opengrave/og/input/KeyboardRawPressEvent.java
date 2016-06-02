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
package com.opengrave.og.input;

import com.opengrave.common.event.ConsumableEvent;
import com.opengrave.og.gui.UIElement;

/***
 * Called when a UIElement recieves a key-press. This is not the same as KeyDown
 * or KeyUp, which are raw events. A Key press is when a distinct key is
 * pressed, or held in such a way text input should see a repeat of it.
 * 
 * @author triggerhapp
 * 
 */
public class KeyboardRawPressEvent extends ConsumableEvent {

	private UIElement element;
	private boolean state;
	private String key;
	private int keycode;
	private char c;

	public KeyboardRawPressEvent(UIElement element, String key, boolean s, int keycode, Character c) {
		this.element = element;
		this.state = s;
		this.key = key;
		this.keycode = keycode;
		this.c = c;
	}

	public UIElement getElement() {
		return element;
	}

	public int getKeyCode() {
		return keycode;
	}

	@Override
	public String getEventName() {
		return "keypressevent";
	}

	public boolean getState() {
		return state;
	}

	public String getKey() {
		return key;
	}

	public Character getCharacter() {
		return c;
	}
}
