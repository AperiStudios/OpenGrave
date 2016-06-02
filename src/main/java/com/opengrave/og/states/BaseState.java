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

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.gui.UIParent;

public abstract class BaseState {

	private boolean active;

	public boolean isActive() {
		return active;
	}

	public UIParent screen;

	public abstract void start();

	public abstract void stop();

	public abstract void update(float delta);

	public void renderGUI() {
		if (screen == null) {
			return;
		}
		Util.checkErr();
		screen.render(0, 0);
	}

	public void updateGUI(float delta) {
		if (screen == null) {
			return;
		}
		screen.setSize(MainThread.lastW, MainThread.lastH, MainThread.lastW, MainThread.lastH);
		screen.update(delta);
	}

	public void renderGuiForPicking() {
		if (screen == null) {
			return;
		}
		screen.renderForPicking(0, 0);
	}

	public void prestart() {
		active = true;
	}

	public void finalise() {
		active = false;
		if (this instanceof EventListener) {
			EventDispatcher.removeHandler((EventListener) this);
		}
		if (screen != null) {
			screen.closePopup();
		}
	}
}
