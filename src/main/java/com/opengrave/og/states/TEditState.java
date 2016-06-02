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
package com.opengrave.og.states;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.pathing.PathFinderPolygonAStar;
import com.opengrave.common.pathing.Point;
import com.opengrave.common.world.*;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.og.MainThread;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.base.Wall;
import com.opengrave.og.engine.*;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.*;
import com.opengrave.og.input.InputHeldEvent;
import com.opengrave.og.input.MouseButtonRenderableEvent;
import com.opengrave.og.input.MouseRenderableDragEvent;
import com.opengrave.og.input.MouseRenderableHoverEvent;
import com.opengrave.og.light.DayCycleSkyLight;
import com.opengrave.og.models.DAEFile;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.terrain.*;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class TEditState extends BaseState implements EventListener {

	public ArrayList<Point> nextPoly = new ArrayList<Point>();

	public Vector4f col = new Vector4f(1f, 0f, 0f, 1f), lcol = new Vector4f(1f, 0f, 0f, 1f);
	private TerrainWorld tw;
	private CommonWorld cw;
	private ObjectStorageNode objects;
	// protected TerrainArea cTa = null;
	protected TerrainSelector ts = null;
	protected TerrainPolygonMesh tmesh = null;
	protected TerrainPath tpath = null;

	public int texture = 0, ltexture = 0;
	public float height = 0f, lheight = 0f;

	int lastCollisionTest = 0;

	SceneView editView, objectView;

	FlyByCamera editCam = new FlyByCamera();
	BoundingBoxCamera objectCam = new BoundingBoxCamera();

	TextButton objectModelFile, objectLook, objectMaterialCol, objectMaterialModel;
	CheckButton terrainColour, terrainTexture, terrainHeight, liquidColour, liquidTexture, liquidHeight, liquidFlow, polygonPoints, polygonPoly, polygonTest;
	NumberRoller terrainTextureValue, terrainFloorNumber, liquidTextureValue, liquidLayerNumber, particleCount, objectMaterialNumber;
	FloatNumberRoller terrainHeightValue, wallAngle, liquidHeightValue, liquidFlowX, liquidFlowY, particleRespawnRate;
	TextInput objectMaterialTexture, objectMaterialTextureData;

	VectorInput3 objectPos, wallPos, particleMinPos, particleMaxPos, particleMinDir, particleMaxDir, particleGravity, particleColourMin, particleColourMax,
			objectAngle;

	TerrainLayer lastLayer;
	TerrainLiquidLayer lastLiquid;

	Image terrainImageSample, liquidImageSample;
	TextButton save, load, menu;
	StringRoller chooser;
	VerticalContainer terrain, wall, object, objectAdd, liquid, particle, polygon;
	ArrayList<ColourButton> cBs = new ArrayList<ColourButton>();
	ArrayList<ColourButton> tBs = new ArrayList<ColourButton>();

	Wall editingWall = null;
	private BaseObject editingObject, editingObjectCopy;
	private int editingLayer, leditingLayer;
	private RootNode objectViewNode;
	private TextButton objectStatic;
	private TextButton objectAnim;
	private TextButton objectParticle;

	ArrayList<Point> pathPoints = new ArrayList<Point>();

	public boolean isTerrainEdit() {
		return chooser.getString().equalsIgnoreCase("Edit Floor");
	}

	public boolean isWallEdit() {
		return chooser.getString().equalsIgnoreCase("Edit Wall");
	}

	public boolean isWallAdd() {
		return chooser.getString().equalsIgnoreCase("Add Wall");
	}

	public boolean isObjectAdd() {
		return chooser.getString().equalsIgnoreCase("Add Object");
	}

	public boolean isObjectEdit() {
		return chooser.getString().equalsIgnoreCase("Edit Objects");
	}

	public boolean isLiquidEdit() {
		return chooser.getString().equalsIgnoreCase("Edit Liquid");
	}

	public boolean isPolygonEdit() {
		return chooser.getString().equalsIgnoreCase("Edit Navigation Mesh");
	}

	@Override
	public void start() {
		MainThread.newSession();
		EventDispatcher.addHandler(this);
		tw = new TerrainWorld("overworld");
		cw = new CommonWorld("overworld");
		cw.loadInThread(MainThread.getSession());
		ts = new TerrainSelector();
		tmesh = new TerrainPolygonMesh();
		tpath = new TerrainPath();
		objects = new ObjectStorageNode();
		GUIXML screenFile = new GUIXML("gui/terrain.xml");
		screen = screenFile.getGUI();

		editView = (SceneView) screen.getElementById("editView");

		RootNode node = new RootNode();
		node.addChild(tw);
		node.addChild(ts);
		node.addChild(tpath);
		node.addChild(objects);
		node.addChild(tmesh);
		node.setSkyLight(new DayCycleSkyLight(MainThread.config.getInteger("shadowSize", 1024)));
		// PointLightNode light = new PointLightNode();
		// light.setColour(new Vector4f(1f, 0f, 0f, 1f));
		Location lightLoc = new Location();
		lightLoc.setZ(4f);
		// light.setLocation(lightLoc);

		// node.addChild(light);
		// node.addChild(ts);
		editView.setRenderView(new RenderView(node, editCam));

		objectView = (SceneView) screen.getElementById("objectview");

		objectViewNode = new RootNode();
		objectView.setRenderView(new RenderView(objectViewNode, objectCam));

		liquidColour = (CheckButton) screen.getElementById("setliquidcolour");
		liquidTexture = (CheckButton) screen.getElementById("setliquidtexture");
		liquidHeight = (CheckButton) screen.getElementById("setliquidheight");
		liquidTextureValue = (NumberRoller) screen.getElementById("numberliquidtex");
		liquidImageSample = (Image) screen.getElementById("imageliquid");
		liquidLayerNumber = (NumberRoller) screen.getElementById("liquidnumber");
		liquidHeightValue = (FloatNumberRoller) screen.getElementById("numberliquidheight");
		liquidFlow = (CheckButton) screen.getElementById("setflow");
		liquidFlowX = (FloatNumberRoller) screen.getElementById("numberliquidflowx");
		liquidFlowY = (FloatNumberRoller) screen.getElementById("numberliquidflowy");

		particleCount = (NumberRoller) screen.getElementById("count");
		particleGravity = (VectorInput3) screen.getElementById("gravity");
		particleMinPos = (VectorInput3) screen.getElementById("minpos");
		particleMaxPos = (VectorInput3) screen.getElementById("maxpos");
		particleMinDir = (VectorInput3) screen.getElementById("mindir");
		particleMaxDir = (VectorInput3) screen.getElementById("maxdir");
		particleRespawnRate = (FloatNumberRoller) screen.getElementById("respawnrate");
		particleColourMin = (VectorInput3) screen.getElementById("colmin");
		particleColourMax = (VectorInput3) screen.getElementById("colmax");

		terrainColour = (CheckButton) screen.getElementById("setcolour");
		terrainTexture = (CheckButton) screen.getElementById("settexture");
		terrainHeight = (CheckButton) screen.getElementById("setheight");
		terrainTextureValue = (NumberRoller) screen.getElementById("numbertex");
		terrainImageSample = (Image) screen.getElementById("imagetex");
		terrainFloorNumber = (NumberRoller) screen.getElementById("floornumber");
		terrainHeightValue = (FloatNumberRoller) screen.getElementById("numberheight");

		wallPos = (VectorInput3) screen.getElementById("wallpos");
		wallAngle = (FloatNumberRoller) screen.getElementById("wallangle");

		objectLook = (TextButton) screen.getElementById("objectlook");
		objectPos = (VectorInput3) screen.getElementById("objectpos");
		objectAngle = (VectorInput3) screen.getElementById("objectangle");
		objectModelFile = (TextButton) screen.getElementById("objectmodelfile");
		objectMaterialNumber = (NumberRoller) screen.getElementById("objectmaterialnumber");
		objectMaterialCol = (TextButton) screen.getElementById("objectmaterialcol");
		objectStatic = (TextButton) screen.getElementById("objectstatic");
		objectAnim = (TextButton) screen.getElementById("objectanim");
		objectParticle = (TextButton) screen.getElementById("objectparticle");

		objectMaterialTexture = (TextInput) screen.getElementById("objectmaterialtex");
		objectMaterialTextureData = (TextInput) screen.getElementById("objectmaterialtexdata");
		objectMaterialModel = (TextButton) screen.getElementById("objectmaterialmodel");

		polygonPoints = (CheckButton) screen.getElementById("polygonpoints");
		polygonPoly = (CheckButton) screen.getElementById("polygonpoly");
		polygonTest = (CheckButton) screen.getElementById("polygontest");

		chooser = (StringRoller) screen.getElementById("chooser");
		terrain = (VerticalContainer) screen.getElementById("flooroptions");
		wall = (VerticalContainer) screen.getElementById("walloptions");
		object = (VerticalContainer) screen.getElementById("objectoptions");
		objectAdd = (VerticalContainer) screen.getElementById("objectadd");
		liquid = (VerticalContainer) screen.getElementById("liquidoptions");
		particle = (VerticalContainer) screen.getElementById("particleoptions");
		polygon = (VerticalContainer) screen.getElementById("polygonoptions");
		save = (TextButton) screen.getElementById("save");
		load = (TextButton) screen.getElementById("load");
		menu = (TextButton) screen.getElementById("menu");

		terrainTextureValue.setNumber(0);
		terrainImageSample.setTextureIndex(0);

		liquidTextureValue.setNumber(0);
		terrainImageSample.setTextureIndex(0);

		objectAdd.hide(true);
		terrain.hide(true);
		wall.hide(true);
		object.hide(true);
		liquid.hide(true);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onFloatNumberRollerChanged(FloatNumberRollerChangedEvent event) {
		if (!isActive()) {
			return;
		}
		FloatNumberRoller nr = event.getNumberRoller();
		System.out.println(nr + " " + editingWall);
		if (nr.equals(terrainHeightValue)) {
			height = terrainHeightValue.getNumber();
		} else if (nr.equals(liquidHeightValue)) {
			lheight = liquidHeightValue.getNumber();
		} else if (nr.equals(wallAngle)) {
			if (editingWall != null) {
				editingWall.setAngle(wallAngle.getNumber());
			}
		} else if (nr.equals(particleRespawnRate)) {
			if (editingObject instanceof ParticleObject) {
				ParticleObject part = (ParticleObject) editingObject;
				part.setSpawnRate(particleRespawnRate.getNumber());
				ParticleObject part2 = (ParticleObject) editingObjectCopy;
				part2.setSpawnRate(particleRespawnRate.getNumber());
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onVectorInput(VectorInput3ChangedEvent event) {
		if (!isActive()) {
			return;
		}
		VectorInput3 vecIn = event.getVectorInput();
		boolean isParticle = editingObject instanceof ParticleObject;
		if (vecIn.equals(objectPos)) {
			if (editingObject != null) {
				objects.setObjectLocation(objectPos.asLocation(), editingObject.getUUID());
				// editingObject.setLocation(objectPos.asLocation());
			}
		} else if (vecIn.equals(wallPos)) {
			if (editingWall != null) {
				editingWall.setLocation(wallPos.asLocation());
			}
		} else if (vecIn.equals(particleGravity)) {
			if (isParticle) {
				((ParticleObject) editingObject).setGravity(particleGravity.getVector());
				((ParticleObject) editingObjectCopy).setGravity(particleGravity.getVector());
			}
		} else if (vecIn.equals(particleMinDir)) {
			if (isParticle) {
				((ParticleObject) editingObject).setMinimumDir(particleMinDir.getVector());
				((ParticleObject) editingObjectCopy).setMinimumDir(particleMinDir.getVector());
			}
		} else if (vecIn.equals(particleMaxDir)) {
			if (isParticle) {
				((ParticleObject) editingObject).setMaximumDir(particleMaxDir.getVector());
				((ParticleObject) editingObjectCopy).setMaximumDir(particleMaxDir.getVector());
			}
		} else if (vecIn.equals(particleMinPos)) {
			if (isParticle) {
				((ParticleObject) editingObject).setMinimum(particleMinPos.getVector());
				((ParticleObject) editingObjectCopy).setMinimum(particleMinPos.getVector());
			}
		} else if (vecIn.equals(particleMaxPos)) {
			if (isParticle) {
				((ParticleObject) editingObject).setMaximum(particleMaxPos.getVector());
				((ParticleObject) editingObjectCopy).setMaximum(particleMaxPos.getVector());
			}
		} else if (vecIn.equals(particleColourMin)) {
			if (isParticle) {
				((ParticleObject) editingObject).setMinimumCol(particleColourMin.getVector());
				((ParticleObject) editingObjectCopy).setMinimumCol(particleColourMin.getVector());
			}
		} else if (vecIn.equals(particleColourMax)) {
			if (isParticle) {
				((ParticleObject) editingObject).setMaximumCol(particleColourMax.getVector());
				((ParticleObject) editingObjectCopy).setMaximumCol(particleColourMax.getVector());
			}
		} else if (vecIn.equals(objectAngle)) {
			if (editingObject != null) {
				editingObject.setAngle(objectAngle.getVector());
				editingObjectCopy.setAngle(objectAngle.getVector());
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onInputCaptured(InputHeldEvent event) {
		if (!isActive()) {
			return;
		}
		float ammount = event.getDelta() * event.getMagnitude() * 0.05f;
		if (event.getControl().getControlName().equalsIgnoreCase("move_y_positive")) {
			editCam.setMoveVelocity(new Vector3f(0f, ammount, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("move_y_negative")) {
			editCam.setMoveVelocity(new Vector3f(0f, -ammount, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("move_x_negative")) {
			editCam.setMoveVelocity(new Vector3f(-ammount, 0f, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("move_x_positive")) {
			editCam.setMoveVelocity(new Vector3f(ammount, 0f, 0f));
		} else if (event.getControl().getControlName().equalsIgnoreCase("look_x_positive")) {
			editCam.setAngleVelocity((int) (ammount * 5), 0);
		} else if (event.getControl().getControlName().equalsIgnoreCase("look_x_negative")) {
			editCam.setAngleVelocity((int) -(ammount * 5), 0);
		} else if (event.getControl().getControlName().equalsIgnoreCase("look_y_positive")) {
			editCam.incrementViewSize(ammount * .5f);
			editCam.capViewSize(5f, 25f);
		} else if (event.getControl().getControlName().equalsIgnoreCase("look_y_negative")) {
			editCam.incrementViewSize(-ammount * .5f);
			editCam.capViewSize(5f, 25f);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButtonPress(ButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		Button bP = event.getButton();
		CommonObject cobj;
		if (bP.equals(save)) {
			tw.saveAll();
			cw.save();
		} else if (bP.equals(load)) {

			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("World File", "info");
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(tw.getDirectory(), "world.info"));
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String worldA = chooser.getSelectedFile().getParentFile().getAbsolutePath();
				String cache = MainThread.cache.getAbsolutePath();
				if (worldA.startsWith(cache)) {
					// Good
					String finalWorld = worldA.substring(cache.length());
					tw = new TerrainWorld(finalWorld);
				}
			}
		} else if (bP.equals(menu)) {
			MainThread.changeState(new MenuState());
		} else if (bP.equals(objectLook)) {
			// TODO Re-introduce via CommonWorld
			// if (editingObject != null) {
			// Location zoomTo = tw.getLocationOf(editingObject);
			// if (zoomTo != null) {
			// editCam.setLocation(zoomTo);
			// }
			// }
		} else if (bP.equals(objectModelFile)) {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("DAE Models", "xml", "dae");
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(MainThread.cache, objectModelFile.getString()));
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String modelA = chooser.getSelectedFile().getAbsolutePath();
				String cache = MainThread.cache.getAbsolutePath();
				if (modelA.startsWith(cache)) {
					// Good
					String finalModel = modelA.substring(cache.length());
					objectModelFile.setString(finalModel);
					updateObjectModel(finalModel);
				}
			}
		} else if (bP.equals(objectMaterialModel)) {
			String modelList = "", sep = "";
			DAEFile f = Resources.loadModelFile(objectModelFile.getString());
			if (f != null) {
				if (editingObject.getCommonObject().getType() == Type.Static) {
					JPanel myPanel = new JPanel();
					HashMap<String, JCheckBox> boxes = new HashMap<String, JCheckBox>();
					for (String s : f.getMeshInstNames()) {
						JCheckBox box = new JCheckBox(s);
						boxes.put(s, box);
						myPanel.add(box);
					}
					int result = JOptionPane.showConfirmDialog(null, myPanel, "Please choose meshes for this material", JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						for (String s : boxes.keySet()) {
							JCheckBox box = boxes.get(s);
							if (box.isSelected()) {
								modelList += sep + s;
								sep = ",";
							}
						}
						editingObject.setRenderableSection(objectMaterialNumber.getNumber(), modelList);
						objectMaterialModel.setString(modelList);
					}
				} else if (editingObject.getCommonObject().getType() == Type.Anim) {
					JPanel myPanel = new JPanel();
					HashMap<String, JCheckBox> boxes = new HashMap<String, JCheckBox>();
					for (String s : f.getAnimMeshNames()) {
						JCheckBox box = new JCheckBox(s);
						boxes.put(s, box);
						myPanel.add(box);
					}
					int result = JOptionPane.showConfirmDialog(null, myPanel, "Please choose meshes for this material", JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						for (String s : boxes.keySet()) {
							JCheckBox box = boxes.get(s);
							if (box.isSelected()) {
								modelList += sep + s;
								sep = ",";
							}
						}
						editingObject.setRenderableSection(objectMaterialNumber.getNumber(), modelList);
						objectMaterialModel.setString(modelList);
					}
					// Loading in an animated model needs Idle anim
					editingObject.startAnimation("idle", 1f, false);
				}

			}
		} else if (event.getButton().equals(objectStatic)) {
			cobj = editingObject.getCommonObject();
			cobj.setType(CommonObject.Type.Static);
			objects.removeChild(editingObject);
			editingObject = objects.createObject(cobj);
			this.setEditingObject(editingObject);
		} else if (event.getButton().equals(objectAnim)) {
			cobj = editingObject.getCommonObject();
			cobj.setType(CommonObject.Type.Anim);
			objects.removeChild(editingObject);
			editingObject = objects.createObject(cobj);
			this.setEditingObject(editingObject);
		} else if (event.getButton().equals(objectParticle)) {
			cobj = editingObject.getCommonObject();
			cobj.setType(CommonObject.Type.Particle);
			objects.removeChild(editingObject);
			editingObject = objects.createObject(cobj);
			this.setEditingObject(editingObject);
		} else if (event.getButton().equals(objectMaterialCol)) {
			Vector4f col = editingObject.getMaterialList().getMaterial(objectMaterialNumber.getNumber()).getColour();
			Color newColor = JColorChooser.showDialog(null, "Choose Terrain Colour", new Color(col.x, col.y, col.z));
			if (newColor != null) {
				col = new Vector4f(newColor.getRed() / 255f, newColor.getGreen() / 255f, newColor.getBlue() / 255f, 1f);
				// terrainImageSample.setColour(col);
				editingObject.getMaterialList().getMaterial(objectMaterialNumber.getNumber()).setColour(col);
				objectMaterialCol.getElementData().activeColour = col;
				objectMaterialCol.getElementData().defaultColour = col;
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onNumberRollerChanged(NumberRollerChangedEvent event) {
		if (!isActive()) {
			return;
		}
		NumberRoller nr = event.getNumberRoller();
		if (nr.equals(terrainTextureValue)) {
			terrainImageSample.setTexture(tw.getTextures());
			terrainImageSample.setTextureIndex(terrainTextureValue.getNumber());
			texture = terrainTextureValue.getNumber();
		} else if (nr.equals(terrainFloorNumber)) {
			editingLayer = terrainFloorNumber.getNumber();
			System.out.println(terrainFloorNumber.getNumber());
		} else if (nr.equals(liquidTextureValue)) {
			liquidImageSample.setTexture(tw.getLiquidTextures());
			liquidImageSample.setTextureIndex(liquidTextureValue.getNumber());
			ltexture = liquidTextureValue.getNumber();
		} else if (nr.equals(liquidLayerNumber)) {
			leditingLayer = liquidLayerNumber.getNumber();

		} else if (nr.equals(particleCount)) {
			if (editingObject instanceof ParticleObject) {
				ParticleObject part = (ParticleObject) editingObject;
				part.setParticleCount(particleCount.getNumber());
				ParticleObject part2 = (ParticleObject) editingObjectCopy;
				part2.setParticleCount(particleCount.getNumber());

			}
		} else if (nr.equals(objectMaterialNumber)) {
			setEditingObjectMaterial();
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onStringRollerChanged(StringRollerChangedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getRoller().equals(chooser)) {
			// unselect selected wall unless going into wall edit
			if (!this.isWallEdit()) {
				editingWall = null;
			}
			// Hide all options
			wall.hide(true);
			terrain.hide(true);
			object.hide(true);
			objectAdd.hide(true);
			liquid.hide(true);
			tpath.hide(true);
			polygon.hide(true);
			tmesh.hide(true);
			if (isTerrainEdit()) {
				terrain.hide(false);
			} else if (isWallEdit()) {
				wall.hide(false);
			} else if (isObjectEdit()) {
				object.hide(false);
			} else if (isObjectAdd()) {
				objectAdd.hide(false);
			} else if (isLiquidEdit()) {
				liquid.hide(false);
			} else if (isPolygonEdit()) {
				tmesh.hide(false);
				polygon.hide(false);
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onCheckBoxPress(CheckButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getButton().equals(terrainColour)) {
			if (event.getButton().getChecked()) {
				// Hangs Event Thread while choosing. Not ideal, but also not
				// likely an issue.
				Color newColor = JColorChooser.showDialog(null, "Choose Terrain Colour", new Color(col.x, col.y, col.z));
				if (newColor != null) {
					col = new Vector4f(newColor.getRed() / 255f, newColor.getGreen() / 255f, newColor.getBlue() / 255f, 1f);
					terrainImageSample.setColour(col);
				}
			}
		} else if (event.getButton().equals(liquidColour)) {
			if (event.getButton().getChecked()) {
				// Hangs Event Thread while choosing. Not ideal, but also not
				// likely an issue.
				Color newColor = JColorChooser.showDialog(null, "Choose Liquid Colour", new Color(lcol.x, lcol.y, lcol.z, lcol.w));
				if (newColor != null) {
					lcol = new Vector4f(newColor.getRed() / 255f, newColor.getGreen() / 255f, newColor.getBlue() / 255f, newColor.getAlpha() / 255f);
					liquidImageSample.setColour(lcol);
				}
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onTextInput(TextInputEvent event) {
		if (!isActive()) {
			return;
		}
		TextInput in = event.getInput();
		if (in.equals(objectMaterialTexture)) {
			MaterialList ml = editingObject.getMaterialList();
			ml.setTexture(objectMaterialNumber.getNumber(), event.getTextAfter());
		} else if (in.equals(objectMaterialTextureData)) {
			MaterialList ml = editingObject.getMaterialList();
			ml.setDataTexture(objectMaterialNumber.getNumber(), event.getTextAfter());
		}
	}

	private void updateObjectModel(String fileName) {
		if (editingObject != null) {
			editingObject.setRenderableFile(fileName);
			editingObjectCopy.setRenderableFile(fileName);
		}
	}

	@Override
	public void stop() {
		screen = null;
	}

	public CommonAreaLoc getAreaLocationOfLocation(Location l) {
		int mapx = (int) (Math.floor(l.getFullXAsFloat() / ((TerrainLayer.size - 1) * 1f)));
		int mapy = (int) (Math.floor(l.getFullYAsFloat() / ((TerrainLayer.size - 1) * 1f)));
		return new CommonAreaLoc(mapx, mapy);
	}

	public TerrainArea getAreaViewed() {
		return tw.getAreaOfLocation(editCam.getLocation());
	}

	@Override
	public void update(float delta) {
		tw.setAllObjectsRenderStyle(RenderStyle.GRAYSCALE);
		objects.fillFrom(cw, tw);
		tmesh.setFrom(cw.getNavMesh(), tw);
		if (editingObject != null && isObjectEdit()) {
			editingObject.setRenderStyle(RenderStyle.NORMAL);
		} else if (editingWall != null && isWallEdit()) {
			editingWall.setRenderStyle(RenderStyle.NORMAL);
		} else if (isTerrainEdit()) {
			TerrainArea ta = getAreaViewed();
			if (ta != null) {
				if (ta.ownsLayer(lastLayer)) {
					// We've not moved off this area
					while (editingLayer >= ta.getLayerCount()) {
						ta.addLayer(false);
					}
				} else {
					// We've moved.
					editingLayer = 0;
					terrainFloorNumber.setNumber(0);
				}

				if (ta.getLayer(editingLayer) != null) {
					lastLayer = ta.getLayer(editingLayer).layer;
					lastLayer.setRenderStyle(RenderStyle.NORMAL);
				}

			}
		} else if (isLiquidEdit()) {
			TerrainArea ta = getAreaViewed();
			if (ta != null) {
				if (ta.ownsLayer(lastLiquid)) {
					while (leditingLayer >= ta.getLiquidCount()) {
						ta.addLiquid(false);
					}
				} else {
					leditingLayer = 0;
					liquidLayerNumber.setNumber(0);
				}
				lastLiquid = ta.getLiquid(leditingLayer).layer;
				lastLiquid.setRenderStyle(RenderStyle.NORMAL);
			}
		} else {
			tw.setAllObjectsRenderStyle(RenderStyle.NORMAL);
		}
	}

	public Location snapLocationToGrid(Location l, float size) {
		Location l2 = new Location();
		l2.setLocation(l.getTileX(), l.getXRoundUp(size), l.getTileY(), l.getYRoundUp(size));
		l2.setZ(l.getZ());
		return l2;
	}

	public Location getTerrainVertexNearest(Location l) {
		Location l2 = snapLocationToGrid(l, 1f);
		TerrainArea ta = getAreaViewed();
		if (ta != null) {
			l2.setZ(tw.getHeightAt(l2));
			return l2;
		}
		return l2;
	}

	public void paint(Location l) {
		Location l2 = snapLocationToGrid(l, 1f);
		int x = (int) l2.getTileX();
		int y = (int) l2.getTileY();
		TerrainArea ta = getAreaViewed();
		if (ta != null) {
			if (isTerrainEdit()) {
				// TerrainEditableVertex tev = tw.getVertexAt(x, y,
				// this.editingLayer, false);
				// if (tev == null) { return; }
				TerrainLayerAlteration alter = new TerrainLayerAlteration();
				alter.setColour = terrainColour.getChecked();
				alter.setHeight = terrainHeight.getChecked();
				alter.setTexture = terrainTexture.getChecked();
				alter.col = col;
				alter.height = height;
				alter.tex = texture;
				tw.alter(x, y, this.editingLayer, alter);
			} else if (isLiquidEdit()) {
				// TerrainLiquidEditableVertex tlev = tw.getLiquidVertexAt(x, y,
				// this.leditingLayer, false);
				TerrainLiquidLayerAlteration alter = new TerrainLiquidLayerAlteration();
				alter.setColour = liquidColour.getChecked();
				alter.setHeight = liquidHeight.getChecked();
				alter.setTexture = liquidTexture.getChecked();
				alter.setFlow = liquidFlow.getChecked();
				alter.col = lcol;
				alter.height = lheight;
				alter.texture = ltexture;
				alter.flowx = liquidFlowX.getNumber();
				alter.flowy = liquidFlowY.getNumber();
				tw.alter(x, y, this.leditingLayer, alter);
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onMouseOver(MouseRenderableHoverEvent event) {
		if (!isActive()) {
			return;
		}
		Pickable obj = event.getRenderable();
		if (obj instanceof TerrainLayerNode) {
			if (isTerrainEdit()) {
				ts.setGridSize(1f);
				ts.setLocation(event.getLocation());
			}
		} else if (obj instanceof TerrainLiquidLayerNode) {
			if (isLiquidEdit()) {
				ts.setGridSize(1f);
				ts.setLocation(event.getLocation());
			}
		} else {
			ts.setGridSize(0.01f);
			ts.setLocation(event.getLocation());
		}
		if (isPolygonEdit()) {
			tmesh.setMouseLocation(event.getLocation());
		} else {
			tmesh.setMouseLocation(null);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onDragRenderable(MouseRenderableDragEvent event) {
		if (!isActive()) {
			return;
		}
		Pickable obj = event.getObject();
		if (event.getButton() == 0) {
			if (isTerrainEdit() || isLiquidEdit()) {
				if (obj instanceof TerrainLayerNode || obj instanceof TerrainLiquidLayerNode || obj instanceof BaseObject) {
					paint(event.getLocation());
				}
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onButtonRenderable(MouseButtonRenderableEvent event) {
		if (!isActive()) {
			return;
		}
		System.out.println(event.getObject());
		Location l = event.getLocation();
		if (l == null) {
			return;
		}
		int b = event.getButton();
		if (isTerrainEdit() && b == 0) {
			if (event.getObject() instanceof TerrainLayerNode) {
				paint(l);
				event.setConsumed();
			}
		} else if (isLiquidEdit() && b == 0) {
			if (event.getObject() instanceof TerrainLiquidLayerNode) {
				paint(l);
				event.setConsumed();
			}
		} else if (isWallAdd() && b == 0) {
			System.out.println("Adding wall at " + l);
			Wall w = tw.createWallAt(l);
			chooser.setStringIndex(4);
			setEditingWall(w);
			event.setConsumed();
			// editingWall = w;
		} else if (isWallEdit() && b == 0) {
			if (event.getObject() instanceof Wall) {
				Wall w = (Wall) event.getObject();
				setEditingWall(w);
				// editingWall = w;
				event.setConsumed();
			}
		} else if (isObjectAdd() && b == 0) {
			CommonObject c = cw.createObjectAt(l, Type.Static, "mod/sphere.dae:Sphere", new MaterialList());
			BaseObject obj = objects.createObject(c);
			setEditingObject(obj);
			chooser.setStringIndex(6);
			event.setConsumed();
			/*
			 * if (objectStatic.getChecked()) { BaseObject object =
			 * objects.createObject(cw.createObjectAt(l, "static:" +
			 * objectAddModel.getString(), null)); chooser.setStringIndex(4);
			 * setEditingObject(object); event.setConsumed(); } else if
			 * (objectAnimated.getChecked()) { BaseObject object =
			 * objects.createObject(cw.createObjectAt(l, "animated:" +
			 * objectAddModel.getString(), null)); chooser.setStringIndex(5);
			 * setEditingObject(object); event.setConsumed(); } else if
			 * (objectParticle.getChecked()) { BaseObject object
			 * =objects.createObject(cw.createObjectAt(l, "particle:", null));
			 * chooser.setStringIndex(5); setEditingObject(object);
			 * event.setConsumed(); }
			 */
		} else if (isObjectEdit() && b == 0) {
			if (event.getObject() instanceof BaseObject) {
				setEditingObject((BaseObject) event.getObject());
				event.setConsumed();
			}
		} else if (isPolygonEdit() && b == 0) {
			double x = l.getTileX() + l.getMinorX();
			double y = l.getTileY() + l.getMinorY();
			if (polygonPoints.getChecked()) {
				cw.getNavMesh().addPoint(new Point(x, y, l.getLayer()));
				System.out.println("Adding point");
			} else if (polygonPoly.getChecked()) {
				Point point = cw.getNavMesh().getNearestPoint(new Point(x, y, l.getLayer()));

				if (nextPoly.size() > 0 && nextPoly.get(0) == point) {
					if (nextPoly.size() > 2) {
						// TODO check for integrity
						// hahaha as if
						cw.getNavMesh().addPoly(nextPoly);
						nextPoly = new ArrayList<Point>();
					}
				} else {
					if (nextPoly.contains(point)) {
						System.out.println("Polygon already has point : " + point);
					} else {
						System.out.println("Adding point to polygon: " + point);
						nextPoly.add(point);
					}
				}
			} else if (polygonTest.getChecked()) {
				pathPoints.add(new Point(x, y, l.getLayer()));
				if (pathPoints.size() > 2) {
					pathPoints.remove(0);
				}
				if (pathPoints.size() == 2) {
					PathFinderPolygonAStar path = new PathFinderPolygonAStar(cw.getNavMesh(), pathPoints.get(1));
					path.makePath(pathPoints.get(0));
					if (path.getPath() != null) {
						tmesh.addPath(path.getPath());
					}
				}
			}

		}
	}

	private void setEditingObject(BaseObject object2) {
		objectViewNode.removeChild(null); // Special case - removes all children
		editingObject = object2;
		objectPos.setVector(editingObject.getLocation().toVector3());
		objectAngle.setVector(editingObject.getLocation().getRotate());
		objectModelFile.setString(editingObject.getRenderableFile());
		// objectMaterial.setString(editingObject.getMaterialLabel());
		editingObjectCopy = editingObject.clone();
		objectViewNode.addChild(editingObjectCopy);
		objectCam.setObject(editingObjectCopy);
		objectMaterialNumber.setNumber(0);
		setEditingObjectMaterial();
		// objectView.getRenderView().getCam().setLocation(copy.location);
		if (object2 instanceof ParticleObject) {
			particle.hide(false);
			ParticleObject pobj = (ParticleObject) object2;
			particleCount.setNumber(pobj.getParticleCount());
			particleGravity.setVector(pobj.getGravity());
			particleMinPos.setVector(pobj.getMinimum());
			particleMaxPos.setVector(pobj.getMaximum());
			particleMinDir.setVector(pobj.getMinimumDir());
			particleMaxDir.setVector(pobj.getMaximumDir());
			particleRespawnRate.setNumber(pobj.getSpawnRate());
			particleColourMin.setVector(pobj.getMinimumCol());
			particleColourMax.setVector(pobj.getMaximumCol());
		} else {
			particle.hide(true);
		}
	}

	private void setEditingObjectMaterial() {
		int i = objectMaterialNumber.getNumber();
		MaterialList ml = editingObject.getMaterialList();
		while (ml.getMaterial(i) == null) {
			ml.addMaterialForce(new Material("blank", "tex/flat.png", new Vector4f(1f, 0f, 0f, 1f)));
		}
		Material m = ml.getMaterial(i);
		objectMaterialModel.setString(editingObject.getRenderableSection(i));
		objectMaterialCol.getElementData().defaultColour = m.getColour();
		// objectMaterialModel.setString(editingObject.getRenderableSection(i));
		objectMaterialTexture.setString(m.getTextureName());
		objectMaterialTextureData.setString(m.getTextureDataName());
	}

	private void setEditingWall(Wall w) {
		editingWall = w;
		wallPos.setVector(editingWall.getLocation().toVector3());
		wallAngle.setNumber(w.getAngle());
		// wallMaterial.setString(editingWall.getMaterialLabel());
	}

}
