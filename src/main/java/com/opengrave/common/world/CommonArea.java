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
package com.opengrave.common.world;

import java.util.ArrayList;
import java.util.UUID;

public class CommonArea {

	ArrayList<CommonObject> objects = new ArrayList<CommonObject>();

	public void addObject(CommonObject obj) {
		synchronized (objects) {
			objects.add(obj);
		}
	}

	public ArrayList<CommonObject> getAllObjects() {
		ArrayList<CommonObject> newList = new ArrayList<CommonObject>();
		synchronized (objects) {
			newList.addAll(objects);
		}
		return newList;
	}

	public ArrayList<CommonObject> getObjectsById(String id) {
		ArrayList<CommonObject> newList = new ArrayList<CommonObject>();
		synchronized (objects) {
			for (CommonObject obj : objects) {
				if (obj.getIdentifier().equalsIgnoreCase(id)) {
					newList.add(obj);
				}
			}
		}
		return newList;
	}

	public void removeObject(CommonObject obj) {
		synchronized (objects) {
			objects.remove(obj);
		}
	}

	public CommonObject getObject(UUID id) {
		synchronized (objects) {
			for (CommonObject obj : objects) {
				if (obj.getUUID().equals(id)) {
					return obj;
				}
			}
		}
		return null;
	}
}
