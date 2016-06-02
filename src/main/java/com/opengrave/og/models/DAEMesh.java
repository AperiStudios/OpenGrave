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
import com.opengrave.og.engine.Location;
import com.opengrave.og.util.Vector3f;

public class DAEMesh {

	String geomName;
	ArrayList<DAETriangleList> trianglesLists = new ArrayList<DAETriangleList>();
	ArrayList<DAESource> sources = new ArrayList<DAESource>();
	DAESource vertexList, alteredVertexList = null;
	DAESource normalList;
	DAESource texCoord;
	DAEFile file;
	public boolean visible = true;
	public Location location = new Location();
	public Vector3f rotation = new Vector3f(0, 0, 0);
	public Vector3f scale = new Vector3f(1, 1, 1);
	public ArrayList<DAEVertexWeighted> weights;

	public DAEMesh(DAEFile file, Node item) {
		geomName = ((Element) item).getAttribute("id");
		this.file = file;
		Element mesh = XML.getChild(item, "mesh");
		if (mesh == null) {
			System.out.println("No Mesh in Geometry");
			return;
		}
		Element vertList = XML.getChild(mesh, "vertices");
		if (vertList == null) {
			System.out.println("No Vertex List");
			return;
		}

		// Read in all Source sections

		for (Element source : XML.getChildren(mesh, "source")) {
			DAESource s = new DAESource(source);
			if (s.isNorm) {
				normalList = s;
			}
			sources.add(s);
		}

		// Mark the Vert list seperatly
		Element vert = XML.getChild(mesh, "vertices");
		if (vert != null) {
			Element input = XML.getChild(vert, "input");
			if (input != null) {
				if (input.getAttribute("semantic").equalsIgnoreCase("POSITION")) {
					String sourceName = input.getAttribute("source").substring(1);
					for (DAESource source : sources) {
						if (source.id.equalsIgnoreCase(sourceName)) {
							vertexList = source;
						}
					}
				}
			}
		}

		// Read each Tri section

		for (Element triangle : XML.getChildren(mesh, "triangles")) {
			trianglesLists.add(new DAETriangleList(this, triangle));
		}
		for (Element triangle : XML.getChildren(mesh, "polylist")) {
			// TODO : Break down Polylists with ngons higher than 3
			trianglesLists.add(new DAETriangleList(this, triangle));
		}

		// System.out.println(trianglesLists.size() +
		// " SubMeshes to one model");

		alteredVertexList = vertexList.clone();
	}

	// public void renderForPicking() {

	// }

	// public void render() {
	// for (DAETriangleList tris : trianglesLists) {
	// tris.render();
	// }
	// }

	public String getName() {
		return geomName;
	}

	// public void setMatrix(Matrix4f matrix) {
	// this.matrix = matrix;
	// for (DAETriangleList tris : trianglesLists) {
	// tris.setMatrix(total);
	// }
	// }

	/*
	 * public void update(float delta) { for (DAETriangleList tris :
	 * trianglesLists) { tris.rotation = rotation; tris.scale = scale;
	 * tris.location = location; tris.visible = visible; tris.update(delta); } }
	 */

	public DAESource getSource(String id) {
		for (DAESource source : sources) {
			if (source.id.equalsIgnoreCase(id)) {
				return source;
			}
		}
		return null;
	}

	/*
	 * public Vector4f getOrigVert(Integer i) { return vertexList.values.get(i);
	 * }
	 * 
	 * public Vector4f getVert(Integer i) { //return vertexList.values.get(i);
	 * return alertedVertexList.values.get(i); }
	 * 
	 * public Vector4f getTex(Integer i) { return new Vector4f(1f, 1f, 1f, 1f);
	 * }
	 * 
	 * public Vector4f getNorm(Integer i) { if (normalList == null) { return new
	 * Vector4f(0f, 0f, 0f, 0f); } return normalList.values.get(i); }
	 */

	// public DAEMaterial getMaterial(String name) {
	// for (DAEMaterial mat : materials) {
	// if (mat.id.equalsIgnoreCase(name)) {
	// return mat;
	// }
	// }
	// return null;
	// }

	// public void setTexture(TextureAtlas atlas) {
	// for (DAETriangleList tris : trianglesLists) {
	// tris.setTexture(atlas);
	// }
	// }

	// public void setColour(Vector4f col) {
	// for (DAETriangleList tris : trianglesLists) {
	// tris.setColour(col);
	// }
	// }

	// public void renderShadow(Shadow shadow) {
	// for (DAETriangleList tris : trianglesLists) {
	// tris.renderShadows(shadow);
	// }

	// }

}
