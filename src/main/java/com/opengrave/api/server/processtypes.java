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

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.luahelper.LuaHelper;
import com.opengrave.common.luahelper.ProcessLoopCallback;

public class processtypes {
	public LuaValue get() {
		LuaValue library = LuaValue.tableOf();
		library.set("makeProcess", new makeProcess());
		library.set("addProcessToObject", new addProcessToObject());
		return library;
	}

	public static class addProcessToObject extends OneArgFunction {
		public LuaValue call(LuaValue args) {

			return valueOf(false);
		}
	}

	public static class makeProcess extends OneArgFunction {
		public LuaValue call(LuaValue args) {
			if (args.istable()) {
				ProcessLoopCallback plc = new ProcessLoopCallback();
				LuaHelper.loopThrough(args.checktable(), plc);
				return CoerceJavaToLua.coerce(EventDispatcher.loadingSession.addProcess(plc.name, plc.list));
			}
			return valueOf(false);
		}
	}

}
