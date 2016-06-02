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

public class DAEController {

	private DAEFile file;
	private String controllerName;
	private String skeletonName;
	public DAESceneNode skeleton;
	public ArrayList<DAESkin> skinList = new ArrayList<DAESkin>();
	public String animName;

	public DAEController(DAEFile file, String skeletonName, String controllerName, String animName) {
		this.file = file;
		this.controllerName = controllerName;
		this.skeletonName = skeletonName;
		this.animName = animName;
	}

	public void prepare(DAESceneNode topLevel) {
		if (skeletonName == null) {
			skeleton = topLevel;
		} else {
			skeleton = topLevel.getNodeId(skeletonName);
		}
		skeleton = DAESceneNode.createSkeleton(skeleton); // Cleans out
															// type="NODE" nodes
															// and re-creates
															// the matrices to
															// match
		Element controller = XML.getElementById(file.document, controllerName);

		// Get a list of skins

		for (Element skin : XML.getChildren(controller, "skin")) {
			skinList.add(new DAESkin(file, skin, skeleton));
		}

	}
}
