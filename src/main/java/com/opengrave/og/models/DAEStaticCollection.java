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

import com.opengrave.common.world.MaterialList;
import com.opengrave.og.base.Renderable3DStatic;
import com.opengrave.og.resources.Resources;

public class DAEStaticCollection {

	MaterialList matList;
	Renderable3DStatic renderable = new Renderable3DStatic();

	public DAEStaticCollection(String label) {
		String[] split = label.split(":");
		if (split.length >= 2) {
			DAEFile f = Resources.loadModelFile(split[0]);
			int matNum = 0;
			for (int i = 1; i < split.length; i++) {
				String s = split[i];
				String[] modelList = s.split(",");
				for (String modelName : modelList) {
					DAEMeshInstance mesh = f.getMeshInstance(modelName);
					if (mesh == null) {
						System.out.println("Error cannot find mesh '" + modelName + "'");
						continue;
					}
					mesh.addGeom(renderable, (float) matNum);
				}
				matNum++;
			}

		} else {
			System.out.println("Unknown static model label : " + label);
		}
	}

	public void setMaterialList(MaterialList mlist) {
		this.matList = mlist;
		renderable.setMaterialList(matList);
	}

	// public void render() {
	// renderable.setMaterialList(matList);
	// renderable.render();
	// }

	// public void renderForPicking() {
	// renderable.renderForPicking(this);
	// }

	// public void setScale(Vector3f vector3f) {
	// renderable.setScale(vector3f);
	// renderable.update(0f);
	// }

	// public void setLocation(Location location) {
	// renderable.setLocation(location);
	// renderable.update(0f);
	// }

	// public void renderShadows(Shadow shadow) {
	// renderable.renderShadows(shadow);
	// }

	public Renderable3DStatic getRenderable() {
		return renderable;
	}

}
