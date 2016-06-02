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
package com.opengrave.og;

import java.io.IOException;
import java.net.Socket;

import com.opengrave.common.Connector;
import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.packet.Packet;
import com.opengrave.common.packet.fromclient.ClientAuthPacket;
import com.opengrave.common.packet.fromserver.PlayerJoinedPacket;
import com.opengrave.og.states.ProfileState;
import com.opengrave.server.Server;

public class ServerConnection implements Runnable, EventListener {

	public enum State {
		UNKNOWN, PROGRESS, CONNECTED, COMPLETE, ERROR
	}

	private State state = State.UNKNOWN;
	private String host;
	private int port = -1;
	private String id = "-1";
	Server s = null;
	private Connector sConn = null;
	private Socket client;

	public ServerConnection(String host, String port, String id) {
		this.host = host;
		this.port = Integer.parseInt(port);
		this.id = id;
		EventDispatcher.addHandler(this);
	}

	public String getHost() {
		if (s != null) {
			return s.getExternalIP();
		}
		return host;
	}

	public int getPort() {
		if (s != null) {
			return s.getExternalPort();
		}
		return port;
	}

	/**
	 * Forcably closes connection without warning.
	 */
	public void killConnection() {
		if (sConn != null) {
			sConn.finalDestroy();
			sConn = null;
		}
	}

	public void connect() {
		Thread t = new Thread(this);
		t.start();
	}

	public State getState() {
		synchronized (this) {
			return state;
		}
	}

	public void setState(State s) {
		synchronized (this) {
			state = s;
			this.notifyAll();
		}
	}

	@Override
	public void run() {
		setState(State.PROGRESS);
		if (port == -1) {
			port = 4242;
		}
		if (host == null) {
			// Starting Server
			s = new Server(port);
			Server.setModSession(MainThread.getSession());
			host = "localhost";
			// Waiting for Server
			s.waitUntilReady(500, 10); // Wait 5 seconds at most for server to
										// start
		}
		try {
			client = new Socket(host, port);
			sConn = new Connector(client, "ConnectionToServer");
			ClientAuthPacket packet = new ClientAuthPacket();
			packet.userID = MainThread.USERNAME;
			packet.passKey = ProfileState.state.accessToken;
			sConn.send(packet);
			setState(State.CONNECTED);
		} catch (IOException e) {
			new DebugExceptionHandler(e);
			setState(State.ERROR);
		}
	}

	public Server getServer() {
		return s;
	}

	public String getId() {
		return id;
	}

	public void sendPacket(Packet packet) {
		sConn.send(packet);

	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onConnect(PlayerJoinedPacket event) {
		if (event.getPlayerId().equalsIgnoreCase(MainThread.USERNAME)) {
			setState(State.COMPLETE);
		}
	}

	public void stopServer() {
		if (s != null) {
			s.stop();
		}
	}
}
