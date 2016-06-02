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
import org.w3c.dom.Node;

import com.opengrave.common.xml.XML;
import com.opengrave.og.util.Matrix4f;

public class DAEAnimSource {

	ArrayList<Float> floatList = new ArrayList<Float>();
	String id = null;
	String type = null;
	ArrayList<Matrix4f> matrixList = new ArrayList<Matrix4f>();

	public DAEAnimSource(Node source) {
		id = ((Element) source).getAttribute("id");
		Element floatArray = XML.getChild(source, "float_array");
		if (floatArray != null) {
			floatList = DAEFile.getFloats(floatArray);
		}
		Element teq = XML.getChild(source, "technique_common");
		if (teq != null) {
			Element accessor = XML.getChild(teq, "accessor");
			if (accessor != null) {
				Element param = XML.getChild(accessor, "param");
				if (param != null) {
					if (param.getAttribute("type").equalsIgnoreCase("float")) {
						type = "float";
					} else if (param.getAttribute("type").equalsIgnoreCase("float4x4")) {
						type = "matrix";
					}
				}
			}
		}
	}

	public void createMatrixes() {
		if (type.equalsIgnoreCase("matrix")) {
			// Matrix list
			matrixList = DAEFile.floatsToMatrix(floatList);
		} else {
			// Not done here
		}
	}

}
