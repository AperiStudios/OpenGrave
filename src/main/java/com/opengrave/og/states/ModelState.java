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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.world.*;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.*;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.*;
import com.opengrave.og.light.DayCycleSkyLight;
import com.opengrave.og.models.DAEFile;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class ModelState extends BaseState implements EventListener {

	private String modelFileName = "mod/craig.dae";
	// public DAEFile model;
	public DAEFile model;
	Button quit = null;
	StringRoller chooser;
	private boolean changed = true;
	SceneView view;
	RootNode node;
	ObjectStorageNode dudnode;
	BaseObject obj;
	CommonObject cobj;

	public ArrayList<String> modelParts = new ArrayList<String>();
	ArrayList<ArrayList<String>> chosenParts = new ArrayList<ArrayList<String>>();
	public MaterialList matList;
	// BoundingBoxCamera editCam = new BoundingBoxCamera();
	FlyByCamera editCam = new FlyByCamera();
	private TextButton objectModelFile;
	private NumberRoller objectMaterialNumber;
	private TextButton objectMaterialCol, objectStatic, objectAnim, objectParticle;
	private TextInput objectMaterialTexture;
	private TextInput objectMaterialTextureData;
	private TextButton objectMaterialModel;
	VerticalContainer mat, anim;

	@Override
	public void start() {
		EventDispatcher.addHandler(this);
		GUIXML screenFile = new GUIXML("gui/model.xml");
		screen = screenFile.getGUI();
		chooser = (StringRoller) screen.getElementById("chooser");
		quit = (Button) screen.getElementById("quit");
		view = (SceneView) screen.getElementById("modelview");
		objectModelFile = (TextButton) screen.getElementById("objectmodelfile");
		objectMaterialNumber = (NumberRoller) screen.getElementById("objectmaterialnumber");
		objectMaterialCol = (TextButton) screen.getElementById("objectmaterialcol");
		objectStatic = (TextButton) screen.getElementById("objectstatic");
		objectAnim = (TextButton) screen.getElementById("objectanim");
		objectParticle = (TextButton) screen.getElementById("objectparticle");
		objectMaterialTexture = (TextInput) screen.getElementById("objectmaterialtex");
		objectMaterialTextureData = (TextInput) screen.getElementById("objectmaterialtexdata");
		objectMaterialModel = (TextButton) screen.getElementById("objectmaterialmodel");
		mat = (VerticalContainer) screen.getElementById("matcont");
		anim = (VerticalContainer) screen.getElementById("animcont");

		anim.hide(true);

		objectModelFile.setString(modelFileName);
		objectMaterialNumber.setNumber(0);

		node = new RootNode();
		dudnode = new ObjectStorageNode();
		node.addChild(dudnode);
		node.setSkyLight(new DayCycleSkyLight(MainThread.config.getInteger("shadowSize", 1024)));
		Location lightLoc = new Location();
		lightLoc.setZ(4f);
		view.setRenderView(new RenderView(node, editCam));
		setObject(Type.Static);
		setEditingObjectMaterial();
	}

	public void setObject(Type type) {
		cobj = new CommonObject("", type, modelFileName, new MaterialList(), new CommonLocation());
		obj = dudnode.createObject(cobj);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onStringRollerChanged(StringRollerChangedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getRoller().equals(chooser)) {
			String val = chooser.getString();
			mat.hide(true);
			anim.hide(true);
			if (val.equalsIgnoreCase("Choose Parts")) {
				mat.hide(false);
			} else if (val.equalsIgnoreCase("Choose Animations")) {
				anim.hide(false);
			}
		}
	}

	public void setModel(String fileName) {
		this.modelFileName = fileName;
		this.changed = true;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButtonPress(ButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		if (event.getButton().equals(quit)) {
			MainThread.changeState(new MenuState());
		} else if (event.getButton().equals(objectModelFile)) {
			anim.removeAllChildren();
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
					obj.setRenderableFile(finalModel);
					DAEFile file = Resources.loadModelFile(finalModel);
					for (String s : file.getAnimationNames()) {
						int idx = s.indexOf("-");
						if (idx > 0) {
							s = s.substring(0, idx);
						}
						CheckButton c = new CheckButton(new ElementData(anim.getElementData()));
						c.setString(s);
						anim.addChildEnd(c);
					}
				}
			}
		} else if (event.getButton().equals(objectMaterialModel)) {
			String modelList = "", sep = "";
			DAEFile f = Resources.loadModelFile(objectModelFile.getString());
			if (f != null) {
				if (cobj.getType() == Type.Static) {
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
						obj.setRenderableSection(objectMaterialNumber.getNumber(), modelList);
						objectMaterialModel.setString(modelList);
					}
				} else if (cobj.getType() == Type.Anim) {
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
						obj.setRenderableSection(objectMaterialNumber.getNumber(), modelList);
						objectMaterialModel.setString(modelList);
					}
				}

			}
		} else if (event.getButton().equals(objectStatic)) {
			cobj.setType(CommonObject.Type.Static);
			dudnode.removeChild(obj);
			obj = dudnode.createObject(cobj);
		} else if (event.getButton().equals(objectAnim)) {
			cobj.setType(CommonObject.Type.Anim);
			dudnode.removeChild(obj);
			obj = dudnode.createObject(cobj);
		} else if (event.getButton().equals(objectParticle)) {
			cobj.setType(CommonObject.Type.Particle);
			dudnode.removeChild(obj);
			obj = dudnode.createObject(cobj);
		} else if (event.getButton().equals(objectMaterialCol)) {
			Vector4f col = obj.getMaterialList().getMaterial(objectMaterialNumber.getNumber()).getColour();
			Color newColor = JColorChooser.showDialog(null, "Choose Terrain Colour", new Color(col.x, col.y, col.z));
			if (newColor != null) {
				col = new Vector4f(newColor.getRed() / 255f, newColor.getGreen() / 255f, newColor.getBlue() / 255f, 1f);
				// terrainImageSample.setColour(col);
				obj.getMaterialList().getMaterial(objectMaterialNumber.getNumber()).setColour(col);
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
		if (nr.equals(objectMaterialNumber)) {
			setEditingObjectMaterial();
		}
	}

	@Override
	public void stop() {
		screen.delete();
		screen = null;
	}

	@Override
	public void update(float delta) {
		// editCam.setObject(obj);
		if (changed) {
			changed = false;

			model = new DAEFile();
			try {
				model.parseData(this.modelFileName);
			} catch (ParserConfigurationException e) {
				new DebugExceptionHandler(e);
			} catch (SAXException e) {
				new DebugExceptionHandler(e);
			} catch (IOException e) {
				new DebugExceptionHandler(e);
			}

			model.scale = new Vector3f(1f, 1f, 1f);
		}
	}

	private void setEditingObjectMaterial() {
		int i = objectMaterialNumber.getNumber();
		MaterialList ml = obj.getMaterialList();
		while (ml.getMaterial(i) == null) {
			ml.addMaterialForce(new Material("blank", "tex/flat.png", new Vector4f(1f, 0f, 0f, 1f)));
		}
		Material m = ml.getMaterial(i);
		objectMaterialModel.setString(obj.getRenderableSection(i));
		objectMaterialCol.getElementData().defaultColour = m.getColour();
		// objectMaterialModel.setString(editingObject.getRenderableSection(i));
		objectMaterialTexture.setString(m.getTextureName());
		objectMaterialTextureData.setString(m.getTextureDataName());
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onTextInput(TextInputEvent event) {
		if (!isActive()) {
			return;
		}
		TextInput in = event.getInput();
		if (in.equals(objectMaterialTexture)) {
			MaterialList ml = obj.getMaterialList();
			ml.setTexture(objectMaterialNumber.getNumber(), event.getTextAfter());
		} else if (in.equals(objectMaterialTextureData)) {
			MaterialList ml = obj.getMaterialList();
			ml.setDataTexture(objectMaterialNumber.getNumber(), event.getTextAfter());
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onCheckBoxPress(CheckButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		CheckButton b = event.getButton();
		if (anim.containsChild(b)) {
			if (b.getChecked()) {
				obj.startAnimation(b.getString(), 1f, false);
			} else {
				obj.stopAnimation(b.getString());
			}
		}
	}
}
