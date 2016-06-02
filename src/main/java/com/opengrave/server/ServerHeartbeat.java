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
package com.opengrave.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;

import com.opengrave.common.order.GatherOrder;
import com.opengrave.common.order.Order;
import com.opengrave.common.packet.fromserver.ObjectLookTowardsPacket;
import com.opengrave.common.packet.fromserver.ObjectMovementOnPathPacket;
import com.opengrave.common.packet.fromserver.ObjectsDespawnPacket;
import com.opengrave.common.packet.fromserver.ObjectsSpawnPacket;
import com.opengrave.common.pathing.Line;
import com.opengrave.common.pathing.PathFinderPolygonAStar;
import com.opengrave.common.pathing.Point;
import com.opengrave.common.world.*;

public class ServerHeartbeat extends TimerTask {

	public ServerHeartbeat() {

	}

	Server server;

	public ServerHeartbeat(Server server) {
		this.server = server;
	}

	public void run() {
		ArrayList<DataConnector> newCopy = Server.getServer().getConnectionsCopy();
		for (DataConnector c : newCopy) {
			// If dead, close socket cleanly
			if (c.getDestroy()) {
				// TODO Somehow notify users of logout?
				Server.getServer().dropConnection(c);
				continue;
			}
		}

		// Basic Logic round. Set paths based on orders etc
		newCopy = Server.getServer().getConnectionsCopy(); // Use a new copy with dead connections dropped
		for (DataConnector c : newCopy) {
			if (c.orderList.getArray().size() > 0) {
				// We have orders
				ArrayList<Order> o = c.orderList.getOrders(GatherOrder.class);
				if (o.size() > 0) {
					for (PlayerCharacter pc : c.pcList) {
						if (pc.getFinder() == null) { // TODO: Check destination is the same as last path to replace?
							pc.setFinder(new PathFinderPolygonAStar(Server.getSession().getWorlds().get(0).getNavMesh(), new Point(((GatherOrder) o.get(0))
									.getLocation()))); // TODO: Not Assume world 0
						}
					}
				}
			}
		}

		// TODO Persist the area in common object or object storage. Alter it only on movement below or magical teleport
		HashMap<CommonAreaLoc, ArrayList<CommonObject>> objectsByArea = new HashMap<CommonAreaLoc, ArrayList<CommonObject>>();
		for (CommonObject obj : Server.getSession().getObjectStorage().getObjects()) {
			CommonAreaLoc loc = CommonWorld.getAreaLocFor(obj.getLocation());
			if (!objectsByArea.containsKey(loc)) {
				objectsByArea.put(loc, new ArrayList<CommonObject>());
			}
			objectsByArea.get(loc).add(obj);
		}
		ArrayList<UUID> changedList = Server.getSession().getObjectStorage().getChangedObjectIDs();
		for (DataConnector c : newCopy) {
			if (c.getDestroy()) {
				continue;
			}
			// Calculate areas that connection can see
			ArrayList<CommonAreaLoc> seenAreas = new ArrayList<CommonAreaLoc>();
			ArrayList<CommonAreaLoc> lostAreas = new ArrayList<CommonAreaLoc>();
			ArrayList<CommonAreaLoc> newAreas = new ArrayList<CommonAreaLoc>();
			lostAreas.addAll(c.lastSeenAreas);
			for (PlayerCharacter pc : c.pcList) {
				CommonLocation l = pc.getLocation();
				if (l == null) {
					continue;
				}
				CommonAreaLoc center = CommonWorld.getAreaLocFor(l);
				for (int x = -1; x < 2; x++) {
					for (int y = -1; y < 2; y++) {
						CommonAreaLoc loc = center.getNeighbour(x, y);
						if (!seenAreas.contains(loc)) {
							seenAreas.add(loc);
						}
						if (lostAreas.contains(loc)) {
							lostAreas.remove(loc);
						}
						if (!c.lastSeenAreas.contains(loc)) {
							newAreas.add(loc);
						}
					}
				}
			}
			ArrayList<CommonObject> objSpawnIn = new ArrayList<CommonObject>();
			// Add all objects from areas that have only just moved into view
			for (CommonAreaLoc loc : newAreas) {
				if (!objectsByArea.containsKey(loc)) {
					continue;
				}
				objSpawnIn.addAll(objectsByArea.get(loc));
			}
			// Add (again!) all objects that have had a major change of type, material, or model
			for (CommonAreaLoc loc : seenAreas) {
				if (!objectsByArea.containsKey(loc)) {
					continue;
				}
				for (CommonObject obj : objectsByArea.get(loc)) {
					if (changedList.contains(obj.getUUID())) {
						objSpawnIn.add(obj);
					}
				}
			}

			ObjectsSpawnPacket spawning = new ObjectsSpawnPacket();
			spawning.obj = objSpawnIn;
			c.send(spawning);

			ArrayList<UUID> objDespawn = new ArrayList<UUID>();
			for (CommonAreaLoc loc : lostAreas) {
				if (!objectsByArea.containsKey(loc)) {
					continue;
				}
				for (CommonObject obj : objectsByArea.get(loc)) {
					objDespawn.add(obj.getUUID());
				}
			}
			// TODO : Add any objects that have despawned in full view here. Newly dead etc.
			ObjectsDespawnPacket despawning = new ObjectsDespawnPacket();
			despawning.idList = objDespawn;
			c.send(despawning);

			// Push all animations and movement of seen objects to client.

			c.lastSeenAreas = seenAreas;
		}

		// Calculate next tick worth of combat and movement
		for (CommonObject obj : Server.getSession().getObjectStorage().getObjects()) {
			if (obj instanceof MovableObject) {
				MovableObject mobj = (MovableObject) obj;
				mobj.doAnimationTick();
				double pos = mobj.doMovementTick();
				Line l = mobj.getLine(pos);
				if (Double.isNaN(pos)) {
					continue;
				}
				ObjectMovementOnPathPacket packet = new ObjectMovementOnPathPacket(obj.getUUID(), pos);
				server.sendToAllObject(obj.getUUID(), packet);
				if (l == null) {
					continue;
				}
				ObjectLookTowardsPacket packet2 = new ObjectLookTowardsPacket(obj.getUUID(), l.getPoint(1));
				server.sendToAllObject(obj.getUUID(), packet2);

			}
		}
	}
}
