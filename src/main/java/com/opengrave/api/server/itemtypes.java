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

import java.util.ArrayList;
import java.util.HashMap;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.inventory.ItemMaterial;
import com.opengrave.common.inventory.ItemTypePart;
import com.opengrave.common.inventory.ItemTypeParts;
import com.opengrave.common.inventory.ItemTypeSimple;
import com.opengrave.common.luahelper.ItemPartLoopCallback;
import com.opengrave.common.luahelper.LuaHelper;

/**
 * LUA wrapper library for item types
 * 
 * @author triggerhapp
 * 
 */
public class itemtypes {

	public LuaValue get() {
		LuaValue library = LuaValue.tableOf();
		// Add commands, each must be a seperate class. I hate subclasses in
		// large projects but feel here I am justified.
		library.set("addItem", new addItem());
		// library.set("removeItem", new removeItem());
		library.set("addItemMaterial", new addItemMaterial());
		library.set("itemPart", new itemPart());
		library.set("addItemParts", new addItemParts());
		// env.set(modname, library);
		return library;
	}

	public static class addItem extends OneArgFunction {
		public LuaValue call(LuaValue args) {
			if (args == null || !args.istable()) {
				return valueOf(false);
			}
			LuaTable tab = args.checktable();
			String name = "", id = "";
			id = tab.get("id").toString();
			name = tab.get("name").toString();
			ItemTypeSimple itemType = new ItemTypeSimple(name, id);
			EventDispatcher.loadingSession.add(itemType);
			return valueOf(true);
		}
	}

	public static class removeItem extends VarArgFunction {
		public LuaValue call(Varargs args) {
			return valueOf(false);
		}
	}

	public static class addItemMaterial extends OneArgFunction {
		public LuaValue call(LuaValue val) {
			if (val == null || !val.istable()) {
				return valueOf(false);
			}
			LuaTable tab = val.checktable();
			ItemMaterial im = new ItemMaterial(tab.get("name").toString(), tab.get("edge").tofloat(), tab.get("lightarmour").tofloat(), tab.get("heavyarmour")
					.tofloat(), tab.get("mass").tofloat(), tab.get("springiness").tofloat(), tab.get("tautness").tofloat(), tab.get("red").tofloat(), tab.get(
					"green").tofloat(), tab.get("blue").tofloat(), tab.get("shininess").tofloat());

			EventDispatcher.loadingSession.add(im);

			return valueOf(false);
		}
	}

	public static class itemPart extends OneArgFunction {
		public LuaValue call(LuaValue val) {
			if (val == null || !val.istable()) {
				return valueOf(false);
			}
			LuaTable tab = val.checktable();
			ItemTypePart part = new ItemTypePart(tab);
			return CoerceJavaToLua.coerce(part);
		}
	}

	public static class addItemParts extends OneArgFunction {

		@Override
		public LuaValue call(LuaValue arg0) {
			if (arg0 == null || !arg0.istable()) {
				return valueOf(false);
			}
			LuaTable tab = arg0.checktable();
			HashMap<String, ArrayList<ItemTypePart>> parts;
			String name = "unknown", id = "noid";
			ItemPartLoopCallback lc = new ItemPartLoopCallback();
			LuaHelper.loopThrough(tab, lc);
			name = lc.name;
			id = lc.id;
			parts = lc.parts;
			ItemTypeParts itemType = new ItemTypeParts(name, id, parts);
			EventDispatcher.loadingSession.add(itemType);
			return null;
		}

	}

}
