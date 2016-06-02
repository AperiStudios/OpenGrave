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
package com.opengrave.server;

import java.io.IOException;

import org.bitlet.weupnp.GatewayDevice;
import org.xml.sax.SAXException;

import com.opengrave.common.DebugExceptionHandler;

public class ServerShutdownThread implements Runnable {

	private GatewayDevice d;
	private int port;

	public ServerShutdownThread(GatewayDevice d, int externalport) {
		this.d = d;
		this.port = externalport;
	}

	@Override
	public void run() {
		if (d != null) {
			try {
				d.deletePortMapping(port, "TCP");
			} catch (IOException e) {
				new DebugExceptionHandler(e);

			} catch (SAXException e) {
				new DebugExceptionHandler(e);

			}
		}

	}

}
