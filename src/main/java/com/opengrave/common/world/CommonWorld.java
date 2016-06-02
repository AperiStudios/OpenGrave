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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.ModSession;
import com.opengrave.common.pathing.NavigationMesh;
import com.opengrave.common.pathing.PathingArea;
import com.opengrave.common.pathing.Point;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.Location;

public class CommonWorld {

	public static final byte actionWalkable = 0b1, actionShootable = 0b10, actionTeleportUp = 0b100, actionTeleportDown = 0b1000;
	String fileName;
	private boolean loaded;
	protected NavigationMesh navMesh = new NavigationMesh();
	private ConcurrentHashMap<CommonAreaLoc, CommonArea> areas = new ConcurrentHashMap<CommonAreaLoc, CommonArea>();
	private ConcurrentHashMap<String, ArrayList<CommonWayPoint>> waypoints = new ConcurrentHashMap<String, ArrayList<CommonWayPoint>>();

	public CommonWorld(String string) {
		fileName = string;
		// loadInThread();
	}

	public File getDirectory() {
		return new File(MainThread.cache, fileName);
	}

	public void save() {
		File f = new File(getDirectory(), "collision.data");
		synchronized (areas) {
			if (f.isFile() || !f.exists()) {
				try (OGOutputStream out = new OGOutputStream(new FileOutputStream(f))) {
					out.writeInt(areas.size());
					for (CommonAreaLoc cal : areas.keySet()) {
						CommonArea ca = areas.get(cal);
						out.writeInt(cal.getX());
						out.writeInt(cal.getY());
						ArrayList<CommonObject> objs = ca.getAllObjects();
						out.writeInt(objs.size());
						for (CommonObject co : objs) {
							out.writeBinaryNode(co.getData());
						}

					}
					out.writeInt(navMesh.polygonList.size());
					for (PathingArea area : navMesh.polygonList) {
						out.writeInt(area.getPoints().size());
						for (Point point : area.getPoints()) {
							out.writeDouble(point.getX());
							out.writeDouble(point.getY());
							out.writeInt(point.getZ());
						}

					}
				} catch (FileNotFoundException e) {
					new DebugExceptionHandler(e);
				} catch (IOException e) {
					new DebugExceptionHandler(e);
				}
			}
		}
	}

	public void loadInThread(ModSession sess) {
		CommonWorldLoader cwl = new CommonWorldLoader(this, sess);
		Thread t = new Thread(cwl);
		t.start();
	}

	public void setLoaded() {
		loaded = true;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public CommonArea getArea(int x, int y) {
		CommonAreaLoc loc = new CommonAreaLoc(x, y);
		return getArea(loc);
	}

	public static CommonAreaLoc getAreaLocFor(CommonLocation cloc) {
		float aX = cloc.getTileX() / 64f;
		float aY = cloc.getTileY() / 64f;
		int iX = (int) Math.floor(aX);
		int iY = (int) Math.floor(aY);
		return new CommonAreaLoc(iX, iY);
	}

	public CommonArea getArea(CommonAreaLoc loc) {
		if (!loaded) {
			return null;
		}
		synchronized (areas) {
			if (!areas.containsKey(loc)) {
				return null;
			}
			return areas.get(loc);
		}
	}

	public String getName() {
		return fileName;
	}

	public void addArea(int areaX, int areaY, CommonArea ca) {
		CommonAreaLoc cal = new CommonAreaLoc(areaX, areaY);
		synchronized (areas) {
			areas.put(cal, ca);
		}

	}

	public CommonArea newArea(CommonAreaLoc cal) {
		synchronized (areas) {
			areas.put(cal, new CommonArea());
			return areas.get(cal);

		}
	}

	public void putObjectInArea(CommonObject obj) {

	}

	public CommonObject createObjectAt(CommonLocation l, Type type, String model, MaterialList mat) {
		System.out.println("Creating '" + model + "' at " + l);
		CommonObject object = new CommonObject("unknown", type, model, mat, l);
		CommonAreaLoc loc = getAreaLocFor(l);
		System.out.println(loc.getX() + " " + loc.getY());
		CommonArea ca = getArea(loc);
		if (ca == null) {
			ca = new CommonArea();
			this.addArea(loc.getX(), loc.getY(), ca);
		}
		ca.addObject(object);
		return object;
	}

	public ArrayList<CommonArea> getAreas() {
		ArrayList<CommonArea> newList = new ArrayList<CommonArea>();
		if (loaded) {
			synchronized (areas) {
				for (CommonArea area : areas.values()) {
					newList.add(area);
				}
			}
		}
		return newList;
	}

	public CommonObject getObject(UUID id) {
		if (loaded) {
			synchronized (areas) {
				for (CommonArea area : areas.values()) {
					CommonObject obj = area.getObject(id);
					if (obj != null) {
						return obj;
					}
				}
			}
		}
		return null;
	}

	public static CommonLocation negateAreaFromLocation(CommonAreaLoc area, Location loc) {
		CommonLocation nL = new CommonLocation(loc);
		nL.setTileX(nL.getTileX() - (area.getX() * 63));
		nL.setTileY(nL.getTileY() - (area.getY() * 63));
		return nL;
	}

	public NavigationMesh getNavMesh() {
		return navMesh;
	}

}
