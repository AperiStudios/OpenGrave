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

import com.opengrave.common.event.Event;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.engine.Location;

public class MouseRenderableDragEvent extends Event {

	private Pickable picked;
	private int button;
	private Location location;

	public MouseRenderableDragEvent(Pickable picked, Location location, int button) {
		this.button = button;
		this.picked = picked;
		this.location = location;
	}

	public int getButton() {
		return button;
	}

	public Pickable getObject() {
		return picked;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public String getEventName() {
		return "renderabledragevent";
	}

}
