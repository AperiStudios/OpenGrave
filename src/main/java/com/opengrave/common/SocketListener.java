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
package com.opengrave.common;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import com.opengrave.common.event.ConnectionLostEvent;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.PacketRecieveEvent;
import com.opengrave.common.packet.Packet;

public class SocketListener implements Runnable {

	private final OGInputStream input;
	private boolean running = true;
	private Thread thread;
	Connector conn;

	public void stop() {
		running = false;
		thread.interrupt();
		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SocketListener(Connector conn, InputStream input) {
		this.conn = conn;
		this.input = new OGInputStream(input);
		thread = new Thread(this, "Socket Listener Thread");
		thread.start();
	}

	public void run() {
		try {
			while (running) {
				int type = 0;
				try {
					type = input.readInt();
				} catch (SocketException e) {
				} catch (EOFException e) {
					EventDispatcher.dispatchEvent(new ConnectionLostEvent(conn));
					conn.setDestroy();
					return;
				} catch (IOException e) {
					new DebugExceptionHandler(e, "No Packet ID");
					EventDispatcher.dispatchEvent(new ConnectionLostEvent(conn));
					conn.setDestroy();
					return;
				}
				if (type >= 0) {
					Packet packet = Packet.getPacketId(type);
					if (packet == null) {
						System.out.println("Unknown packet : " + type);
						EventDispatcher.dispatchEvent(new ConnectionLostEvent(conn));
						conn.setDestroy();
						return;
					}
					if (Thread.interrupted()) {
						return;
					}
					packet.setFrom(conn);
					packet.recieve(input);
					EventDispatcher.dispatchEvent(new PacketRecieveEvent(conn, packet));
					EventDispatcher.dispatchEvent(packet);
				}

			}
			input.close();
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}
	}

	public void setName(String string) {
		thread.setName(string + " : Listener");
	}

}
