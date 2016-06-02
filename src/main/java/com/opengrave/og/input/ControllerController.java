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
package com.opengrave.og.input;

import java.util.ArrayList;

/**
 * Yo dawg
 * 
 * @author triggerhapp
 * 
 */
public class ControllerController {
	private ArrayList<ControllerLayout> layouts = new ArrayList<ControllerLayout>();

	public ControllerController() {

	}

	public void addController(ControllerLayout layout) {
		removeController(layout.getName());
		synchronized (layouts) {
			layouts.add(layout);
		}
	}

	public void addController(String fileName) {
		ControllerLayout c = null;
		synchronized (layouts) {
			c = ControllerLayout.load(fileName);
			if (c != null) {
				layouts.add(c);
			}
		}
	}

	public void removeController(String name) {
		synchronized (layouts) {
			for (int i = 0; i < layouts.size(); i += 0) {
				if (layouts.get(i).getName().equalsIgnoreCase(name)) {
					layouts.remove(i);
				} else {
					i++;
				}
			}
		}
	}

	public ControllerLayout getController(String name) {
		name = ControllerLayout.sanitiseFileString(name);
		synchronized (layouts) {
			for (ControllerLayout c2 : layouts) {
				if (name.toLowerCase().startsWith(c2.getName().toLowerCase())) {
					return c2;
				}
			}
		}
		return null;
	}
}
