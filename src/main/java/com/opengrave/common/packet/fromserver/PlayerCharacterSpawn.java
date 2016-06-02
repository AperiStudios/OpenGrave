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
package com.opengrave.common.packet.fromserver;

import java.io.IOException;
import java.util.UUID;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.config.BinaryNodeException;
import com.opengrave.common.config.BinaryObjectWrongType;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.packet.Packet;

/**
 * Seperated slightly from ObjectSpawnPacket simply because this stores info that no other player sees, profiles, HP, stats, skills
 * 
 * @author triggerhapp
 * 
 */
public class PlayerCharacterSpawn extends Packet {

	public int playerSpot;
	// public BinaryParent data;
	public UUID objectLinked;
	public BinaryParent data;

	@Override
	public void send(OGOutputStream stream) throws IOException {
		stream.writeInt(playerSpot);
		stream.writeUUID(objectLinked);
		if (data == null) {
			data = new BinaryParent();
		} // Bah
		try {
			data.save(stream);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	@Override
	public void recieve(OGInputStream stream) throws IOException {
		playerSpot = stream.readInt();
		objectLinked = stream.readUUID();
		try {
			data = new BinaryParent(stream);
		} catch (BinaryObjectWrongType e) {
			new DebugExceptionHandler(e);
		}
	}

	@Override
	public String getEventName() {
		return "playercharacterspawn";
	}

}
