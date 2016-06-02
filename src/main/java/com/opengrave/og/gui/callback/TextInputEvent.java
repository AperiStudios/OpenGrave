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
package com.opengrave.og.gui.callback;

import com.opengrave.common.event.ConsumableEvent;
import com.opengrave.og.gui.TextInput;

public class TextInputEvent extends ConsumableEvent {

	public enum Action {
		INSERT, DELETE, DELETEALL, IGNORE
	}

	private char added;
	private TextInput ti;
	private Action action = Action.INSERT;

	public String getTextAfter() {
		if (action == Action.INSERT) {
			return ti.getString() + added;
		} else if (action == Action.DELETE) {
			if (ti.getString().length() == 0) {
				return "";
			}
			return ti.getString().substring(0, ti.getString().length() - 1);
		} else if (action == Action.DELETEALL) {
			return "";
		}
		return ti.getString();
	}

	public TextInputEvent(TextInput ti, char added) {
		this.added = added;
		this.ti = ti;
	}

	public boolean isBackspace() {
		return added == 8;
	}

	public boolean isEndLine() {
		return added == 10 || added == 13;
	}

	public Character getCharAdded() {
		return added;
	}

	public TextInput getInput() {
		return ti;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public String getEventName() {
		return "textinputevent";
	}
}
