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
package com.opengrave.api.both;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import com.opengrave.common.event.CustomEvent;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.Mod;

public class core {

	public LuaValue get() {
		LuaValue library = LuaValue.tableOf();
		library.set("registerEvent", new registerEvent());
		library.set("throwCustomEvent", new throwCustomEvent());
		library.set("getAllMods", new getAllMods());
		return library;
	}

	/**
	 * First Argument is the mod library passed to main Second Argument is the
	 * event type Third Arguement is the priority Fourth Argument is the
	 * function to call in that event
	 * 
	 * There is no warning about typos in event type, because the event system
	 * will allow mods to add their own events which will be treated in a
	 * similar manner to built-in events.
	 * 
	 * @author triggerhapp
	 * 
	 */
	public static class registerEvent extends VarArgFunction {

		@Override
		public LuaValue invoke(Varargs args) { // LuaValue library, LuaValue
												// eventType, LuaValue priority,
												// LuaValue handler) {
			if (args.narg() != 4) {
				return valueOf(false);
			}
			EventDispatcher.addHandler(args.arg(1), args.arg(2).checkjstring(), args.arg(3).checkjstring(), args.arg(4));
			return valueOf(true);
		}
	}

	/***
	 * First Argument is the event type Second Argument is a table (array?) of
	 * variables as meta-data for handlers
	 * 
	 * @author triggerhapp
	 * 
	 */
	public static class throwCustomEvent extends TwoArgFunction {

		@Override
		public LuaValue call(LuaValue arg0, LuaValue arg1) {
			CustomEvent customEvent = new CustomEvent(arg0.toString(), arg1);
			EventDispatcher.dispatchEvent(customEvent);
			return valueOf(true);
		}
	}

	public static class getAllMods extends ZeroArgFunction {

		@Override
		public LuaValue call() {
			LuaValue lib = LuaValue.tableOf();
			for (Mod mod : Mod.getAll()) {
				lib.set("mod" + mod.getId(), mod.getLibrary());
			}
			return lib;
		}
	}

}
