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
package com.opengrave.server.events;

import com.opengrave.common.event.ConsumableEvent;
import com.opengrave.common.pathing.PathFinderPolygon;
import com.opengrave.common.world.MovableObject;

public class ServerObjectSetPathEvent extends ConsumableEvent {

	private PathFinderPolygon pathFinder;
	private MovableObject obj;

	public ServerObjectSetPathEvent(PathFinderPolygon pathFinder, MovableObject obj) {
		this.pathFinder = pathFinder;
		this.obj = obj;
	}

	@Override
	public String getEventName() {
		return "serverobjectsetpath";
	}

	public PathFinderPolygon getPathFinder() {
		return pathFinder;
	}

	public MovableObject getObject() {
		return obj;
	}

	public void setPathFinder(PathFinderPolygon pathFinder) {
		this.pathFinder = pathFinder;
	}

}
