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
package com.opengrave.og.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opengrave.og.MainThread;
import com.opengrave.og.engine.Location;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public class DAEFile {

	public Location location = new Location();
	public Vector3f rotation = new Vector3f();
	public Vector3f scale = new Vector3f();
	public Document document;

	public ArrayList<DAEAnimatedMesh> meshAnim = new ArrayList<DAEAnimatedMesh>();
	public ArrayList<DAEMesh> meshStatic = new ArrayList<DAEMesh>();
	public ArrayList<DAEMeshInstance> meshStaticInstance = new ArrayList<DAEMeshInstance>();
	private ArrayList<DAEController> controls = new ArrayList<DAEController>();
	public ArrayList<DAEAnimation> animations = new ArrayList<DAEAnimation>();
	public ArrayList<DAEAnimClip> animationClips = new ArrayList<DAEAnimClip>();

	public void parseData(String fileName) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(MainThread.cache, fileName);

		if (!file.exists()) {
			System.out.println("Error loading DAE file " + fileName + " : File not found");
			return;
		}
		InputStream is = new FileInputStream(file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = dbf.newDocumentBuilder();
		document = builder.parse(is);

		// Get all Materials
		// ArrayList<String> textureNames = new ArrayList<String>();
		/*
		 * NodeList nodes = document.getElementsByTagName("material"); for
		 * (int i = 0; i < nodes.getLength(); i++) { DAEMaterial material =
		 * new DAEMaterial(this, (Element) nodes.item(i));
		 * materials.add(material); if (material.textureName != null) { if
		 * (!textureNames.contains(material.textureName)) {
		 * textureNames.add(material.textureName); } material.textureIndex =
		 * textureNames .indexOf(material.textureName); } }
		 */
		// Bake materials into a texture pile
		// TextureAtlas texture = (TextureAtlas) Resources
		// .loadTextures(textureNames);
		// Hide it in each Material, for access sake
		// for (DAEMaterial mat : materials) {
		// mat.texture = texture;
		// }

		// Get all Static Meshes.

		NodeList nodes = document.getElementsByTagName("geometry");
		for (int i = 0; i < nodes.getLength(); i++) {
			DAEMesh object = new DAEMesh(this, nodes.item(i));
			meshStatic.add(object);
		}

		/*
		 * NodeList nodes = document.getElementsByTagName("triangles");
		 * 
		 * for (int i = 0; i < nodes.getLength(); i++) { DAEStaticMesh
		 * object = new DAEStaticMesh(this, nodes.item(i));
		 * meshStatic.add(object); }
		 */

		// System.out.println("Meshes Found : " + meshStatic.size());

		// Get all Scene nodes. These magically include bone/joint info too!
		nodes = document.getElementsByTagName("visual_scene");
		DAESceneNode topLevel = new DAESceneNode();
		// topLevel.total.scale(new Vector3f(0.03f, 0.03f, 0.03f));
		for (int i = 0; i < nodes.getLength(); i++) {
			Node cNode = nodes.item(i);
			DAESceneNode.make(this, topLevel, (Element) cNode);
		}

		for (DAEController controller : controls) {
			controller.prepare(topLevel);
		}

		// System.out.println("Skeletal Controllers found : "
		// + controls.size());

		// Get all Animations

		nodes = document.getElementsByTagName("animation");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			DAEAnimation anim = new DAEAnimation(node);
			animations.add(anim);
		}

		nodes = document.getElementsByTagName("animation_clip");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			DAEAnimClip anim = new DAEAnimClip(this, node);
			animationClips.add(anim);
		}

		// Create Animated Models using info we've garnered

		// This will be the perfect time to bake any of the info in

		// TODO Bake in BSM from skin instead of giving it to Anim Mesh
		// Every Vertex should be vertex = Matrix4f.transform(BSM, vertex,
		// null);

		for (DAEController controller : controls) {
			for (DAESkin skin : controller.skinList) {
				if (skin.staticModelName != null) {
					DAEMesh sMesh = getMesh(skin.staticModelName);
					DAESceneNode skeleton = controller.skeleton;

					// System.out.println("Mesh : " + skin.staticModelName
					// + " Skele : " + controller.skeleton.nodeName);
					if (sMesh == null || skeleton == null || animationClips.size() == 0) {
						continue;
					}
					DAEAnimatedMesh daeam = new DAEAnimatedMesh(this, sMesh, skeleton, animationClips, controller.animName);
					daeam.matrix = skin.bindShapeMatrix;
					meshAnim.add(daeam);
				}
			}
		}

		// System.out
		// .println("Animated, Skeletal Meshes : " + meshAnim.size());

	}

	/*
	 * public void update(float delta) { for (DAEMesh object : meshStatic) {
	 * object.location = location; object.rotation = rotation; object.scale =
	 * scale; object.update(delta); } for (DAEAnimatedMesh object : meshAnim) {
	 * object.location = location; object.rotation = rotation; object.scale =
	 * scale; object.update(delta); } }
	 */

	/*
	 * public void renderForPicking() { for (DAEMesh object : meshStatic) {
	 * object.renderForPicking(); } for (DAEAnimatedMesh object : meshAnim) {
	 * object.renderForPicking(); } }
	 */

	/*
	 * public void render() { for (DAEMesh object : meshStatic) {
	 * object.render(); } for (DAEAnimatedMesh object : meshAnim) {
	 * object.render(); } }
	 */

	public DAEMesh getMesh(String id) {
		for (DAEMesh object : meshStatic) {
			if (object == null || object.getName() == null) {
				continue;
			}
			if (object.getName().equalsIgnoreCase(id)) {
				return object;
			}
		}
		System.out.println("Failed to find static mesh : " + id);
		for (DAEMesh object : meshStatic) {
			System.out.println(object.getName());
		}
		return null;
	}

	public DAEAnimatedMesh getAnimMesh(String id) {
		for (DAEAnimatedMesh object : meshAnim) {
			if (object == null || object.getName() == null) {
				continue;
			}
			if (object.getName().equalsIgnoreCase(id)) {
				return object;
			}
		}
		System.out.println("Failed to get animated mesh : " + id);
		return null;
	}

	public void addController(String animName, String skeleNode, String controllerName, Matrix4f total) {
		DAEController controller = new DAEController(this, skeleNode, controllerName, animName);
		// controller.bindShapeMatrix = total;
		controls.add(controller);

	}

	public static Matrix4f readMatrix(String textContent) {
		ArrayList<Matrix4f> matrixList = floatsToMatrix(getFloats(textContent));
		if (matrixList.size() == 0) {
			System.out.println("No Matrices returned");
			return new Matrix4f();
		}
		return matrixList.get(0);
	}

	public static ArrayList<String> getStrings(Node node) {
		return getStrings(node.getTextContent());
	}

	public static ArrayList<String> getStrings(String string) {
		ArrayList<String> strings = new ArrayList<String>();
		for (String s : string.split("\\s+")) {
			if (s.length() == 0) {
				continue;
			}
			strings.add(s);
		}
		return strings;
	}

	public static ArrayList<Float> getFloats(String string) {
		ArrayList<Float> floats = new ArrayList<Float>();
		for (String s : getStrings(string)) {
			floats.add(Float.parseFloat(s));
		}
		return floats;
	}

	public static ArrayList<Float> getFloats(Node node) {
		return getFloats(node.getTextContent());
	}

	public static ArrayList<Integer> getIntegers(Node node) {
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for (String s : getStrings(node)) {
			ints.add(Integer.parseInt(s));
		}
		return ints;
	}

	public static ArrayList<Matrix4f> floatsToMatrix(ArrayList<Float> floats) {
		// TODO Check the order of Matrix filling.
		ArrayList<Matrix4f> matrixList = new ArrayList<Matrix4f>();
		for (int index = 0; index < floats.size(); index += 16) {
			Matrix4f matrix = new Matrix4f();
			// Pretty sure this option is correct.
			boolean swap = true;

			if (swap) {
				for (int i = 0; i < 16; i++) {
					matrix.put(i, floats.get(index + i));
				}
			} else {
				// Swapped axis, no longer used.
				/*
				 * matrix.m00 = floats.get(index + 0);
				 * matrix.m01 = floats.get(index + 1);
				 * matrix.m02 = floats.get(index + 2);
				 * matrix.m03 = floats.get(index + 3);
				 * matrix.m10 = floats.get(index + 4);
				 * matrix.m11 = floats.get(index + 5);
				 * matrix.m12 = floats.get(index + 6);
				 * matrix.m13 = floats.get(index + 7);
				 * matrix.m20 = floats.get(index + 8);
				 * matrix.m21 = floats.get(index + 9);
				 * matrix.m22 = floats.get(index + 10);
				 * matrix.m23 = floats.get(index + 11);
				 * matrix.m30 = floats.get(index + 12);
				 * matrix.m31 = floats.get(index + 13);
				 * matrix.m32 = floats.get(index + 14);
				 * matrix.m33 = floats.get(index + 15);
				 */

			}
			matrixList.add(matrix);
		}
		return matrixList;
	}

	public void addMeshInstance(DAEMeshInstance minst) {
		meshStaticInstance.add(minst);
	}

	public DAEMeshInstance getMeshInstance(String modelName) {
		for (DAEMeshInstance minst : meshStaticInstance) {
			// System.out.println(minst.getName());
			if (minst.getName().equalsIgnoreCase(modelName)) {
				return minst;
			}
		}
		return null;
	}

	public ArrayList<String> getMeshInstNames() {
		ArrayList<String> meshNames = new ArrayList<String>();
		for (DAEMeshInstance minst : meshStaticInstance) {
			meshNames.add(minst.getName());
		}
		return meshNames;
	}

	public ArrayList<String> getAnimMeshNames() {
		ArrayList<String> meshNames = new ArrayList<String>();
		for (DAEAnimatedMesh mesh : meshAnim) {
			meshNames.add(mesh.getName());
		}
		return meshNames;
	}

	public ArrayList<String> getAnimationNames() {
		ArrayList<String> animNames = new ArrayList<String>();
		for (DAEAnimClip a : this.animationClips) {
			animNames.add(a.id);
		}
		return animNames;
	}

}
