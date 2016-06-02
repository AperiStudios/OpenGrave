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
package com.opengrave.server.runnables;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.pathing.PathFinderPolygon;
import com.opengrave.common.pathing.Point;
import com.opengrave.common.world.MovableObject;
import com.opengrave.server.events.ServerObjectSetPathEvent;

public class PathRunnable implements Runnable {

	private MovableObject obj;
	private PathFinderPolygon pathFinder;

	public PathRunnable(MovableObject obj, PathFinderPolygon finder) {
		this.obj = obj;
		this.pathFinder = finder;
	}

	@Override
	public void run() {
		if (pathFinder != null) {
			if (pathFinder.isComplete(obj)) {
				obj.setFinder(null);
				return;
			}
			pathFinder.makePath(new Point(obj.getLocation()));
			obj.setPath(pathFinder.getPath());
			if (pathFinder.getPath() != null) { // Don't spam nullpath to each client every tick.
				ServerObjectSetPathEvent sospe = new ServerObjectSetPathEvent(pathFinder, obj);
				EventDispatcher.dispatchEvent(sospe);
			}
		}
	}
}
