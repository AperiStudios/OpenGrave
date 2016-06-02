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
import com.opengrave.og.Util;
import com.opengrave.og.base.Renderable3DStatic;
import com.opengrave.og.base.RenderableBoneAnimatedStatic;
import com.opengrave.og.base.Vertex3D;
import com.opengrave.og.base.VertexAnimated;
import com.opengrave.og.engine.Location;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector4f;

public class DAETriangleList {

	ArrayList<Integer> indexOfVert = new ArrayList<Integer>();
	ArrayList<Integer> indexOfTex = new ArrayList<Integer>();
	ArrayList<Integer> indexOfNormal = new ArrayList<Integer>();

	ArrayList<DAEInput> inputs = new ArrayList<DAEInput>();

	DAESource vertexData;
	DAESource normalData;
	DAESource texData;

	DAEMesh mesh;

	private Location location = new Location();

	// int material;

	public DAETriangleList(DAEMesh mesh, Element triangle) {
		this.mesh = mesh;

		// List out inputs
		for (Element input : XML.getChildren(triangle, "input")) {
			DAEInput i = new DAEInput(input);
			i.addSource(mesh.sources); // Link to the named source
			inputs.add(i);
			if (i.type.equals("NORMAL")) {
				normalData = i.source;
			} else if (i.type.equals("TEXCOORD")) {
				texData = i.source;
			}
		}
		vertexData = mesh.vertexList;

		Element p = XML.getChild(triangle, "p");

		ArrayList<Integer> intList = DAEFile.getIntegers(p);
		for (Integer i : intList) {
			// int inputNumber = index % size;
			// DAEInput input = inputs.get(inputNumber);
			// if (input.type.equalsIgnoreCase("VERTEX")) {
			indexOfVert.add(i);
			// } else if (input.type.equalsIgnoreCase("NORMAL")) {
			indexOfNormal.add(i);
			// } else if (input.type.substring(0, 3).equalsIgnoreCase("TEX")) {
			indexOfTex.add(i);
			// }
			// TODO Deal with multi-texture and colours
		}
		// System.out.println("SubMesh Verts : " + indexOfVert.size()
		// + " Normals : " + indexOfNormal.size() + " Texcoords : "
		// + indexOfTex.size());
	}

	public void bake(Renderable3DStatic stat, float materialNum, Matrix4f matrix) {
		Matrix4f m = Util.createMatrixFor(location, null, matrix, null);
		for (int count = 0; count < indexOfVert.size(); count++) {
			Vertex3D vd = new Vertex3D();
			int index = indexOfVert.get(count);
			Vector4f v = vertexData.values.get(index);
			v = m.mult4(v, null); // Get a model-based rather than
									// sub-mesh-based location
			vd.setPos(v);
			Vector4f tx = new Vector4f(0f, 0f, 0f, 0f);
			if (texData != null) {
				tx = texData.values.get(indexOfTex.get(count));
			}
			vd.setTex(new Vector4f(tx.x, 1f - tx.y, materialNum, 0f));
			Vector4f n = normalData.values.get(indexOfNormal.get(count));
			n = m.mult4(n, null);
			vd.setNorm(n);
			// TODO: Care about normals at all. Probably need to check
			// rotation based on above matrix.
			stat.addVertex(vd);
		}
	}

	public void bake(RenderableBoneAnimatedStatic stat, DAEMesh model, float materialNum, Matrix4f matrix) {
		Matrix4f m = Util.createMatrixFor(location, null, matrix, null);
		for (Integer index : indexOfVert) {
			VertexAnimated va = new VertexAnimated();
			Vector4f v = vertexData.values.get(index);
			v = m.mult4(v, null);
			va.setPos(v);
			va.setNorm(normalData.values.get(index));
			Vector4f tx = new Vector4f(0f, 0f, 0f, 0f);
			if (texData != null) {
				tx = texData.values.get(index);
			}
			va.setTex(tx);
			va.setInfluences(model.weights.get(index));
			stat.addVertex(va);
		}
	}

}
