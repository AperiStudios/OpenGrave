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

import java.io.IOException;
import java.util.ArrayList;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.config.BinaryParent;

public abstract class Order {
	BinaryParent data = new BinaryParent();
	private static ArrayList<Class<? extends Order>> orderClasses = new ArrayList<Class<? extends Order>>();

	public static void organiseOrders() {
		if (orderClasses.size() == 0) {
			orderClasses.add(KillOrder.class);
			orderClasses.add(GatherOrder.class);
		}
	}

	public void send(OGOutputStream stream) throws IOException {
		organiseOrders();
		stream.writeInt(orderClasses.indexOf(this.getClass()));
		stream.writeBinaryNode(data);
	}

	public static Order read(OGInputStream stream) throws IOException {
		organiseOrders();
		int klass = stream.readInt();
		BinaryParent node = stream.readBinaryNode();
		Order o = null;
		if (klass >= orderClasses.size() || klass < 0) {
			// Not a class we know
			return null;
		}
		try {
			o = orderClasses.get(klass).newInstance();
		} catch (InstantiationException e) {
			new DebugExceptionHandler(e);
		} catch (IllegalAccessException e) {
			new DebugExceptionHandler(e);
		}
		o.data = node;
		return o;
	}

	public abstract boolean canAddToList(OrderList orderList);

}
