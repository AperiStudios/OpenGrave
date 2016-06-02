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

import com.opengrave.common.event.ConsumableEvent;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.engine.Location;

public class MouseWheelRenderableEvent extends ConsumableEvent {

	int delta;
	private Pickable picked;
	private Location worldLoc;

	public int getDelta() {
		return delta;
	}

	public Pickable getPicked() {
		return picked;
	}

	public Location getLocation() {
		return worldLoc;
	}

	public MouseWheelRenderableEvent(int delta) {
		this.delta = delta;
	}

	public MouseWheelRenderableEvent(Pickable picked, Location worldLoc, int delta) {
		this.delta = delta;
		this.picked = picked;
		this.worldLoc = worldLoc;
	}

	@Override
	public String getEventName() {
		return "mousewheelrenderableevent";
	}

}
