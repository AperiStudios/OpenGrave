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
import java.util.HashMap;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.common.world.*;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.og.MainThread;
import com.opengrave.og.engine.*;
import com.opengrave.og.engine.gait.BipedWalk;
import com.opengrave.og.engine.gait.Gait;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.gui.callback.CheckButtonPressedEvent;
import com.opengrave.og.gui.callback.NumberRollerChangedEvent;
import com.opengrave.og.gui.callback.TextInputEvent;
import com.opengrave.og.light.DayCycleSkyLight;
import com.opengrave.og.models.DAEFile;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Vector4f;

public class IKState extends BaseState implements EventListener {
	Surface s;
	CommonObject cobj;
	BaseObject object;
	SkeletonInstance skele;
	ObjectStorageNode dudnode;
	private String modelFileName = "mod/craig.dae";
	private RootNode node;
	FlyByCamera editCam = new FlyByCamera();
	SceneView view;
	NumberRoller objectMaterialNumber;
	TextInput objectMaterialTexture, objectMaterialTextureData;
	TextButton objectModelFile, objectLook, objectMaterialCol, objectMaterialModel;
	private TextButton objectStatic;
	private TextButton objectAnim;
	private TextButton objectParticle;

	CheckButton skinButton, skeleButton;

	@Override
	public void start() {
		EventDispatcher.addHandler(this);

		GUIXML screenFile = new GUIXML("gui/ik.xml");
		screen = screenFile.getGUI();
		view = (SceneView) screen.getElementById("ikview");

		objectModelFile = (TextButton) screen.getElementById("objectmodelfile");
		objectMaterialNumber = (NumberRoller) screen.getElementById("objectmaterialnumber");
		objectMaterialCol = (TextButton) screen.getElementById("objectmaterialcol");
		objectMaterialTexture = (TextInput) screen.getElementById("objectmaterialtex");
		objectMaterialTextureData = (TextInput) screen.getElementById("objectmaterialtexdata");
		objectMaterialModel = (TextButton) screen.getElementById("objectmaterialmodel");
		objectStatic = (TextButton) screen.getElementById("objectstatic");
		objectAnim = (TextButton) screen.getElementById("objectanim");
		objectParticle = (TextButton) screen.getElementById("objectparticle");

		node = new RootNode();
		dudnode = new ObjectStorageNode();
		node.addChild(dudnode);
		node.setSkyLight(new DayCycleSkyLight(MainThread.config.getInteger("shadowSize", 1024)));
		view.setRenderView(new RenderView(node, editCam));

		s = new InclineSurface();
		objectMaterialNumber.setNumber(0);
		setObject(new CommonObject("", Type.Anim, modelFileName, new MaterialList(), new CommonLocation()));
		// object.startAnimation("idle", 1f, false);

		skinButton = (CheckButton) screen.getElementById("skin");
		skeleButton = (CheckButton) screen.getElementById("skele");
		Gait.init();
	}

