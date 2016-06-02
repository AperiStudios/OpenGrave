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
package com.opengrave.common.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.MenuInfo;
import com.opengrave.common.config.BinaryNodeException;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.server.Server;

/***
 * Very similar to BaseObject except without any render/client information. Only
 * what needs to be known by server.
 * 
 * @author triggerhapp
 * 
 */
public class CommonObject {
	public static enum Type {
		Static, Anim, Particle
	}

	public static Type getType(String s) {
		if (s.equalsIgnoreCase("static")) {
			return Type.Static;
		} else if (s.equalsIgnoreCase("anim")) {
			return Type.Anim;
		} else if (s.equalsIgnoreCase("particle")) {
			return Type.Particle;
		}
		return null;
	}

	// private UUID uuid;
	private BinaryParent data;

	public CommonObject() {
		data = new BinaryParent();
	}

	public CommonObject(BinaryParent parent) {
		data = parent;
	}

	public CommonObject(String id, Type type, String model, MaterialList mat, CommonLocation cloc) {
		data = new BinaryParent();
		try {
			setType(type);
			data.setString("model", model);
			data.setMaterialList("mat", mat);
			data.setString("id", id);
			data.setLocation("location", cloc);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	public CommonObject(OGInputStream in) {
		try {
			data = in.readBinaryNode();
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}
	}

	public String getModelLabel() {
		try {
			return data.getString("model");
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
		return null;
	}

	public void setModelLabel(String value) {
		try {
			data.setString("model", value);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	public MaterialList getMaterialList() {
		try {
			return data.getMaterialList("mat");
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
		return null;
	}

	public String getIdentifier() {
		try {
			return data.getString("id");
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
		return null;
	}

	public void setIdentifier(String id) {
		try {
			data.setString("id", id);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	public CommonLocation getLocation() {
		CommonLocation loc = null;
		try {
			loc = data.getLocation("location");
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);

		}
		if (loc == null) {
			loc = new CommonLocation();
		}
		return loc;
	}

	public UUID getUUID() {
		UUID uuid = null;
		try {
			uuid = getData().getUUID("uuid");
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
		if (uuid == null) {
			setUUID(UUID.randomUUID());
			uuid = getUUID();
		}
		return uuid;
	}

	public BinaryParent getData() {
		return data;
	}

	public void setUUID(UUID id) {
		try {
			getData().setUUID("uuid", id);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	public void setType(Type type) {
		String typeS = "";
		if (type == Type.Static) {
			typeS = "static";
		} else if (type == Type.Anim) {
			typeS = "anim";
		} else if (type == Type.Particle) {
			typeS = "particle";
		}
		if (!typeS.equals("")) {
			try {
				data.setString("type", typeS);
			} catch (BinaryNodeException e) {
				new DebugExceptionHandler(e);

			}
		}
	}

	public Type getType() {
		try {
			return getType(data.getString("type"));
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
		return null;
	}

	public void setLocation(CommonLocation loc) {
		try {
			data.setLocation("location", loc);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	/**
	 * Only intended to be called via LUA Code
	 * 
	 * @param proc
	 * @param values
	 */
	public void addProcess(CommonProcess proc, ArrayList<Float> values) {
		EventDispatcher.loadingSession.addObjectProcess(getUUID(), proc, values);
	}

	/**
	 * Options for this object need to be updated. Will update for all connected users where applicable
	 */
	public void replaceOptions() {
		if (Server.getServer() == null) {
			return;
		}
		Server.getServer().replaceOptionsAll(getUUID());
	}

	public MenuInfo getMenuInfo() {
		MenuInfo mi = new MenuInfo();
		try {
			mi = data.getMenuInfo("menu");
		} catch (BinaryNodeException bne) {
		}
		if (mi == null) {
			return new MenuInfo();
		}
		return mi;
	}

	public void setMenuInfo(MenuInfo mi) {
		if (mi == null) {
			mi = new MenuInfo();
		}
		try {
			data.setMenuInfo("menu", mi);
		} catch (BinaryNodeException e) {
			e.printStackTrace();
		}
	}
}
