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
import com.opengrave.og.util.Vector4f;

public class DAESource {

	public DAESource clone() {
		DAESource clone = new DAESource();
		for (Vector4f value : values) {
			clone.values.add(value);
		}
		clone.stride = stride;
		clone.size = size;
		clone.id = id;
		return clone;
	}

	ArrayList<Vector4f> values = new ArrayList<Vector4f>();
	int stride = 0, size = 0;
	String id;
	public boolean isNorm;

	public DAESource(Element source) {
		id = source.getAttribute("id");
		isNorm = id.toLowerCase().contains("norm");
		stride = XML.getChildCount(XML.getChild(XML.getChild(source, "technique_common"), "accessor"), "param");
		// System.out.println(id + " Source has " + stride + " stride");
		if (stride == 0) {
			stride = 3;
		} // Welp. There's a random guess!
		Element floats = XML.getChild(source, "float_array");

		ArrayList<Float> floatList = DAEFile.getFloats(floats);
		int meantToHave = Integer.parseInt(floats.getAttribute("count"));
		if (meantToHave != floatList.size()) {
			System.out.println("Meant to have " + meantToHave + " but only found " + floatList.size());
		}
		int pos = 0;
		while (pos < floatList.size()) {
			Vector4f vec = new Vector4f();
			if (stride >= 2) {
				vec.x = floatList.get(pos++);
				vec.y = floatList.get(pos++);
			}
			if (stride >= 3) {
				vec.z = floatList.get(pos++);
			}
			if (stride >= 4) {
				vec.w = floatList.get(pos++);
			} else {
				if (isNorm) {
					vec.w = 0f;
				} else {
					vec.w = 1f;
				}
			}
			size++;
			values.add(vec);
		}
	}

	public DAESource() {
	}
}
