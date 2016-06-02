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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import com.opengrave.common.config.BinaryObjectWrongType;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.inventory.Item;
import com.opengrave.common.pathing.Path;
import com.opengrave.common.pathing.Point;
import com.opengrave.common.world.CommonLocation;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class OGInputStream extends DataInputStream {

	public OGInputStream(InputStream in) {
		super(in);
	}

	public String readString() throws IOException {
		StringBuilder sb = new StringBuilder();
		int len = readInt();
		for (int i = 0; i < len; i++) {
			sb.append(readChar());
		}
		return sb.toString();

	}

	public Colour readColour() throws IOException {
		return new Colour(readString());
		/*
		 * int r = 0, g = 0, b = 0; boolean bold=false, italic=false,
		 * underline=false, strikethrough=false, magic=false; String format="";
		 * try{ format = readString(); r = readInt(); g = readInt(); b =
		 * readInt(); bold = readBoolean(); italic = readBoolean(); underline =
		 * readBoolean(); strikethrough = readBoolean(); magic = readBoolean();
		 * } catch (IOException ex){
		 * Logger.getLogger(MCInputStream.class.getName()).log(Level.SEVERE,
		 * null, ex); } return new
		 * Colour(format,r,g,b,bold,italic,underline,strikethrough,magic);
		 */
	}

	public Vector4f readVector4f() throws IOException {
		Vector4f vec = new Vector4f();

		vec.x = readFloat();
		vec.y = readFloat();
		vec.z = readFloat();
		vec.w = readFloat();
		return vec;
	}

	public CommonLocation readLocation() throws IOException {
		CommonLocation l = new CommonLocation();
		long x = readLong();
		float xm = readFloat();
		long y = readLong();
		float ym = readFloat();
		l.setLocation(x, xm, y, ym);
		float z = readFloat();
		float xrot = readFloat();
		float yrot = readFloat();
		float zrot = readFloat();
		float xscale = readFloat();
		float yscale = readFloat();
		float zscale = readFloat();
		l.setZ(z);
		l.setRotate(xrot, yrot, zrot);
		l.setScale(xscale, yscale, zscale);
		return l;
	}

	public Vector3f readVector3f() throws IOException {
		Vector3f vec = new Vector3f();
		vec.x = readFloat();
		vec.y = readFloat();
		vec.z = readFloat();

		return vec;
	}

	public BinaryParent readBinaryNode() throws IOException {
		try {
			return new BinaryParent(this);
		} catch (BinaryObjectWrongType e) {
			new DebugExceptionHandler(e);

		}
		return null;

	}

	public UUID readUUID() throws IOException {
		long most = readLong();
		long least = readLong();
		UUID uuid = new UUID(most, least);
		return uuid;
	}

	public MaterialList readMaterialList() throws IOException {
		return new MaterialList(this);
	}

	public Path readPath() throws IOException {
		Path p = new Path();
		p.setStartPoint(readPoint());
		int t = readInt();
		for (int i = 0; i < t; i++) {
			p.addPoint(readPoint());
		}
		return p;
	}

	public Point readPoint() throws IOException {
		double x = readDouble();
		double y = readDouble();
		int z = readInt();
		if (Double.isNaN(x) && Double.isNaN(y)) {
			return null;
		}
		Point p = new Point(x, y, z);
		return p;
	}

	public ArrayList<Object> readList() throws IOException, BinaryObjectWrongType {
		int typeOf2 = readInt();
		Class<?> klass2 = BinaryParent.types.get(typeOf2);
		int number = readInt();
		ArrayList<Object> list = new ArrayList<Object>(); // Since generics get compiled out it's mostly a lost cause remembering what it is here
		if ((typeOf2 < 0 || typeOf2 >= BinaryParent.types.size()) && number > 0) {
			throw new BinaryObjectWrongType("");
		}
		for (int i = 0; i < number; i++) {
			list.add(readObjectOfClass(klass2));
		}
		return list;
	}

	public Object readObjectOfClass(Class<?> klass) throws IOException, BinaryObjectWrongType {
		if (klass == String.class) {
			return readString();
		} else if (klass == Integer.class) {
			return readInt();
		} else if (klass == Long.class) {
			return readLong();
		} else if (klass == Float.class) {
			return readFloat();
		} else if (klass == Double.class) {
			return readDouble();
		} else if (klass == UUID.class) {
			return readUUID();
		} else if (klass == CommonLocation.class) {
			return readLocation();
		} else if (klass == MaterialList.class) {
			return readMaterialList();
		} else if (klass == ArrayList.class) {
			return readList();
		} else if (klass == Item.class) {
			return readItem();
		} else if (klass == MenuInfo.class) {
			return readMenuOptions();
		} else {
			throw new BinaryObjectWrongType(klass.getSimpleName());
		}
	}

	private Item readItem() {
		return null;
	}

	public MenuInfo readMenuOptions() throws IOException {
		MenuInfo mi = new MenuInfo();
		int contextCount = readInt();
		for (int i = 0; i < contextCount; i++) {
			String label = readString();
			int optionCount = readInt();
			ArrayList<PopupMenuOption> list = new ArrayList<PopupMenuOption>();
			for (int j = 0; j < optionCount; j++) {
				String id = readString();
				String lab = readString();
				Vector4f col = GUIXML.getColour(readString());
				String icon = readString();
				PopupMenuOption pmo = new PopupMenuOption(id, icon, lab).setColour(col.x, col.y, col.z);
				list.add(pmo);
			}
			mi.addOptions(label, list);
		}
		return mi;
	}
}
