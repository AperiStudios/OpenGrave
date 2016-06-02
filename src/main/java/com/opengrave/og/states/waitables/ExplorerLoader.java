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
package com.opengrave.og.states.waitables;

import com.opengrave.common.ModSession;
import com.opengrave.common.event.*;
import com.opengrave.og.MainThread;
import com.opengrave.og.states.BaseState;
import com.opengrave.og.states.ModExplorerState;

public class ExplorerLoader extends Loader implements EventListener {
	String statusMessage = "Loading Mods";
	ModExplorerState preparedState = new ModExplorerState();
	BaseState nextState;
	private String modList;

	public ExplorerLoader(String modList) {
		this.modList = modList;
		EventDispatcher.addHandler(this);
	}

	@Override
	public String getStatusMessage() {
		return statusMessage;
	}

	@Override
	public boolean isDone() {
		return nextState != null;
	}

	@Override
	public void finish() {
		MainThread.changeState(nextState);
	}

	@Override
	public void run() {
		MainThread.newSession();
		ModSession sess = MainThread.getSession();
		for (String mod : modList.split(",")) {
			statusMessage = "Preparing mod " + mod;
			Mod m = Mod.getMod(mod);
			EventDispatcher.dispatchEvent(new ModLoadEvent(m, sess));
		}
		EventDispatcher.dispatchEvent(new PrepareSessionEvent());
		statusMessage = "Waiting for mods to finish creating session";
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onSessionStarted(PrepareSessionEvent event) {
		nextState = preparedState;
	}

	@Override
	public void start() {
	}

}
