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

public class ServerData {

	private String names;
	private String ip = null;
	private String port = "-1";
	private String id = "-1";
	private String mods = "1";

	public ServerData(String names, String ip, String port, String id, String mods) {
		this.names = names;
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.mods = mods;
	}

	public ServerData() {
		this.id = "-1";
	}

	public String getIP() {
		return ip;
	}

	public String getPort() {
		return port;
	}

	public String getNames() {
		return names;
	}

	public String getId() {
		return id;
	}

	public String getMods() {
		return mods;
	}
}
