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

import org.w3c.dom.Element;

import com.opengrave.common.xml.XML;
import com.opengrave.og.resources.TextureAtlas;

public class DAEMaterial {

	String id;
	String effect;
	String textureName;
	int textureIndex;
	TextureAtlas texture;

	/**
	 * This can be improved when I have a clue how to use all the possible
	 * combinations of data
	 * 
	 * @param file
	 * @param material
	 */
	public DAEMaterial(DAEFile file, Element material) {
		id = material.getAttribute("id");
		Element instEffect = XML.getChild(material, "instance_effect");
		effect = instEffect.getAttribute("url").substring(1);
		Element actualEffect = XML.getElementById(file.document, effect);
		Element profile = XML.getChild(actualEffect, "profile_COMMON");
		if (profile != null) {
			for (Element param : XML.getChildren(profile, "newparam")) {
				Element surface = XML.getChild(param, "surface");
				if (surface != null) {
					Element initFrom = XML.getChild(surface, "init_from");
					if (initFrom != null) {
						textureName = "tex/" + initFrom.getTextContent();
					}
				}
			}
		}

	}
}
