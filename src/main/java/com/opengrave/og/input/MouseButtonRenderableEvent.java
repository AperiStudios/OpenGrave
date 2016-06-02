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

public class MouseButtonRenderableEvent extends ConsumableEvent {

	private Pickable picked;
	private int button, rx, ry, sx, sy;
	private Location location;

	public MouseButtonRenderableEvent(Pickable picked, Location l, int button, int screenx, int screeny) {
		this.picked = picked;
		this.button = button;
		this.location = l;
		this.sx = screenx;
		this.sy = screeny;
	}

	public MouseButtonRenderableEvent(Pickable picked, Location l, int button, int rx, int ry, int screenx, int screeny) {
		this.picked = picked;
		this.location = l;
		this.button = button;
		this.rx = rx;
		this.ry = ry;
		this.sx = screenx;
		this.sy = screeny;
	}

	public Pickable getObject() {
		return picked;
	}

	public int getButton() {
		return button;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public String getEventName() {
		return "mousebuttonrenderableevent";
	}

	public int getRX() {
		return rx;
	}

	public int getRY() {
		return ry;
	}

	public int getScreenX() {
		return sx;
	}

	public int getScreenY() {
		return sy;
	}
}
