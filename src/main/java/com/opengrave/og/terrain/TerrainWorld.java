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
package com.opengrave.og.terrain;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.opengrave.common.config.Config;
import com.opengrave.common.world.CommonAreaLoc;
import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.base.BoringWall;
import com.opengrave.og.base.Wall;
import com.opengrave.og.engine.Camera;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.Node;
import com.opengrave.og.engine.Surface;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.resources.Texture;
import com.opengrave.og.resources.TextureAtlas;
import com.opengrave.og.util.Matrix4f;

public class TerrainWorld extends Node {

	enum LiquidType {
		SEA, LAVA, SLIME
	};

	enum LightType {
		DAYCYCLE, PERMADAY, HELLISH
	};

	LightType light = LightType.PERMADAY;
	LiquidType liquid = LiquidType.SEA;
	Location last = null;
	String worldName = "";
	String directory = "";
	private ConcurrentHashMap<CommonAreaLoc, TerrainArea> areasLoaded = new ConcurrentHashMap<CommonAreaLoc, TerrainArea>();
	private TextureAtlas terrainAtlas, terrainNormAtlas, liquidAtlas, liquidNormAtlas;
	private ArrayList<TerrainSurface> surfaces = new ArrayList<TerrainSurface>();

	public TerrainWorld(String worldName) {
		if (worldName.matches("/[^a-zA-Z0-9]/")) {
			System.out.println("Error with world name \"" + worldName + "\" a-z and 0-9 only!");
			worldName = "overworld";
		}
		this.worldName = worldName;
		touchWorldDir(worldName);
		Config config = new Config(worldName + "/world.info");
		String lightName = config.getString("light", "permaday");
		if (lightName.equalsIgnoreCase("daycycle")) {
			light = LightType.DAYCYCLE;
		} else if (lightName.equalsIgnoreCase("hellish")) {
			light = LightType.HELLISH;
		} else {
			light = LightType.PERMADAY;
		}
		String liquidName = config.getString("watertype", "sea");
		if (liquidName.equalsIgnoreCase("lava")) {
			liquid = LiquidType.LAVA;
		} else if (liquidName.equalsIgnoreCase("slime")) {
			liquid = LiquidType.SLIME;
		} else {
			liquid = LiquidType.SEA;
		}
		ArrayList<String> texturesList = new ArrayList<String>();
		texturesList.add("blank");
		texturesList.add("tex/smooth.png");
		texturesList.add("tex/coarse.png");
		texturesList.add("tex/bricks.png");
		this.terrainAtlas = (TextureAtlas) Resources.loadTextures(texturesList);
		ArrayList<String> normList = new ArrayList<String>();
		normList.add("tex/flat.png");
		normList.add("tex/smooth-norm.png");
		normList.add("tex/coarse-norm.png");
		normList.add("tex/bricks-norm.png");
		this.terrainNormAtlas = (TextureAtlas) Resources.loadTextures(normList);
		ArrayList<String> texturesList2 = new ArrayList<String>();
		texturesList2.add("blank");
		texturesList2.add("tex/cloud.png");
		texturesList2.add("tex/smooth.png");
		this.liquidAtlas = (TextureAtlas) Resources.loadTextures(texturesList2);
		ArrayList<String> normList2 = new ArrayList<String>();
		normList2.add("tex/flat.png");
		normList2.add("tex/cloud-norm.png");
		normList2.add("tex/smooth-norm.png");
		this.liquidNormAtlas = (TextureAtlas) Resources.loadTextures(normList2);
	}

	private void touchWorldDir(String name) {
		File f = new File(MainThread.cache, name);
		f.mkdirs();
	}

	public Texture getLiquidTextures() {
		return liquidAtlas;
	}

	@Override
	public void doUpdate(float delta) {
		// if(cam.equals(last)){
		// last = cam.clone();
		// int mapx = (int)(Math.floor(lastcx/((TerrainArea.size-1)*10f)));
		// int mapy = (int)(Math.floor(lastcy/((TerrainArea.size-1)*10f)));
		Camera cam = context.getCam();
		float scale = 1f * (TerrainLayer.size - 1); // The last element joins
													// with the first of the
													// next grid... ignore the
													// one

		int mapx = (int) Math.floor(cam.getLocation().getTileX() / scale);
		int mapy = (int) Math.floor(cam.getLocation().getTileY() / scale);
		// children.clear();
		for (int x = -1; x < 2; x++) {
			for (int y = -1; y < 2; y++) {
				loadAreaForce(mapx + x, mapy + y);
			}
		}
		// }
	}

	@Override
	public void doRender(Matrix4f parent) {
		Camera cam = context.getCam();
		float scale = 1f * (TerrainLayer.size - 1);
		int mapx = (int) Math.floor(cam.getLocation().getTileX() / scale);
		int mapy = (int) Math.floor(cam.getLocation().getTileY() / scale);
		for (int x = -1; x < 2; x++) {
			for (int y = -1; y < 2; y++) {
				TerrainArea area = loadAreaForce(mapx + x, mapy + y);
				ArrayList<TerrainArea> connected = new ArrayList<TerrainArea>();
				synchronized (this) {
					for (TerrainArea area2 : areasLoaded.values()) {
						int ax = area2.getAreaLoc().getX(), ay = area2.getAreaLoc().getY();
						if (ax <= x + 1 && ax >= x && ay <= y + 1 && ay >= y) {
							connected.add(area2);
						}
					}
					area.forceLiquidEdges(connected);
				}
			}
		}
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f matrix) {
	}

