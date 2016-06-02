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

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * Single part of an ItemTypeParts description. Comes with alterations to the stats of the whole, most of which won't be used depending on the part type
 * 
 * @author triggerhapp
 * 
 */
public class ItemTypePart {
	public ItemTypePart(LuaTable tab) {
		if (tab.get("attack") != LuaValue.NIL) {
			attack = tab.get("attack").tofloat();
		}
		if (tab.get("defense") != LuaValue.NIL) {
			defense = tab.get("defense").tofloat();
		}
		if (tab.get("range") != LuaValue.NIL) {
			range = tab.get("range").tofloat();
		}
		if (tab.get("speed") != LuaValue.NIL) {
			speed = tab.get("speed").tofloat();
		}
		if (tab.get("model") != LuaValue.NIL) {
			model = tab.get("model").toString();
		}
		if (tab.get("image") != LuaValue.NIL) {
			image = tab.get("image").toString();
		}
		if (tab.get("imagecol") != LuaValue.NIL) {
			imagecol = tab.get("imagecol").toString();
		}
		if (tab.get("name") != LuaValue.NIL) {
			name = tab.get("name").toString();
		}
	}

	String name, model, imagecol, image;
	// Weapon Stats
	float attack, defense, range, speed;

	public String getName() {
		return name;
	}

	public void fill(StringBuilder sb) {
		sb.append("Model : ").append(model).append("\n");
		sb.append("Image Colour : ").append(imagecol).append("\n");
		sb.append("Image : ").append(image).append("\n");
		sb.append("Attack : ").append(attack).append("\n");
		sb.append("Defense : ").append(defense).append("\n");
		sb.append("Range : ").append(range).append("\n");
		sb.append("Speed : ").append(speed).append("\n");

	}

}