	public void setObject(CommonObject object) {
		if (object.getType() != Type.Anim) {
			return;
		}
		if (this.object != null) {
			dudnode.removeChild(this.object);
		}
		if (this.skele != null) {
			dudnode.removeChild(this.skele);
		}
		this.object = dudnode.createObject(object);
		AnimatedObject anim = (AnimatedObject) this.object;
		this.skele = new SkeletonInstance(anim);
		dudnode.addChild(this.skele);
		dudnode.addChild(this.object);

		this.object.setSurface(s);
		setEditingObjectMaterial();
		Location l = new Location(object.getLocation());
		l.setScale(0.2f, 0.2f, 0.2f);
		this.object.setLocation(l);
		if (anim.getSkeleton() != null) {
			anim.setWalk(new BipedWalk(anim, anim.getSkeleton().getBone("UpperLeg.left"), anim.getSkeleton().getBone("UpperLeft.right")));
		}
		this.cobj = object;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onCheckBoxPress(CheckButtonPressedEvent event) {
		CheckButton b = event.getButton();
		if (b == skinButton) {
			object.setVisible(b.getChecked());
		} else if (b == skeleButton) {
			skele.setVisible(b.getChecked());
		}
	}

	@Override
	public void stop() {

	}

	@Override
	public void update(float delta) {
		// this.skele.setLocation(this.object.getLocation());
		this.skele.setSkeleton((AnimatedObject) this.object);
	}

	private void setEditingObjectMaterial() {
		int i = objectMaterialNumber.getNumber();
		MaterialList ml = object.getMaterialList();
		while (ml.getMaterial(i) == null) {
			ml.addMaterialForce(new Material("blank", "tex/flat.png", new Vector4f(1f, 0f, 0f, 1f)));
		}
		Material m = ml.getMaterial(i);
		objectMaterialModel.setString(object.getRenderableSection(i));
		objectMaterialCol.getElementData().defaultColour = m.getColour();
		// objectMaterialModel.setString(editingObject.getRenderableSection(i));
		objectMaterialTexture.setString(m.getTextureName());
		objectMaterialTextureData.setString(m.getTextureDataName());
		objectModelFile.setString(modelFileName);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButonPress(ButtonPressedEvent event) {
		if (!isActive()) {
			return;
		}
		Button bP = event.getButton();
		if (bP.equals(objectModelFile)) {
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
					object.setRenderableFile(finalModel);
				}
			}
		} else if (bP.equals(objectMaterialModel)) {
			String modelList = "", sep = "";
			DAEFile f = Resources.loadModelFile(objectModelFile.getString());
			if (f != null) {
				if (object.getCommonObject().getType() == Type.Static) {
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
						object.setRenderableSection(objectMaterialNumber.getNumber(), modelList);
						objectMaterialModel.setString(modelList);
					}
				} else if (object.getCommonObject().getType() == Type.Anim) {
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
						object.setRenderableSection(objectMaterialNumber.getNumber(), modelList);
						objectMaterialModel.setString(modelList);
					}
					// Loading in an animated model needs Idle anim
					object.startAnimation("idle", 1f, false);
					AnimatedObject anim = (AnimatedObject) object;
					anim.setWalk(new BipedWalk(anim, anim.getSkeleton().getBone("UpperLeg.left"), anim.getSkeleton().getBone("UpperLeg.right")));

				}

			}
		} else if (event.getButton().equals(objectStatic)) {
			cobj = object.getCommonObject();
			cobj.setType(CommonObject.Type.Static);
			dudnode.removeChild(object);
			object = dudnode.createObject(cobj);
		} else if (event.getButton().equals(objectAnim)) {
			cobj = object.getCommonObject();
			cobj.setType(CommonObject.Type.Anim);
			dudnode.removeChild(object);
			object = dudnode.createObject(cobj);
		} else if (event.getButton().equals(objectParticle)) {
			cobj = object.getCommonObject();
			cobj.setType(CommonObject.Type.Particle);
			dudnode.removeChild(object);
			object = dudnode.createObject(cobj);
		} else if (event.getButton().equals(objectMaterialCol)) {
			Vector4f col = object.getMaterialList().getMaterial(objectMaterialNumber.getNumber()).getColour();
			Color newColor = JColorChooser.showDialog(null, "Choose Terrain Colour", new Color(col.x, col.y, col.z));
			if (newColor != null) {
				col = new Vector4f(newColor.getRed() / 255f, newColor.getGreen() / 255f, newColor.getBlue() / 255f, 1f);
				// terrainImageSample.setColour(col);
				object.getMaterialList().getMaterial(objectMaterialNumber.getNumber()).setColour(col);
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

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onTextInput(TextInputEvent event) {
		if (!isActive()) {
			return;
		}
		TextInput in = event.getInput();
		if (in.equals(objectMaterialTexture)) {
			MaterialList ml = object.getMaterialList();
			ml.setTexture(objectMaterialNumber.getNumber(), event.getTextAfter());
		} else if (in.equals(objectMaterialTextureData)) {
			MaterialList ml = object.getMaterialList();
			ml.setDataTexture(objectMaterialNumber.getNumber(), event.getTextAfter());
		}
	}

	float rx, ry;

	/*
	 * @EventHandler(priority = EventHandlerPriority.LATE)
	 * public void onJoystickInput(JoystickRawAxisEvent event){
	 * //System.out.println(event.getAxisName()+" "+event.getValue());
	 * if(event.getAxisName().equals("x")){
	 * if(Math.abs(event.getValue()) < 0.3f) { return; }
	 * Location l = object.getLocation();
	 * l.add(new Vector3f(event.getValue() * event.getDelta() * 0.001f, 0f,
	 * 0f));
	 * object.setLocation(l);
	 * object.moveTo(l);
	 * }else if(event.getAxisName().equals("y")){
	 * if(Math.abs(event.getValue()) < 0.3f) { return; }
	 * Location l = object.getLocation();
	 * l.add(new Vector3f(0f, event.getValue() * event.getDelta()* 0.001f, 0f));
	 * object.moveTo(l);
	 * }else if(event.getAxisName().equals("z")){
	 * if(Math.abs(event.getValue()) < 0.3f || event.getValue() < 0f){ return; }
	 * Location l = object.getLocation();
	 * l.add(new Vector3f(0f,0f,event.getValue()* event.getDelta() * 0.001f));
	 * }else if(event.getAxisName().equals("rz")){
	 * if(Math.abs(event.getValue()) < 0.3f || event.getValue() < 0f){ return; }
	 * Location l = object.getLocation();
	 * l.add(new Vector3f(0f,0f,-event.getValue()* event.getDelta() * 0.001f));
	 * 
	 * }else if(event.getAxisName().equalsIgnoreCase("rx")){
	 * rx = event.getValue();
	 * }else if(event.getAxisName().equalsIgnoreCase("ry")){
	 * ry = event.getValue();
	 * }else if(event.getAxisName().equals("A")){
	 * if(event.getValue()>0.5f){
	 * ((AnimatedObject) object).getWalk().removePlant();
	 * }
	 * }
	 * if(rx*rx + ry * ry >=0.9f){ // Distance from center of pad > 0.3f.
	 * 
	 * object.lookDir(new Point(rx, ry, 0));
	 * }
	 * }
	 */
}
