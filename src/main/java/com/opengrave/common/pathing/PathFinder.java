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
package com.opengrave.common.pathing;

import java.util.ArrayList;

public abstract class PathFinder {

	public static ArrayList<PathFinder> pathsToFind = new ArrayList<PathFinder>();

	public static void findSomePaths() {
		// Limit paths to find per tick to avoid excessive lag.
		int count = 20;
		synchronized (pathsToFind) {
			while (count > 0 && pathsToFind.size() > 0) {
				count--;
				PathFinder path = pathsToFind.remove(0);
				if (path.isDone()) {
					continue;
				}
				path.find();
			}
		}
	}

	public abstract void find();

	public abstract boolean isDone();

	public abstract PathProgress getResult();

	abstract int getHeuristic(PathNode node);

}
