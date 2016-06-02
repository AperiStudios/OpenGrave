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
package com.opengrave.og.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.opengrave.common.world.*;
import com.opengrave.og.Util;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.terrain.TerrainArea;
import com.opengrave.og.terrain.TerrainWorld;
import com.opengrave.og.util.Matrix4f;

public class ObjectStorageNode extends Node {
	HashMap<CommonAreaLoc, ArrayList<BaseObject>> objects = new HashMap<CommonAreaLoc, ArrayList<BaseObject>>();
	HashMap<CommonAreaLoc, EmptyNode> areas = new HashMap<CommonAreaLoc, EmptyNode>();
	ArrayList<CommonAreaLoc> lastLoaded = new ArrayList<CommonAreaLoc>();

	Matrix4f matrix = new Matrix4f();

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(new Location(), null, null, context);
	}

	@Override
	public void doUpdate(float delta) {
	}

	@Override
	public void doRender(Matrix4f parent) {
	}

	@Override
	public void doRenderShadows(Matrix4f parent, Shadow shadow) {
	}

	@Override
	public void doRenderForPicking(Matrix4f parent) {
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f parent) {
	}

	public BaseObject createObject(CommonObject cobj) {
		removeObject(cobj.getUUID());
		BaseObject obj = BaseObject.createObject(cobj);
		MaterialList mList = cobj.getMaterialList();
		obj.setMaterialList(mList);
		obj.setLocation(cobj.getLocation());
		addObject(obj);
		return obj;
	}

	/**
	 * Used in TerrainEditor only, Bypasses the need for a Server, Connection, and Servers control over what areas you can see
	 * 
	 * @param cw
	 * @param tw
	 */
	public void fillFrom(CommonWorld cw, TerrainWorld tw) {
		// Will end up holding a list of any newly unloaded areas.
		ArrayList<CommonAreaLoc> areasUnloaded = new ArrayList<CommonAreaLoc>();
		areasUnloaded.addAll(lastLoaded);
		// Will end up holding a list of any newly loaded areas.
		ArrayList<CommonAreaLoc> areasLoaded = new ArrayList<CommonAreaLoc>();
		// Will end up holding next frames "lastLoaded"
		ArrayList<CommonAreaLoc> nextLoaded = new ArrayList<CommonAreaLoc>();
		for (Node node : tw.children) {
			if (node instanceof TerrainArea) {
				TerrainArea tnode = (TerrainArea) node;
				CommonAreaLoc areaLoc = tnode.getAreaLoc();
				if (areasUnloaded.contains(areaLoc)) {
					areasUnloaded.remove(areaLoc);
				}
				if (!lastLoaded.contains(areaLoc)) {
					areasLoaded.add(areaLoc);
				}
				nextLoaded.add(areaLoc);
			}
		}
		for (CommonAreaLoc cal : areasLoaded) {
			CommonArea ca = cw.getArea(cal);
			if (ca == null) {
				continue;
			}
			for (CommonObject co : ca.getAllObjects()) {
				System.out.println("Adding Object from CommonWorld " + co);
				createObject(co);
			}
		}
		for (CommonAreaLoc cal : areasUnloaded) {
			CommonArea ca = cw.getArea(cal);
			if (ca == null) {
				continue;
			}
			for (CommonObject co : ca.getAllObjects()) {
				// TODO - remove by CommonObject reference. Not as easy as I might have hoped.
				removeObject(co.getUUID());
			}
		}
		lastLoaded = nextLoaded;
	}

	public void setLoadedAreas(ArrayList<CommonAreaLoc> loc) {
		ArrayList<CommonAreaLoc> areasUnloaded = new ArrayList<CommonAreaLoc>();
		areasUnloaded.addAll(lastLoaded);
		for (CommonAreaLoc location : loc) {
			if (areasUnloaded.contains(location)) {
				areasUnloaded.remove(location);
			}
		}
		for (CommonAreaLoc location : areasUnloaded) {
			removeArea(location);
		}

	}

	public void removeArea(CommonAreaLoc location) {
		for (BaseObject obj : getObjects(location)) {
			removeObjectFromArea(location, obj);
		}
	}

	public void removeObject(UUID id) {
		synchronized (objects) {
			for (CommonAreaLoc loc : objects.keySet()) {
				ArrayList<BaseObject> list = objects.get(loc);
				ArrayList<BaseObject> copy = new ArrayList<BaseObject>();
				synchronized (list) {
					copy.addAll(list);
				}
				for (BaseObject obj : copy) {
					if (obj.getUUID().compareTo(id) == 0) {
						removeObjectFromArea(loc, obj);
					}
				}

			}
		}
	}

	private void removeObjectOnly(BaseObject obj) {
		synchronized (objects) {
			for (CommonAreaLoc loc : objects.keySet()) {
				Node n = getAreaNode(loc);
				n.removeChild(obj);
			}
		}
	}

	/**
	 * Takes a world location and instantly moves object. Corrects the area it's attatched to in the process
	 * 
	 * @param l
	 */
	public void setObjectLocation(Location l, UUID id) {
		// removeObject(id);
		CommonAreaLoc loc = CommonWorld.getAreaLocFor(l);
		Node n = getAreaNode(loc);
		CommonLocation cloc = CommonWorld.negateAreaFromLocation(loc, l);
		BaseObject obj = getObject(id);
		removeObjectOnly(obj);
		obj.setLocation(cloc);
		n.addChild(obj);
	}

	public void addObject(BaseObject obj) {
		synchronized (objects) {
			CommonAreaLoc loc = CommonWorld.getAreaLocFor(obj.getLocation());
			Node n = getAreaNode(loc);
			CommonLocation cloc = CommonWorld.negateAreaFromLocation(loc, obj.getLocation());
			obj.setLocation(cloc);
			if (!objects.containsKey(loc)) {
				objects.put(loc, new ArrayList<BaseObject>());
			}
			synchronized (objects.get(loc)) {
				objects.get(loc).add(obj);
			}
			n.addChild(obj);
		}

	}

	public void removeObjectFromArea(CommonAreaLoc location, BaseObject obj) {
		synchronized (objects) {
			synchronized (objects.get(location)) {
				this.objects.get(location).remove(obj);
			}
			Node n = getAreaNode(location);
			n.removeChild(obj);
		}
	}

	public BaseObject getObject(UUID id) {
		synchronized (objects) {
			for (CommonAreaLoc loc : objects.keySet()) {
				System.out.println("Checking for objects in " + loc.getX() + " " + loc.getY());
				synchronized (objects.get(loc)) {
					for (BaseObject obj : objects.get(loc)) {
						if (obj.getUUID().compareTo(id) == 0) {
							return obj;
						}
					}
				}
			}
		}
		return null;
	}

	public ArrayList<BaseObject> getObjects(CommonAreaLoc location) {
		ArrayList<BaseObject> objectList = new ArrayList<BaseObject>();
		synchronized (objects) {
			if (!this.objects.containsKey(location)) {
				return objectList;
			}
			if (this.objects.get(location) == null) {
				return objectList;
			}
			synchronized (objects.get(location)) {
				for (BaseObject obj : this.objects.get(location)) {
					objectList.add(obj);
				}
			}
		}
		return objectList;
	}

	private Node getAreaNode(CommonAreaLoc loc) {
		if (areas.containsKey(loc)) {
			return areas.get(loc);
		}
		EmptyNode n = new EmptyNode(loc);
		areas.put(loc, n);
		addChild(n);
		return n;
	}

}
