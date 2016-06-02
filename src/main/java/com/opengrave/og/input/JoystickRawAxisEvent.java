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

import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;

import com.opengrave.common.event.ConsumableEvent;

public class JoystickRawAxisEvent extends ConsumableEvent {

	private Controller pad;
	private int index;
	private float value;
	private float delta;
	private boolean button;

	public JoystickRawAxisEvent(Controller pad, int index, float value, float delta, Identifier identifier) {
		this.pad = pad;
		this.index = index;
		this.value = value;
		this.delta = delta;
		this.button = identifier instanceof Identifier.Button || identifier instanceof Identifier.Key;
	}

	@Override
	public String getEventName() {
		return "joystickrawaxisevent";
	}

	public int getAxisIndex() {
		return index;
	}

	public float getValue() {
		return value;
	}

	public float getDelta() {
		return delta;
	}

	public Controller getPad() {
		return pad;
	}

	public float getMagnitude() {
		return getValue();
	}

	public boolean isButton() {
		return button;
	}

}
