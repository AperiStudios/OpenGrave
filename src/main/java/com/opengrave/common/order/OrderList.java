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

import java.util.ArrayList;

public class OrderList {
	ArrayList<Order> orderList = new ArrayList<Order>();

	public ArrayList<Order> getArray() {
		ArrayList<Order> newList = new ArrayList<Order>();
		synchronized (orderList) {
			newList.addAll(orderList);
		}
		return newList;
	}

	public void remove(Order order) {
		synchronized (orderList) {
			orderList.remove(order);
		}
	}

	public ArrayList<Order> getOrders(Class<? extends Order> orderType) {
		ArrayList<Order> newList = new ArrayList<Order>();
		synchronized (orderList) {
			for (Order order : orderList) {
				if (orderType.isInstance(order)) {
					newList.add(order);
				}
			}
		}
		return newList;
	}

	public void add(Order order) {
		if (order.canAddToList(this)) {
			orderList.add(order);
		}
	}

}
