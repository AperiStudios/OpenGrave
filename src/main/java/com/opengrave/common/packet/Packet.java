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
package com.opengrave.common.packet;

import java.io.IOException;
import java.util.HashMap;

import com.opengrave.common.Connector;
import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.event.Event;
import com.opengrave.common.packet.fromclient.*;
import com.opengrave.common.packet.fromserver.*;

public abstract class Packet extends Event {

	public static HashMap<Integer, Class<? extends Packet>> packetList = new HashMap<Integer, Class<? extends Packet>>();
	private Connector from;

	public void setFrom(Connector conn) {
		this.from = conn;
	}

	public Connector getFrom() {
		return from;
	}

	public static void init() {
		packetList.clear();
		addPacket(ClientAuthPacket.class);
		addPacket(ErrorPacket.class);
		addPacket(SendChatPacket.class);
		addPacket(PlayerJoinedPacket.class);
		addPacket(LoadWorldPacket.class);
		addPacket(ClientSaveFilePacket.class);
		addPacket(ObjectsSpawnPacket.class);
		addPacket(ObjectsDespawnPacket.class);
		addPacket(PlayerAddCharacterOption.class);
		addPacket(PlayerCharacterSpawn.class);
		addPacket(PlayerCharacterChosen.class);
		addPacket(NewOrderPacket.class);
		addPacket(ObjectPathSetPacket.class);
		addPacket(ObjectMovementOnPathPacket.class);
		addPacket(ObjectLookTowardsPacket.class);
		addPacket(ObjectAnimationSetPacket.class);
		addPacket(ObjectReplaceOptionsPacket.class);
		addPacket(ObjectOptionChosenPacket.class);
	}

	public static void addPacket(Class<? extends Packet> klass) {
		System.out.println("Adding Packet " + klass + " at " + packetList.size());
		packetList.put(packetList.size(), klass);
	}

	public static int getPacketId(Packet packet) {
		for (int i : packetList.keySet()) {
			if (packetList.get(i) == packet.getClass()) {
				return i;
			}
		}
		return -1;
	}

	public static Packet getPacketId(int id) {
		Packet packet = null;
		try {
			packet = (Packet) Packet.packetList.get(id).newInstance();
		} catch (InstantiationException e) {
			new DebugExceptionHandler(e, id);
			return null;
		} catch (IllegalAccessException e) {
			new DebugExceptionHandler(e, id);
			return null;
		} catch (NullPointerException e) {
			return null;
		}
		return packet;
	}

	public abstract void send(OGOutputStream stream) throws IOException;

	public abstract void recieve(OGInputStream stream) throws IOException;

}
