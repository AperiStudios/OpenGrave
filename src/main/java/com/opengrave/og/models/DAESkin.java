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

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.opengrave.common.xml.XML;
import com.opengrave.og.util.Matrix4f;

public class DAESkin {

	ArrayList<String> boneNames = new ArrayList<String>();
	ArrayList<Float> weights = new ArrayList<Float>();
	ArrayList<Matrix4f> transforms = new ArrayList<Matrix4f>();
	ArrayList<DAEVertexWeighted> vertexs = new ArrayList<DAEVertexWeighted>();
	Matrix4f bindShapeMatrix;
	String staticModelName = null;

	public DAESkin(DAEFile file, Element skin, DAESceneNode skeleton) {
		staticModelName = skin.getAttribute("source").substring(1);
		Element bindNode = XML.getChild(skin, "bind_shape_matrix");
		String mat = bindNode.getTextContent();
		bindShapeMatrix = DAEFile.readMatrix(mat);

		// FIIINE. Last Implementation was too exporter-specific.

		Element joints = XML.getChild(skin, "joints");
		// We can't be without this, right?
		for (Element input : XML.getChildren(joints, "input")) {
			Element thing = XML.getElementById(file.document, input.getAttribute("source").substring(1));
			if (input.getAttribute("semantic").equalsIgnoreCase("INV_BIND_MATRIX")) {
				transforms.clear();
				Element matrixList = XML.getChild(thing, "float_array");
				if (matrixList != null) {
					ArrayList<Float> floats = new ArrayList<Float>();
					floats = DAEFile.getFloats(matrixList);
					transforms = DAEFile.floatsToMatrix(floats);
				}

			}
		}
		Element vw = XML.getChild(skin, "vertex_weights");
		for (Element input : XML.getChildren(vw, "input")) {
			Element thing = XML.getElementById(file.document, input.getAttribute("source").substring(1));
			if (input.getAttribute("semantic").equalsIgnoreCase("JOINT")) {
				// It's a list of joints!! Woop!
				boneNames.clear();

				Element nameList = XML.getChild(thing, "Name_array");
				if (nameList != null) {
					boneNames = DAEFile.getStrings(nameList);
				}
			} else if (input.getAttribute("semantic").equalsIgnoreCase("WEIGHT")) {
				// List of weights
				weights.clear();
				Element weightList = XML.getChild(thing, "float_array");
				if (weightList != null) {
					weights = DAEFile.getFloats(weightList);
				}
			}
		}
		if (vw != null) {
			Element vcount = XML.getChild(vw, "vcount");
			Element v = XML.getChild(vw, "v");
			ArrayList<Integer> values = DAEFile.getIntegers(v);
			int indexOfValues = 0;
			for (Integer countString : DAEFile.getIntegers(vcount)) {
				int count = countString * 2; // Number of values to read
				DAEVertexWeighted daevw = new DAEVertexWeighted(count);
				for (int i = 0; i < count; i++) {
					if ((i % 2) == 0) {
						// It's Even, so a Joint ID
						daevw.jointIndex.add(values.get(indexOfValues));
					} else {
						// Odd, A weight
						daevw.weight.add(weights.get(values.get(indexOfValues)));

					}
					indexOfValues++;
				}
				vertexs.add(daevw);

			}
		}
		// Let's dump this info into the skeleton!

		int i = 0;
		for (String boneName : boneNames) {
			DAESceneNode bone = skeleton.getBone(boneName);
			if (bone == null) {
				System.out.println("Error can't find bone : " + boneName);
			} else {
				bone.inverseBindMatrix = transforms.get(i);
				bone.index = i;
				// bone.weights = weights;
				// bone.weightedVertexes = vertexs.get(i);
			}
			i++;
		}
		DAEMesh mesh = file.getMesh(staticModelName);
		mesh.weights = vertexs;
		// mesh.
		// for(VertexAnimated va : mesh.getVertexList()){
		// va.setInfluences(vertexs.get(vert));
		// vert++;
		// }
		// }
	}

}
