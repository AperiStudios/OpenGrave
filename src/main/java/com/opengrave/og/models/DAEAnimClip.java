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

public class DAEAnimClip {

	public float start, end;
	public String id;
	public ArrayList<DAEAnimation> animations = new ArrayList<DAEAnimation>();

	public DAEAnimClip(DAEFile file, Node node) {
		id = ((Element) node).getAttribute("name");
		start = Float.parseFloat(((Element) node).getAttribute("start"));
		end = Float.parseFloat(((Element) node).getAttribute("end"));
		for (Element e : XML.getChildren(node, "instance_animation")) {
			String name = e.getAttribute("url").substring(1);
			for (DAEAnimation da : file.animations) {
				if (da.id == null) {
					continue;
				}
				if (da.id.equalsIgnoreCase(name)) {
					animations.add(da);
				}
			}
		}

	}

}
