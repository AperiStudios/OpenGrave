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
package com.opengrave.api.server;

import java.io.File;
import java.util.UUID;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.luahelper.LuaHelper;
import com.opengrave.common.luahelper.PopupMenuLoopCallback;
import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.CommonWorld;
import com.opengrave.og.MainThread;
import com.opengrave.server.Server;

public class core {

	public LuaValue get() {
		LuaValue library = LuaValue.tableOf();
		library.set("getServer", new getServer());
		library.set("loadWorld", new loadWorld());
		library.set("addObject", new addObject());
		library.set("getObjectsById", new getObjectsById());
		library.set("createMenuOption", new createMenuOption());
		library.set("addTokensFromFile", new addTokensFromFile());

		com.opengrave.api.server.itemtypes serverItems = new com.opengrave.api.server.itemtypes();
		library.set("itemtypes", serverItems.get());
		com.opengrave.api.server.processtypes processTypes = new com.opengrave.api.server.processtypes();
		library.set("processtypes", processTypes.get());
		return library;
	}

	public static class getServer extends ZeroArgFunction {
		@Override
		public LuaValue call() {
			return CoerceJavaToLua.coerce(Server.getServer());
		}

	}

	public static class createMenuOption extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg0) {
			if (!arg0.istable()) {
				return LuaValue.valueOf(false);
			}
			PopupMenuLoopCallback lc = new PopupMenuLoopCallback();
			LuaHelper.loopThrough(arg0.checktable(), lc);
			PopupMenuOption pmo = lc.getPopup();
			if (pmo == null) {
				return LuaValue.valueOf(false);
			}
			return CoerceJavaToLua.coerce(pmo);
		}
	}

	public static class getObjectsById extends OneArgFunction {
		@Override
		public LuaValue call(LuaValue arg0) {
			LuaTable table = LuaValue.tableOf();
			if (arg0.isstring()) {
				for (CommonObject obj : EventDispatcher.loadingSession.getObjectStorage().getObjectsByType(arg0.checkjstring())) {
					table.add(CoerceJavaToLua.coerce(obj));
				}
			} else if (arg0.isuserdata() && arg0.checkuserdata() instanceof UUID) {
				UUID uuid = (UUID) arg0.checkuserdata();
				table.add(CoerceJavaToLua.coerce(EventDispatcher.loadingSession.getObjectStorage().getObject(uuid)));
			}
			return table;
		}
	}

	/**
	 * First Argument is the directory of the world to load, relative to the
	 * game cache. Most likely to be "mod/MODNUMBER/worldName"
	 * 
	 * @author triggerhapp
	 * 
	 */
	public static class loadWorld extends OneArgFunction {

		@Override
		public LuaValue call(LuaValue arg0) {
			CommonWorld w = EventDispatcher.loadingSession.addWorld(arg0.tojstring());
			return CoerceJavaToLua.coerce(w);
		}
	}

	/**
	 * First Argument is an LUA table describing the object to create.
	 * 
	 * @author triggerhapp
	 * 
	 */
	public static class addObject extends OneArgFunction {

		@Override
		public LuaValue call(LuaValue arg0) {
			if (!arg0.istable()) {
				return null;
			}
			BinaryParent parent = new BinaryParent((LuaTable) arg0);
			CommonObject obj = new CommonObject(parent);
			EventDispatcher.loadingSession.getObjectStorage().addObject(obj);
			return CoerceJavaToLua.coerce(obj);
		}
	}

	/**
	 * First Argument is the file to load tokens from, relative to game cache.
	 * Most likely "/mod/MODNUMBER/tokenFile.xml"
	 * 
	 * @author triggerhapp
	 * 
	 */
	public static class addTokensFromFile extends VarArgFunction {
		@Override
		public LuaValue call(LuaValue arg0) {
			if (arg0.isstring()) {
				String s = arg0.toString();
				File f = new File(MainThread.cache, s);
				// TODO Work out how the fuck xp tokens should work
				// return
				// CoerceJavaToLua.coerce(Server.getServer().addTokens(f));
			}
			return CoerceJavaToLua.coerce(false);
		}
	}

}
