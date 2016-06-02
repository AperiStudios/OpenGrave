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
package com.opengrave.api.client;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

import com.opengrave.og.MainThread;
import com.opengrave.og.input.InputMain;
import com.opengrave.og.states.BindingState;
import com.opengrave.og.states.ControllerSetupState;
import com.opengrave.og.states.TEditState;

public class core {

	public LuaValue get() {
		LuaValue library = LuaValue.tableOf();
		library.set("changeState", new changeState());
		return library;
	}

	public static class changeState extends OneArgFunction {

		@Override
		public LuaValue call(LuaValue arg0) {
			String s = arg0.checkjstring();
			if (s.equalsIgnoreCase("padsetup")) {
				if (InputMain.cl == null || InputMain.c == null) {
					System.err.println("No pad is connected/chosen to setup");
					return null;
				}
				MainThread.changeState(new ControllerSetupState(InputMain.cl, InputMain.c));
			} else if (s.equalsIgnoreCase("controls")) {
				MainThread.changeState(new BindingState());
			} else if (s.equalsIgnoreCase("terrain")) {
				MainThread.changeState(new TEditState());
			}
			return null;
		}
	}
}
