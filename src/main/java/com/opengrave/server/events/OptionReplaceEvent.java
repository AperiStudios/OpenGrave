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
package com.opengrave.server.events;

import java.util.UUID;

import com.opengrave.common.MenuInfo;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.event.Event;
import com.opengrave.common.world.CommonObject;
import com.opengrave.server.DataConnector;
import com.opengrave.server.Server;

public class OptionReplaceEvent extends Event {
	private DataConnector dC;
	private UUID object;

	public OptionReplaceEvent(DataConnector dC, UUID object) {
		this.dC = dC;
		this.object = object;
	}

	public DataConnector getConnection() {
		return dC;
	}

	public UUID getObjectId() {
		return object;
	}

	public CommonObject getObject() {
		return Server.getSession().getObjectStorage().getObject(object);
	}

	public MenuInfo getMenu() {
		return getObject().getMenuInfo();
	}

	/**
	 * For ease of use for LUA scripting
	 * 
	 * @param id
	 * @return
	 */
	public PopupMenuOption createMenuOption() {
		PopupMenuOption pmo = new PopupMenuOption();
		return pmo;
	}

	@Override
	public String getEventName() {
		return "optionreplaceevent";
	}

}
