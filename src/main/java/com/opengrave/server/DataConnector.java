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

import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import com.opengrave.common.Connector;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.order.Order;
import com.opengrave.common.order.OrderList;
import com.opengrave.common.packet.Packet;
import com.opengrave.common.world.CommonAreaLoc;
import com.opengrave.common.world.CommonObject;
import com.opengrave.server.events.OptionReplaceEvent;

public class DataConnector extends Connector implements EventListener {

	public String name = "", token = "";
	public boolean identified = false;
	public long latency;
	public boolean hideLogout = false;

	public ArrayList<CommonAreaLoc> lastSeenAreas = new ArrayList<CommonAreaLoc>();
	public ArrayList<PlayerCharacter> pcList = new ArrayList<PlayerCharacter>();
	public OrderList orderList = new OrderList();

	public DataConnector(Socket Socket, String string) {
		super(Socket, string);
		EventDispatcher.addHandler(this);
	}

	@Override
	public void send(Packet packet) {
		if (!identified) {
			return;
		}
		super.send(packet);
	}

	public String getIP() {
		return socket.getInetAddress().toString();
	}

	public void addOrder(Order order) {
		orderList.add(order);
	}

	public void replaceOptions(UUID object) {
		CommonObject co = Server.getSession().getObjectStorage().getObject(object);
		co.setMenuInfo(null);
		EventDispatcher.dispatchEvent(new OptionReplaceEvent(this, object));
	}
}
