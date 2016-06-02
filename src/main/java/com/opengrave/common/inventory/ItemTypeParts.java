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
package com.opengrave.common.inventory;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Items that are created of parts.
 * 
 * @author triggerhapp
 * 
 */
public class ItemTypeParts extends ItemType {
	private HashMap<String, ArrayList<ItemTypePart>> parts;

	public ItemTypeParts(String name, String id, HashMap<String, ArrayList<ItemTypePart>> parts) {
		this.itemName = name;
		this.identifier = id;
		this.parts = parts;
	}

	public HashMap<String, ArrayList<ItemTypePart>> getPartsLists() {
		return parts;
	}

}
