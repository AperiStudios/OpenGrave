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

public class InputChangeEvent extends ConsumableEvent {

	private ControlBinding cb;
	private InputBinding ib;
	private boolean newState;
	private float rawState;

	public InputChangeEvent(ControlBinding cb, InputBinding ib, boolean newState, float rawState) {
		this.cb = cb;
		this.ib = ib;
		this.newState = newState;
		this.rawState = rawState;
	}

	public float getRawState() {
		return rawState;
	}

	@Override
	public String getEventName() {
		return "inputheldevent";
	}

	public InputBinding getInput() {
		return ib;
	}

	public ControlBinding getControl() {
		return cb;
	}

	public boolean getState() {
		return newState;
	}

}
