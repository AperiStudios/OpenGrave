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
package com.opengrave.common.packet.fromclient;

import java.io.IOException;

import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.packet.Packet;

public class SendChatPacket extends Packet {

	public String origin, message;

	@Override
	public void send(OGOutputStream stream) throws IOException {
		stream.writeString(origin);
		stream.writeString(message);

	}

	@Override
	public void recieve(OGInputStream stream) throws IOException {
		origin = stream.readString();
		message = stream.readString();

	}

	@Override
	public String getEventName() {
		return "sendchatpacket";
	}

}
