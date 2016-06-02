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

import java.util.HashMap;

import com.opengrave.common.ServerData;
import com.opengrave.common.event.*;
import com.opengrave.common.xml.HGXMLThread;
import com.opengrave.og.MainThread;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.states.waitables.GameLoader;

public class ServerListState extends BaseState implements EventListener {

	long timestamp;
	private TextButton refreshButton;
	private Button exitButton;
	private VerticalContainer vert;
	HashMap<TextButton, ServerData> serverButtons = new HashMap<TextButton, ServerData>();

	public ServerListState() {
		EventDispatcher.addHandler(this);
		GUIXML mainMenuFile = new GUIXML("gui/serverlist.xml");
		screen = mainMenuFile.getGUI();
		refreshButton = (TextButton) screen.getElementById("refreshbutton");
		exitButton = (Button) screen.getElementById("exitbutton");
		vert = (VerticalContainer) screen.getElementById("container");
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButtonPress(ButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getButton().equals(refreshButton)) {
			doRefreshListNow();
		} else if (event.getButton().equals(exitButton)) {
			MainThread.changeState(new MenuState());
		} else if (serverButtons.containsKey(event.getButton())) {
			MainThread.changeServerConnection(serverButtons.get(event.getButton()));
			MainThread.changeState(new LoadingState(new GameLoader(serverButtons.get(event.getButton()).getMods())));
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onServerListUpdate(ServerListEvent event) {
		if (!isActive()) {
			return;
		}
		vert.removeAllChildren();
		TextArea ta = new TextArea(new ElementData(vert.getElementData()));
		ta.setString("Not ingame:");
		for (ServerData sd : event.getList()) {
			if (sd.getPort().equals("-1")) {
				// Not a Server. Client is online but not playing yet
				TextButton tb = new TextButton(new ElementData(vert.getElementData()));
				tb.setString(sd.getNames());
				tb.setColourScheme(screen.getElementData());
				tb.setDisabled(true);
				vert.addChildEnd(tb);
			} else {
				// Name length random guess. IP length x.x.x.x (7 chars min)
				if (sd.getNames().length() < 3 || sd.getIP().length() < 7) {
					continue;
				}
				TextButton tb = new TextButton(new ElementData(vert.getElementData()));
				serverButtons.put(tb, sd);
				tb.setString(sd.getNames());
				tb.setColourScheme(screen.getElementData());
				vert.addChildStart(tb);
			}
		}
		refreshButton.setDisabled(false);
		refreshButton.setString("Refresh Now");
		vert.setAllChanged();
	}

	public void doRefreshList() {
		long now = System.currentTimeMillis();
		long inter = 1000 * 30; // Refresh every 30 seconds
		if (timestamp + inter < now) {
			doRefreshListNow();

		}
	}

	public void doRefreshListNow() {
		refreshButton.setDisabled(true);
		refreshButton.setString("Refreshing...");
		timestamp = System.currentTimeMillis();
		HGXMLThread.requestServerlist();

	}

	@Override
	public void start() {
		doRefreshListNow();

	}

	@Override
	public void stop() {

	}

	@Override
	public void update(float delta) {
		doRefreshList();
	}
}
