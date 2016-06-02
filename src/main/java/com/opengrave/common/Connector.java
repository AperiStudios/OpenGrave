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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.opengrave.common.packet.Packet;

public class Connector {

	// Every Connector of any sort must be able to send and recieve
	protected Socket socket = null;
	protected InputStream input = null;
	protected OutputStream output = null;
	public SocketListener socketListen;
	public SocketSender socketSend;
	public boolean loopback;
	private boolean destroy;

	public Connector() {
		// Fake connector to allow pseudo clients
	}

	public void setDestroy() {
		destroy = true;
	}

	public boolean getDestroy() {
		return destroy;
	}

	public Connector(Socket Socket, String string) {
		socket = Socket;
		try {
			input = Socket.getInputStream();
			output = Socket.getOutputStream();
		} catch (IOException e) {
			new DebugExceptionHandler(e);
			return;
		}

		socketSend = new SocketSender(this, output);
		socketListen = new SocketListener(this, input);
		setName(string);
		loopback = true;
	}

	public void setName(String string) {
		socketSend.setName(string);
		socketListen.setName(string);
	}

	public void send(Packet packet) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OGOutputStream out = new OGOutputStream(baos);
		try {
			out.writeInt(Packet.getPacketId(packet));
			packet.send(out);
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}
		if (out.size() > 1) {
			try {
				out.flush();
			} catch (IOException e) {
				new DebugExceptionHandler(e);
			}
			socketSend.addSend(baos);
		}

	}

	public void finalDestroy() {
		destroy = true;
		socketListen.stop();
		socketSend.stop();
	}
}
