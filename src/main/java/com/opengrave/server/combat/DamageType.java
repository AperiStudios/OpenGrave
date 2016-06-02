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
package com.opengrave.server.combat;

import java.util.ArrayList;

public class DamageType {
	private static ArrayList<DamageType> types = new ArrayList<DamageType>();

	public static void addDamageType(String name) {
		DamageType type = new DamageType();
		type.setLabel(name);
		types.add(type);
	}

	String label;

	private void setLabel(String name) {
		label = name;
	}

	public String getLabel() {
		return label;
	}
}
