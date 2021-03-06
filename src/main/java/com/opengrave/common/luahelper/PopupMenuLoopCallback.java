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
package com.opengrave.common.luahelper;

import org.luaj.vm2.LuaValue;

import com.opengrave.common.PopupMenuOption;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.util.Vector4f;

public class PopupMenuLoopCallback extends LoopCallback {
	String id, label, icon;
	Vector4f col;

	@Override
	public void loopIter(String key, LuaValue val) {
		if (key.equalsIgnoreCase("id")) {
			id = val.tojstring();
		} else if (key.equalsIgnoreCase("label")) {
			label = val.tojstring();
		} else if (key.equalsIgnoreCase("icon")) {
			icon = val.tojstring();
		} else if (key.equalsIgnoreCase("col")) {
			col = GUIXML.getColour(val.tojstring());
		}
	}

	public PopupMenuOption getPopup() {
		PopupMenuOption pmo = null;
		if (id != null && label != null) {
			if (icon == null) {
				icon = "none";
			}
			pmo = new PopupMenuOption(id, icon, label);
			if (col != null) {
				pmo.setColour(col.x, col.y, col.z);
			}
		}
		return pmo;
	}

}