	@Override
	public void doRenderForPicking(Matrix4f matrix) {
	}

	@Override
	public void doRenderShadows(Matrix4f matrix, Shadow shadow) {
	}

	public TerrainArea loadAreaForce(int x, int y) {
		TerrainArea a = loadArea(x, y);
		addChild(a);
		a.load();
		return a;
	}

	public synchronized TerrainArea loadArea(int x, int y) {
		// System.out.println("Loading "+x+" "+y);
		if (hasAreaLoaded(x, y)) {
			return getArea(x, y);
		}
		// System.out.println("Creating new");
		CommonAreaLoc tac = new CommonAreaLoc(x, y);
		TerrainArea ta = new TerrainArea(this, tac);
		areasLoaded.put(tac, ta);
		return ta;

	}

	public synchronized TerrainArea getArea(int x, int y) {
		CommonAreaLoc tac = getAreaLocation(x, y);
		if (tac == null) {
			return null;
		}

		return areasLoaded.get(tac);

	}

	public synchronized boolean hasAreaLoaded(int x, int y) {
		return getAreaLocation(x, y) != null;
	}

	/*
	 * public TerrainEditableVertex getVertexAt(int x, int y, int layer, boolean ignoreEdge) {
	 * int mapx = (int) (Math.floor(x / ((TerrainLayer.size - 1) * 1f)));
	 * int mapy = (int) (Math.floor(y / ((TerrainLayer.size - 1) * 1f)));
	 * CommonAreaLoc tac = getAreaLocation(mapx, mapy);
	 * if (tac == null) {
	 * System.out.println("Couldn't get TerrainArea location with " + mapx + ":" + mapy);
	 * return null;
	 * }
	 * int inmapx = x - (mapx * (TerrainLayer.size - 1));
	 * int inmapy = y - (mapy * (TerrainLayer.size - 1));
	 * 
	 * synchronized (this) {
	 * synchronized (areasLoaded) {
	 * TerrainArea ta = areasLoaded.get(tac);
	 * return ta.getVertexAt(inmapx, inmapy, layer, ignoreEdge);
	 * }
	 * }
	 * }
	 */

	/*
	 * public synchronized TerrainLiquidEditableVertex getLiquidVertexAt(int x, int y, int layer, boolean ignoreEdge) {
	 * int mapx = (int) (Math.floor(x / ((TerrainLayer.size - 1) * 1f)));
	 * int mapy = (int) (Math.floor(y / ((TerrainLayer.size - 1) * 1f)));
	 * CommonAreaLoc tac = getAreaLocation(mapx, mapy);
	 * if (tac == null) {
	 * System.out.println("Couldn't get TerrainArea location with " + mapx + ":" + mapy);
	 * return null;
	 * }
	 * int inmapx = x - (mapx * (TerrainLayer.size - 1));
	 * int inmapy = y - (mapy * (TerrainLayer.size - 1));
	 * TerrainArea ta;
	 * ta = areasLoaded.get(tac);
	 * return ta.getLiquidVertexAt(inmapx, inmapy, layer, ignoreEdge);
	 * }
	 */

	public synchronized CommonAreaLoc getAreaLocation(int x, int y) {
		for (CommonAreaLoc tac : areasLoaded.keySet()) {
			if (tac.isEquals(x, y)) {
				return tac;
			}
		}
		return null;
	}

	public TextureAtlas getTextures() {
		return terrainAtlas;
	}

	public synchronized void saveAll() {
		for (TerrainArea ta : areasLoaded.values()) {
			ta.save();
		}
	}

	public synchronized TerrainArea getArea(CommonAreaLoc cal) {
		return getArea(cal.getX(), cal.getY());
	}

	// public CommonAreaLoc getAreaLocationOfLocation(Location l) {
	// int mapx = (int) (Math.floor(l.getFullXAsFloat() / ((TerrainLayer.size - 1) * 1f)));
	// int mapy = (int) (Math.floor(l.getFullYAsFloat() / ((TerrainLayer.size - 1) * 1f)));
	// return new CommonAreaLoc(mapx, mapy);
	// }

	public synchronized TerrainArea getAreaOfLocation(Location l) {
		int mapx = (int) (Math.floor(l.getFullXAsFloat() / ((TerrainLayer.size) * 1f)));
		int mapy = (int) (Math.floor(l.getFullYAsFloat() / ((TerrainLayer.size) * 1f)));
		return getArea(mapx, mapy);
	}

	public synchronized Wall createWallAt(Location l) {
		Wall wall = new BoringWall();
		TerrainArea ta = getAreaOfLocation(l);
		ta.addWall(wall);
		wall.setLocation(ta.relativeLocationOf(l));
		return wall;
	}

