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
package com.opengrave.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.MovableObject;

public class ObjectStorage {
	private HashMap<UUID, CommonObject> store = new HashMap<UUID, CommonObject>();
	private ArrayList<UUID> changedLastTick = new ArrayList<UUID>();

	public void reset() {
		store.clear();
	}

	public ArrayList<CommonObject> getObjectsByClass(Class<? extends MovableObject> k) {
		ArrayList<CommonObject> list = new ArrayList<CommonObject>();
		synchronized (store) {
			for (CommonObject object : store.values()) {
				// if(object.get)#
				// TODO Have a class system in objects?
			}
		}
		return list;

	}

	public ArrayList<CommonObject> getObjectsByType(String type) {
		ArrayList<CommonObject> newList = new ArrayList<CommonObject>();
		synchronized (store) {
			for (CommonObject co : store.values()) {
				if (co.getIdentifier() == null) {
					continue;
				}
				if (co.getIdentifier().equalsIgnoreCase(type)) {
					newList.add(co);
				}
			}
		}
		return newList;
	}

	public CommonObject getObject(UUID id) {
		synchronized (store) {
			if (store.containsKey(id)) {
				return store.get(id);
			}
		}
		return null;
	}

	public boolean addObject(CommonObject obj) {
		UUID id;
		synchronized (store) {
			if (store.containsValue(obj)) {
				return false;
			}
			if (obj.getUUID() == null) {
				id = UUID.randomUUID();
			} else {
				id = obj.getUUID();
			}
			while (store.containsKey(id)) { // Redundant ? Probably.
				id = UUID.randomUUID();
			}
			obj.setUUID(id);
			store.put(id, obj);
		}
		synchronized (changedLastTick) {
			changedLastTick.add(id);
		}
		return true;
	}

	public ArrayList<CommonObject> getObjects() {
		ArrayList<CommonObject> newList = new ArrayList<CommonObject>();
		synchronized (store) {
			newList.addAll(store.values());
		}
		return newList;
	}

	/**
	 * Returns a list of all objects that had a major change (changed model or
	 * materials)
	 * Clears the list as well as returning it.
	 * 
	 * @return
	 */
	public ArrayList<UUID> getChangedObjectIDs() {
		ArrayList<UUID> retList;
		synchronized (changedLastTick) {
			retList = changedLastTick;
			changedLastTick = new ArrayList<UUID>();
		}
		return retList;
	}
}
