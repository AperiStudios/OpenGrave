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
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.event.Mod;
import com.opengrave.common.event.ModLoadEvent;
import com.opengrave.og.MainThread;
import com.opengrave.og.ServerConnection;
import com.opengrave.og.ServerConnection.State;
import com.opengrave.og.states.BaseState;
import com.opengrave.og.states.CharacterCreateState;
import com.opengrave.og.states.ErrorState;
import com.opengrave.og.states.GameState;

public class GameLoader extends Loader implements Runnable, EventListener {

	BaseState nextState = null;
	GameState preparedGameState = null;
	private String modList;
	String statusMessage = "Preparing game";

	public GameLoader(String modList) {
		this.modList = modList;
		// For pre-load sent packets, have a gamestate prepared
		preparedGameState = new GameState();
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
	public void start() {
		MainThread.startConnectionThread();
	}

	@Override
	public void run() {
		MainThread.newSession();
		ModSession sess = MainThread.getSession();
		// Pre-run, Load all Mods into play.
		for (String mod : modList.split(",")) {
			statusMessage = "Preparing mod " + mod;
			Mod m = Mod.getMod(mod);
			ModLoadEvent event = new ModLoadEvent(m, sess);
			EventDispatcher.dispatchEvent(event);
		}
		ServerConnection sConn = MainThread.getConnection();
		statusMessage = "Connecting to server";
		while (nextState == null) {
			State connState = sConn.getState();
			if (connState == State.ERROR) {
				nextState = new ErrorState("Could not connect to game server");
				return;
			} else if (connState == State.CONNECTED) {
				statusMessage = "Authenticating with game server";
			} else if (connState == State.COMPLETE) {
				// Authentication complete. Next up is character select
				nextState = new CharacterCreateState(preparedGameState);
				return;
				// nextState = preparedGameState;
			}
			try {
				synchronized (sConn) {
					sConn.wait();
				}
			} catch (InterruptedException e) {
			}
		}
	}

}
