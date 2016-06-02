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

import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.packet.Packet;
import com.opengrave.common.pathing.Point;

public class ObjectLookTowardsPacket extends Packet {
	public UUID id;
	public Point point;

	public ObjectLookTowardsPacket() {
	}

	public ObjectLookTowardsPacket(UUID id, Point point) {
		this.id = id;
		this.point = point;
	}

	@Override
	public void send(OGOutputStream stream) throws IOException {
		stream.writeUUID(id);
		stream.writePoint(point);
	}

	@Override
	public void recieve(OGInputStream stream) throws IOException {
		id = stream.readUUID();
		point = stream.readPoint();
	}

	@Override
	public String getEventName() {
		return "objectlooktowardspacket";
	}

}
