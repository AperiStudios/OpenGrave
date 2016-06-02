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
package com.opengrave.common.world;

import com.opengrave.common.packet.fromserver.ObjectAnimationSetPacket;
import com.opengrave.common.pathing.Line;
import com.opengrave.common.pathing.Path;
import com.opengrave.common.pathing.PathFinderPolygon;
import com.opengrave.common.pathing.Point;
import com.opengrave.server.Server;
import com.opengrave.server.runnables.PathRunnable;

public class MovableObject extends CommonObject {
	Path p;
	PathFinderPolygon finder;
	private boolean animTicked;
	private int animTicks;

	public PathFinderPolygon getFinder() {
		return finder;
	}

	public void setFinder(PathFinderPolygon finder) {
		this.finder = finder;
	}

	/**
	 * Should only be used on server objects. Untested effects when used in client
	 * 
	 * @param animName
	 * @param ticks
	 */
	public void startAnimation(String animName, int ticks) {
		if (Server.getServer() == null) {
			return;

		}
		ObjectAnimationSetPacket packet = new ObjectAnimationSetPacket();
		packet.uuid = this.getUUID();
		packet.animName = animName;
		packet.ticks = ticks;
		Server.getServer().sendToAllObject(this.getUUID(), packet);
		animTicks = ticks;
	}

	public void doAnimationTick() {
		animTicked = false;
		if (animTicks > 0) {
			animTicks--;
			animTicked = true;
		}
	}

	public double doMovementTick() {
		if (animTicked) {
			return Double.NaN;
		}
		if (finder == null) {
			return Double.NaN;
		} // We have no destination. Idle
		if (p != null) {
			if (p.getEndPoint().equals(new Point(getLocation()))) {
				p = null;
				finder = null;
				return Double.NaN;
			}
			double dist = p.getDistanceOnPath(getLocation());

			if (Double.isNaN(dist)) {
				// We're no longer on the path
				p = null;
			} else {
				dist += 0.3; // TODO Speed, Ground types etcetc
				CommonLocation cl = p.getLocation(dist);
				getLocation().setFullX(cl.getFullXAsFloat());
				getLocation().setFullY(cl.getFullYAsFloat());
				getLocation().setLayer(cl.getLayer());
				// TODO Add time elapsed to dist and set that as "next tick" dist
				return dist;
			}
		}
		// Either p == null or p is valid but we're not on it
		PathRunnable pr = new PathRunnable(this, finder);
		Server.getServer().addRunnable(pr);
		return Double.NaN;
	}

	public Path getPath() {
		return p;
	}

	public void setPath(Path p) {
		this.p = p;
	}

	public Line getLine(double pos) {
		if (p == null) {
			return null;
		}
		return p.getLine(pos);
	}

}
