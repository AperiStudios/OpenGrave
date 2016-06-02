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
import java.util.ArrayList;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.config.BinaryNodeException;
import com.opengrave.common.config.BinaryObjectWrongType;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.packet.Packet;
import com.opengrave.common.world.CommonObject;

public class ObjectsSpawnPacket extends Packet {
	public ArrayList<CommonObject> obj;

	@Override
	public void send(OGOutputStream stream) throws IOException {
		if (obj == null) {
			stream.writeInt(0);
		}
		stream.writeInt(obj.size());
		try {
			for (CommonObject o : obj) {
				o.getData().save(stream);
			}
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	@Override
	public void recieve(OGInputStream stream) throws IOException {
		int i = stream.readInt();
		try {
			if (obj == null) {
				obj = new ArrayList<CommonObject>();
			}
			obj.clear();
			for (int c = 0; c < i; c++) {
				obj.add(new CommonObject(new BinaryParent(stream)));
			}
		} catch (BinaryObjectWrongType e) {
			new DebugExceptionHandler(e);
		}
	}

	@Override
	public String getEventName() {
		return "objectsspawnpacket";
	}

}
