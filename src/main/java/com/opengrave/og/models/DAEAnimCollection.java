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

import com.opengrave.og.base.RenderableBoneAnimatedStatic;
import com.opengrave.og.engine.gait.Skeleton;
import com.opengrave.og.resources.Resources;

public class DAEAnimCollection {

	RenderableBoneAnimatedStatic renderable;

	public DAEAnimCollection(String label) {
		String[] split = label.split(":", 2);
		renderable = new RenderableBoneAnimatedStatic();
		Skeleton skeleton = null;
		if (split.length == 2) {
			DAEFile f = Resources.loadModelFile(split[0]);
			String[] modelNames = split[1].split(":");
			int matNum = 0;

			for (String modelList : modelNames) {
				for (String modelData : modelList.split(",")) {

					DAEAnimatedMesh mesh = f.getAnimMesh(modelData);
					if (mesh == null) {
						System.out.println("Error cannot find mesh '" + modelData + "'");
						continue;
					}
					if (mesh.skeleton != null) {
						if (!mesh.skeleton.equals(skeleton) && skeleton != null) {
							throw (new RuntimeException("Multiple Skeletons in animated model '" + label + "'"));
						}
						skeleton = new Skeleton(mesh.skeleton);
					}

					mesh.addGeom(renderable, (float) matNum);
				}
				matNum++;
			}
			renderable.setSkeleton(skeleton);
		} else {
			System.out.println("Unknown static model label : " + label);
		}
	}

	public RenderableBoneAnimatedStatic getRenderable() {
		return renderable;
	}

}
