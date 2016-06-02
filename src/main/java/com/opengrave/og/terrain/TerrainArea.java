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

import java.io.*;
import java.util.ArrayList;

import com.opengrave.common.*;
import com.opengrave.common.world.CommonAreaLoc;
import com.opengrave.og.Util;
import com.opengrave.og.base.Wall;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.Node;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public class TerrainArea extends Node implements ThreadedLoading {

	private TerrainWorld world;
	private CommonAreaLoc locationInWorld;
	private ArrayList<Wall> wallList = new ArrayList<Wall>();
	private ArrayList<TerrainLayerNode> layers = new ArrayList<TerrainLayerNode>();
	private ArrayList<TerrainLiquidLayerNode> liquidLayer = new ArrayList<TerrainLiquidLayerNode>();
	private Matrix4f renderOffset = new Matrix4f();

	// private Location location = new Location();
	private boolean loaded = false;

	public TerrainArea(TerrainWorld terrainWorld, CommonAreaLoc tac) {
		this.world = terrainWorld;
		world.addChild(this);
		this.locationInWorld = tac;
		renderOffset.translate(new Vector3f((float) (tac.getX() * (TerrainLayer.size - 1)), (float) (tac.getY() * (TerrainLayer.size - 1)), 0f), renderOffset);
		LoadingThread.addLoadable(this);
	}

	@Override
	public void doRenderShadows(Matrix4f matrix, Shadow shadow) {
	}

	@Override
	public void doRenderForPicking(Matrix4f matrix) {

	}

	@Override
	public void doRender(Matrix4f matrix) {
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f matrix) {

	}

	@Override
	public void doUpdate(float delta) {

	}

	public void save() {
		String name = locationInWorld.getX() + ":" + locationInWorld.getY() + ".area";
		File f = new File(world.getDirectory(), name);
		System.out.println("Saving area : " + f);
		if (!f.isDirectory()) {
			File parent = f.getParentFile();
			if (!parent.isDirectory()) {
				parent.mkdirs();
			}
			synchronized (world) {

				try (OGOutputStream stream = new OGOutputStream(new FileOutputStream(f, false))) {

					synchronized (layers) {
						stream.writeInt(layers.size());
						for (TerrainLayerNode layer : layers) {
							layer.save(stream);
						}
					}
					stream.writeInt(0); // Keep binary compatibility with older worlds
					synchronized (wallList) {
						stream.writeInt(wallList.size());
						for (Wall wall : wallList) {
							wall.save(stream);
						}
					}
					synchronized (liquidLayer) {
						stream.writeInt(liquidLayer.size());
						for (TerrainLiquidLayerNode tll : liquidLayer) {
							tll.save(stream);
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

	public void load() {
	}

	/*
	 * public TerrainEditableVertex getVertexAt(int inmapx, int inmapy, int layer, boolean ignoreEdge) {
	 * synchronized (world) {
	 * synchronized (layers) {
	 * if (layers.size() <= layer) { return null; }
	 * TerrainLayerNode tl = layers.get(layer);
	 * return tl.layer.getVertexAt(inmapx, inmapy, ignoreEdge);
	 * }
	 * }
	 * }
	 */

	/*
	 * public TerrainLiquidEditableVertex getLiquidVertexAt(int inmapx, int inmapy, int layer, boolean ignoreEdge) {
	 * synchronized (world) {
	 * synchronized (liquidLayer) {
	 * if (liquidLayer.size() <= layer) { return null; }
	 * 
	 * TerrainLiquidLayerNode tll = liquidLayer.get(layer);
	 * return tll.layer.getVertexAt(inmapx, inmapy, ignoreEdge);
	 * }
	 * }
	 * }
	 */

	public Location relativeLocationOf(Location l) {
		Location l2 = new Location();
		l2.setLocation(l.getTileX() - (locationInWorld.getX() * 64l), l.getMinorX(), l.getTileY() - (locationInWorld.getY() * 64l), l.getMinorY());
		l2.setLayer(l.getLayer());
		return l2;
	}

	public void addWall(Wall wall) {
		synchronized (world) {
			synchronized (wallList) {
				wallList.add(wall);
				addChild(wall);
			}
		}
	}

	public void setAllObjectsRenderStyle(RenderStyle renderStyle) {
		synchronized (world) {
			// synchronized (objectList) {
			// for (BaseObject obj : objectList) {
			// obj.setRenderStyle(renderStyle);
			// }
			// }
			synchronized (wallList) {
				for (Wall wall : wallList) {
					wall.setRenderStyle(renderStyle);
				}
			}
			synchronized (layers) {
				for (TerrainLayerNode layer : layers) {
					layer.layer.setRenderStyle(renderStyle);
				}
			}
			synchronized (liquidLayer) {
				for (TerrainLiquidLayerNode layer : liquidLayer) {
					layer.layer.setRenderStyle(renderStyle);
				}
			}
		}

	}

	public boolean ownsLayer(TerrainLayer layer) {
		synchronized (world) {
			if (layer == null) {
				return false;
			}
			synchronized (layers) {
				for (TerrainLayerNode layer2 : layers) {
					if (layer2.layer.equals(layer)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean ownsLayer(TerrainLiquidLayer layer) {
		synchronized (world) {
			if (layer == null) {
				return false;
			}
			synchronized (liquidLayer) {
				for (TerrainLiquidLayerNode layer2 : liquidLayer) {
					if (layer2.layer.equals(layer)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getLayerCount() {
		synchronized (world) {
			synchronized (layers) {
				return layers.size();
			}
		}
	}

	public int getLiquidCount() {
		synchronized (world) {
			synchronized (liquidLayer) {
				return liquidLayer.size();
			}
		}
	}

	public void addLayer(boolean fill) {
		synchronized (world) {
			synchronized (layers) {
				int i = layers.size();
				TerrainLayerNode tl = new TerrainLayerNode(new TerrainLayer(world, locationInWorld, getLayerCount(), null), i);
				layers.add(tl);
				addChild(tl);
				if (!fill) {
					tl.layer.setAllTextures(-1);
				}
			}
		}
	}

	public void addLiquid(boolean fill) {
		synchronized (world) {
			synchronized (liquidLayer) {
				TerrainLiquidLayerNode tll = new TerrainLiquidLayerNode(new TerrainLiquidLayer(world, locationInWorld, getLiquidCount(), null));
				liquidLayer.add(tll);
				addChild(tll);
				if (!fill) {
					tll.layer.setAllTextures(-1);
				}
			}
		}
	}

	public TerrainLayerNode getLayer(int editingLayer) {
		synchronized (layers) {
			if (editingLayer >= layers.size()) {
				return null;
			}
			return layers.get(editingLayer);
		}
	}

	public TerrainLiquidLayerNode getLiquid(int leditingLayer) {
		synchronized (world) {
			synchronized (liquidLayer) {
				if (leditingLayer >= liquidLayer.size()) {
					addLiquid(false);
				}
				return liquidLayer.get(leditingLayer);
			}
		}
	}

	/*
	 * public boolean ownsObject(BaseObject object) {
	 * synchronized (objectList) {
	 * for (BaseObject obj : objectList) {
	 * if (obj.equals(object)) { return true; }
	 * }
	 * }
	 * return false;
	 * }
	 */

	public Location getLocation() {
		Location l = new Location();
		l.setLocation(locationInWorld.getX() * 63, 0, locationInWorld.getY() * 63, 0);
		return l;
	}

	/*
	 * public void getObjectsByIdentifier(ArrayList<BaseObject> objList, String ident) {
	 * synchronized (objectList) {
	 * for (BaseObject obj : objectList) {
	 * if (obj.getIdentifier().equals(ident)) {
	 * objList.add(obj);
	 * }
	 * }
	 * }
	 * 
	 * }
	 */

	/**
	 * 
	 * @param loc
	 *            Location in relative proportions.
	 * @return
	 */
	public float getHeightAt(Location loc) {
		synchronized (world) {
			synchronized (layers) {
				if (layers.size() <= loc.getLayer()) {
					return 0f;
				}
				TerrainLayerNode layer = layers.get(loc.getLayer());
				return layer.layer.getHeightAt(loc);

			}
		}
	}

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(getLocation(), null, null, null);
	}

	public CommonAreaLoc getAreaLoc() {
		return locationInWorld;
	}

	public void alter(int x, int y, int layer, TerrainLayerAlteration alter) {
		if (x < 0 || x >= TerrainLayer.size || y < 0 || y >= TerrainLayer.size) {
			throw new RuntimeException("Cannot be <0 or >63...  x:" + x + " y:" + y);
		}
		synchronized (world) {
			synchronized (layers) {
				if (layer < layers.size()) {
					TerrainLayerNode layerN = layers.get(layer);
					layerN.layer.alter(x, y, alter);
				}
			}
		}
	}

	public void alter(int x, int y, int layer, TerrainLiquidLayerAlteration alter) {
		if (x < 0 || x >= TerrainLayer.size || y < 0 || y >= TerrainLayer.size) {
			throw new RuntimeException("Cannot be <0 or >63...  x:" + x + " y:" + y);
		}
		synchronized (world) {
			synchronized (liquidLayer) {
				if (layer < liquidLayer.size()) {
					TerrainLiquidLayerNode layerN = liquidLayer.get(layer);
					layerN.layer.alter(x, y, alter);
				}
			}
		}
	}

	public void forceLiquidEdges(ArrayList<TerrainArea> connected) {
		for (TerrainArea area : connected) {
			CommonAreaLoc otherLoc = area.getAreaLoc();
			int offx = otherLoc.getX() - locationInWorld.getX();
			int offy = otherLoc.getY() - locationInWorld.getY();
			synchronized (liquidLayer) {
				for (int i = 0; i < liquidLayer.size(); i++) {
					if (area.getLiquidCount() <= i) {
						break;
					}
					TerrainLiquidLayer thisLayer = liquidLayer.get(i).layer;
					TerrainLiquidLayer thatLayer = area.getLiquid(i).layer;

					// Both layers have a matching liquid X layer
					if (offx == 1 && offy == 0) {
						// Directly to the right - take whole left edge as right

						for (int c = 0; c < 64; c++) {
							if (thisLayer.getTextureAt(63, c) != -1 && thatLayer.getTextureAt(0, c) != -1) {
								thisLayer.setLastLiquidFlow(64, c + 1, thatLayer.getCurrentFlowXAt(0, c), thatLayer.getCurrentFlowYAt(0, c));
							}
						}
					}
					if (offx == 0 && offy == 1) {
						// Directly below - take whole top edge as bottom
						for (int c = 0; c < 64; c++) {
							if (thatLayer.getTextureAt(c, 0) != -1 && thisLayer.getTextureAt(c, 63) != -1) {
								thisLayer.setLastLiquidFlow(c + 1, 64, thatLayer.getCurrentFlowXAt(c, 0), thatLayer.getCurrentFlowYAt(c, 0));
							}
						}
					}
					if (offx == 1 && offy == 1) {
						// Take bottom right as top left
						thisLayer.setLastLiquidFlow(64, 64, thatLayer.getCurrentFlowXAt(0, 0), thatLayer.getCurrentFlowYAt(0, 0));
					}
				}
			}
		}

	}

	@Override
	public void loadInThisThread() {
		// if (loaded) { return; }
		String name = locationInWorld.getX() + ":" + locationInWorld.getY() + ".area";
		File f = new File(world.getDirectory(), name);
		ArrayList<TerrainLayerNode> layers = new ArrayList<TerrainLayerNode>();
		ArrayList<Wall> walls = new ArrayList<Wall>();
		ArrayList<TerrainLiquidLayerNode> liquids = new ArrayList<TerrainLiquidLayerNode>();

		if (f.isFile()) {
			System.out.println("Reading area file : " + f);
			try (OGInputStream reader = new OGInputStream(new FileInputStream(f))) {
				int layerCount, wallCount, liquidCount;
				layerCount = reader.readInt();
				for (int i = 0; i < layerCount; i++) {
					TerrainLayerNode layer = new TerrainLayerNode(new TerrainLayer(world, locationInWorld, i, reader), i);
					layers.add(layer);
					addChild(layer);
				}
				reader.readInt(); // Throw away an int for object count. No longer stored here
				wallCount = reader.readInt();
				for (int i = 0; i < wallCount; i++) {
					Wall wall = Wall.createWall(reader);
					walls.add(wall);
					addChild(wall);
				}
				liquidCount = reader.readInt();
				for (int i = 0; i < liquidCount; i++) {
					TerrainLiquidLayerNode layer = new TerrainLiquidLayerNode(new TerrainLiquidLayer(world, locationInWorld, i, reader));
					liquids.add(layer);
					addChild(layer);
				}
			} catch (FileNotFoundException e) {
				new DebugExceptionHandler(e, name);
			} catch (IOException e) {
				new DebugExceptionHandler(e, name);
			}
		} else {
			System.out.println("Could not load area file : " + f);
		}

		if (layers.size() == 0) {
			TerrainLayerNode layer = new TerrainLayerNode(new TerrainLayer(world, locationInWorld, 0, null), 0);
			layers.add(layer);
			addChild(layer);
		}
		this.layers = layers;
		this.liquidLayer = liquids;
		this.wallList = walls;
		loaded = true;
	}

	@Override
	public boolean isLoaded() {
		return loaded;
	}

	@Override
	public void setLoaded(boolean b) {
		loaded = b;

	}
}
