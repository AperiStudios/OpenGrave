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

import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

public class ControllerPluggedListener implements ControllerListener {

	@Override
	public void controllerAdded(ControllerEvent arg0) {
		if (InputMain.c == null) {
			InputMain.c = arg0.getController();
			// InputMain.c
			System.out.println("Controller Added");
		}
	}

	@Override
	public void controllerRemoved(ControllerEvent arg0) {
		if (InputMain.c.equals(arg0.getController())) {
			InputMain.c = null;
			System.out.println("Controller Removed");
		}
	}
}
