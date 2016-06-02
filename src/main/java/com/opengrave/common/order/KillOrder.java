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
package com.opengrave.common.order;

import java.util.UUID;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.config.BinaryNodeException;

public class KillOrder extends Order {
	public UUID getUUID() {
		try {
			return data.getUUID("enemy");
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
		return null;
	}

	public void setUUID(UUID enemy) {
		try {
			data.setUUID("enemy", enemy);
		} catch (BinaryNodeException e) {
			new DebugExceptionHandler(e);
		}
	}

	@Override
	public boolean canAddToList(OrderList orderList) {
		return true;
	}
}
