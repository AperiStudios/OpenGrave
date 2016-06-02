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
package com.opengrave.api;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.luaj.vm2.LuaValue;

import com.opengrave.common.DebugExceptionHandler;

public class hgcore {

	LuaValue library = null;

	public hgcore() {
		library = LuaValue.tableOf();
		com.opengrave.api.server.core serverCore = new com.opengrave.api.server.core();
		library.set("server", serverCore.get());
		com.opengrave.api.client.core clientCore = new com.opengrave.api.client.core();
		library.set("client", clientCore.get());
		com.opengrave.api.both.core bothCore = new com.opengrave.api.both.core();
		library.set("both", bothCore.get());
	}

	public void bind(SimpleBindings env) {
		env.put("hg", library);
	}

	public void bind(ScriptEngine engine) {
		engine.put("hg", library);
		try {
			engine.eval("inspect = function(x) for k, v in pairs(x) do io.write(k, \" \") end io.write(\"\\n\") end");
		} catch (ScriptException e) {
			new DebugExceptionHandler(e);
		}
	}
}