	public synchronized void setAllObjectsRenderStyle(RenderStyle renderStyle) {
		for (TerrainArea ta : areasLoaded.values()) {
			ta.setAllObjectsRenderStyle(renderStyle);
		}
	}

	public File getDirectory() {
		return new File(MainThread.cache, worldName);
	}

	/*
	 * public Location getLocationOf(BaseObject object) {
	 * synchronized (this) {
	 * synchronized (areasLoaded) {
	 * for (TerrainArea area : areasLoaded.values()) {
	 * if (area.ownsObject(object)) { return object.getLocation().add(area.getLocation()); }
	 * }
	 * }
	 * }
	 * return null;
	 * }
	 */

	/**
	 * Returns all objects in loaded memory with given identifier
	 * 
	 * @param ident
	 * @return
	 */
	/*
	 * public ArrayList<BaseObject> getObjectsByIdentifier(String ident) {
	 * ArrayList<BaseObject> objList = new ArrayList<BaseObject>();
	 * synchronized (this) {
	 * synchronized (areasLoaded) {
	 * for (TerrainArea area : areasLoaded.values()) {
	 * area.getObjectsByIdentifier(objList, ident);
	 * }
	 * }
	 * }
	 * return objList;
	 * }
	 */

	public synchronized float getHeightAt(Location loc) {
		TerrainArea area = getAreaOfLocation(loc);
		if (area == null) {
			return 0f;
		}
		return area.getHeightAt(area.relativeLocationOf(loc));
	}

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(new Location(), null, null, context);
	}

	public String getFileName() {
		return worldName;
	}

	public TextureAtlas getNormalTextures() {
		return terrainNormAtlas;
	}

	public TextureAtlas getLiquidNormTextures() {
		return liquidNormAtlas;
	}

	public synchronized void alter(int x, int y, int layer, TerrainLayerAlteration alter) {
		// TODO Done edges - need to care about corners (4 layers as one vertex)
		int mapx = (int) (Math.floor(x / ((TerrainLayer.size - 1) * 1f)));
		int mapy = (int) (Math.floor(y / ((TerrainLayer.size - 1) * 1f)));
		CommonAreaLoc mloc = getAreaLocation(mapx, mapy);
		TerrainArea area = getArea(mloc);
		if (area == null) {
			return;
		}
		int size = TerrainLayer.size - 1;
		x = x - (mapx * size);
		y = y - (mapy * size);
		if (x == 0) {
			// Also affect the next area over
			CommonAreaLoc loc = new CommonAreaLoc(mapx - 1, mapy);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(TerrainLayer.size - 1, y, layer, alter);
		}
		if (x == TerrainLayer.size - 1) {
			CommonAreaLoc loc = new CommonAreaLoc(mapx + 1, mapy);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(0, y, layer, alter);
		}
		if (y == 0) {
			CommonAreaLoc loc = new CommonAreaLoc(mapx, mapy - 1);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(x, TerrainLayer.size - 1, layer, alter);
		}
		if (y == TerrainLayer.size - 1) {
			CommonAreaLoc loc = new CommonAreaLoc(mapx, mapy + 1);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(x, 0, layer, alter);
		}
		area.alter(x, y, layer, alter);
	}

	public synchronized void alter(int x, int y, int layer, TerrainLiquidLayerAlteration alter) {
		// TODO Done edges - need to care about corners (4 layers as one vertex)
		int mapx = (int) (Math.floor(x / ((TerrainLayer.size - 1) * 1f)));
		int mapy = (int) (Math.floor(y / ((TerrainLayer.size - 1) * 1f)));
		CommonAreaLoc mloc = getAreaLocation(mapx, mapy);
		TerrainArea area = getArea(mloc);
		if (area == null) {
			return;
		}
		int size = TerrainLayer.size - 1;
		x = x - (mapx * size);
		y = y - (mapy * size);
		if (x == 0) {
			// Also affect the next area over
			CommonAreaLoc loc = new CommonAreaLoc(mapx - 1, mapy);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(TerrainLayer.size - 1, y, layer, alter);
		}
		if (x == TerrainLayer.size - 1) {
			CommonAreaLoc loc = new CommonAreaLoc(mapx + 1, mapy);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(0, y, layer, alter);
		}
		if (y == 0) {
			CommonAreaLoc loc = new CommonAreaLoc(mapx, mapy - 1);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(x, TerrainLayer.size - 1, layer, alter);
		}
		if (y == TerrainLayer.size - 1) {
			CommonAreaLoc loc = new CommonAreaLoc(mapx, mapy + 1);
			TerrainArea areaAlso = getArea(loc);
			areaAlso.alter(x, 0, layer, alter);
		}
		area.alter(x, y, layer, alter);
	}

	public Surface getSurface(int i) {
		if (i < 0) {
			return null;
		}
		while (i >= surfaces.size()) {
			int a = surfaces.size();
			TerrainSurface ts = new TerrainSurface(this, a);
			surfaces.add(ts);
		}
		return surfaces.get(i);
	}

}
