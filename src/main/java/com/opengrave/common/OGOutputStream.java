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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.opengrave.common.config.BinaryNodeException;
import com.opengrave.common.config.BinaryObjectWrongType;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.inventory.Item;
import com.opengrave.common.pathing.Line;
import com.opengrave.common.pathing.Path;
import com.opengrave.common.pathing.Point;
import com.opengrave.common.world.CommonLocation;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class OGOutputStream extends DataOutputStream {

	public OGOutputStream(OutputStream out) {
		super(out);
	}

	public void writeString(String s) throws IOException {
		if (s == null) {
			writeInt(0);
			return;
		}
		writeInt(s.length());
		writeChars(s);

	}

	public void writeColour(Colour c) throws IOException {
		if (c == null) {
			c = Colour.none;
		}
		writeString(c.toStream());
	}

	public void writeVector3f(Vector3f vec) throws IOException {
		writeFloat(vec.x);
		writeFloat(vec.y);
		writeFloat(vec.z);

	}

	public void writeVector4f(Vector4f vec) throws IOException {
		writeFloat(vec.x);
		writeFloat(vec.y);
		writeFloat(vec.z);
		writeFloat(vec.w);

	}

	public void writeLocation(CommonLocation l) throws IOException {
		writeLong(l.getTileX());
		writeFloat(l.getMinorX());
		writeLong(l.getTileY());
		writeFloat(l.getMinorY());
		writeFloat(l.getZ());
		writeFloat(l.getAngleX());
		writeFloat(l.getAngleY());
		writeFloat(l.getAngleZ());
		writeFloat(l.getScaleX());
		writeFloat(l.getScaleY());
		writeFloat(l.getScaleZ());

	}

	public void writeBinaryNode(BinaryParent data) throws IOException {
		try {
			data.save(this);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);

		}
	}

	public void writeUUID(UUID value) throws IOException {
		writeLong(value.getMostSignificantBits());
		writeLong(value.getLeastSignificantBits());
	}

	public void writeMaterialList(MaterialList matList) throws IOException {
		matList.save(this);
	}

	public void writePath(Path path) throws IOException {
		writePoint(path.getStartPoint());
		writeInt(path.getLines().size() + 1);
		writePoint(path.getLines().get(0).getPoint(0));
		for (Line l : path.getLines()) {
			writePoint(l.getPoint(1));
		}

	}

	public void writePoint(Point point) throws IOException {
		if (point == null) {
			writeDouble(Double.NaN);
			writeDouble(Double.NaN);
			writeInt(-1);
		}
		writeDouble(point.getX());
		writeDouble(point.getY());
		writeInt(point.getZ());
	}

	public void writeList(ArrayList<? extends Object> value) throws IOException {
		int typeOf = -1;
		if (value.size() > 0) {
			typeOf = BinaryParent.types.indexOf(value.get(0).getClass());
		}
		writeInt(typeOf);
		writeInt(value.size());

	}

	public void writeItem(Item value) {

	}

	@SuppressWarnings("unchecked")
	public void writeObject(Object value) throws IOException, BinaryObjectWrongType {
		if (value instanceof String) {
			writeString((String) value);
		} else if (value instanceof Integer) {
			writeInt((Integer) value);
		} else if (value instanceof Long) {
			writeLong((Long) value);
		} else if (value instanceof Float) {
			writeFloat((Float) value);
		} else if (value instanceof Double) {
			writeDouble((Double) value);
		} else if (value instanceof UUID) {
			writeUUID((UUID) value);
		} else if (value instanceof CommonLocation) {
			writeLocation((CommonLocation) value);
		} else if (value instanceof MaterialList) {
			writeMaterialList((MaterialList) value);
		} else if (value instanceof ArrayList) {
			writeList((ArrayList<? extends Object>) value);
		} else if (value instanceof Item) {
			writeItem((Item) value);
		} else if (value instanceof MenuInfo) {
			writeMenuOptions((MenuInfo) value);
		} else {
			throw new BinaryObjectWrongType(value.getClass().getSimpleName());
		}

	}

	public void writeMenuOptions(MenuInfo mi) throws IOException {
		Set<String> strings = mi.getContexts();
		writeInt(strings.size());
		for (String string : strings) {
			writeString(string);
			ArrayList<PopupMenuOption> list = mi.getFullList(string);
			writeInt(list.size());
			for (PopupMenuOption pmo : list) {
				String label = pmo.label;
				String icon = pmo.icon;
				String col = null;// TODO give a fuck
				String id = pmo.id;
				if (id == null) {
					id = "noid";
				}
				if (label == null) {
					label = "No Label";
				}
				if (icon == null) {
					icon = "none";
				}
				if (col == null) {
					col = "#fff";
				}
				writeString(id);
				writeString(label);
				writeString(col);
				writeString(icon);
			}
		}
	}

}
