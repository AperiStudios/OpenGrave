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
package com.opengrave.common.event;

import com.opengrave.server.DataConnector;

public class PlayerJoinedEvent extends ConsumableEvent {

	private String name;
	private String id;
	private String banReason = null;
	private DataConnector conn;

	public PlayerJoinedEvent(String name, String id, DataConnector c) {
		this.name = name;
		this.id = id;
		this.conn = c;
		banReason = null;
	}

	public DataConnector getConnection() {
		return conn;
	}

	public String getPlayerName() {
		return name;
	}

	public String getPlayerId() {
		return id;
	}

	public String getBanReason() {
		return banReason;
	}

	public void setBanReason(String banReason) {
		this.banReason = banReason;
	}

	@Override
	public String getEventName() {
		return "playerjoinedevent";
	}

}
