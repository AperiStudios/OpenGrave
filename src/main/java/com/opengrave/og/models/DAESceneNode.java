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

public class DAESceneNode {

	boolean valid = false;
	DAESceneNode parent = null;
	public ArrayList<DAESceneNode> children = new ArrayList<DAESceneNode>();
	public Matrix4f total = new Matrix4f();
	public Matrix4f local = new Matrix4f();
	public String nodeName = null;

	@Override
	public boolean equals(Object object) {
		if (object instanceof DAESceneNode) {

			DAESceneNode other = (DAESceneNode) object;
			// System.out.println("Comparing bones "+this.nodeName+":"+this.children.size()+" and "+other.nodeName+":"+other.children.size());
			if (other.nodeName.equals(this.nodeName)) {
				return other.children.size() == this.children.size();
				// Close enough
			}
			// System.out.println("Node Names do not match");
			return false;
		}
		// System.out.println("Types do not match");
		return false;
	}

	public DAESceneNode() {
	}

	public static DAESceneNode make(DAEFile model, DAESceneNode parent, Element node) {
		DAESceneNode sn = new DAESceneNode();
		if (node.getAttribute("type").equalsIgnoreCase("JOINT")) {
			sn.joint = ((Element) node).getAttribute("sid");
			sn.jointName = ((Element) node).getAttribute("name");
		}
		sn.parent = parent;
		if (!node.getNodeName().equalsIgnoreCase("node") && !node.getNodeName().equalsIgnoreCase("visual_scene")) {
			// System.out.println(node.getNodeName()+" is not a node");
			return null;
		}
		sn.nodeName = ((Element) node).getAttribute("id");
		Element matrix = XML.getChild(node, "matrix");
		Element geom = XML.getChild(node, "instance_geometry");
		Element controller = XML.getChild(node, "instance_controller");

		if (matrix == null) {
			// System.out.println("Has no local matrix. Assume Ident");
		} else {
			sn.local = DAEFile.readMatrix(matrix.getTextContent());
		}

		sn.total = sn.parent.total.mult(sn.local, null);

		parent.children.add(sn);
		sn.valid = true;

		// Apply to geometries if needed
		if (geom != null) { // It's a static mesh intended for drawing static
			DAEMesh obj = model.getMesh(geom.getAttribute("url").substring(1));

			if (obj != null) {
				DAEMeshInstance minst = new DAEMeshInstance(node.getAttribute("name"), obj, sn.total);
				model.addMeshInstance(minst);
				// obj.setMatrix(sn.total);

			}
		}
		if (controller != null) { // It's a skeleton to attach to an otherwise
									// static mesh for drawing animated
			Node skele = XML.getChild(controller, "skeleton");
			// Assume first is best. Bad assumption?
			String skeleNode = null;
			if (skele != null) {
				skeleNode = skele.getTextContent().substring(1);

			} else {

			}
			String skinNode = controller.getAttribute("url").substring(1);
			model.addController(node.getAttribute("id"), skeleNode, skinNode, sn.total);

		}

		// Do all children recursively. This sounds so wrong
		for (Element cNode : XML.getChildren(node, "node")) {
			DAESceneNode.make(model, sn, cNode);
		}
		return sn;
	}

	public DAESceneNode getNodeId(String name) {
		if (nodeName != null && nodeName.equalsIgnoreCase(name)) {
			return this;
		}
		for (DAESceneNode child : children) {
			DAESceneNode x = child.getNodeId(name);
			if (x != null) {
				return x;
			}
		}
		return null;
	}

	public String joint = null, jointName = null;
	public Matrix4f skinningMatrix, inverseBindMatrix, animatedWorldMatrix;// ,
																			// animatedLocalMatrix;
	public int index = -1;
	// public DAEAnimChannelCollection animation = null;
	public ArrayList<DAEAnimClip> animation = null;

	/**
	 * Return a DAESceneNode/DAEBone that is a bone which was named the same as
	 * input name. Recurses through children, returning null if no node-bone had
	 * that name
	 * 
	 * @param name
	 * @return
	 */
	public DAESceneNode getBone(String name) {

		if (joint != null && joint.equalsIgnoreCase(name)) {
			return this;
		}
		for (DAESceneNode child : children) {
			DAESceneNode cResult = child.getBone(name);
			if (cResult != null) {
				return cResult;
			}
		}
		return null;
	}

	public DAESceneNode getBoneNamed(String name) {

		if (jointName != null && jointName.equalsIgnoreCase(name)) {
			return this;
		}
		for (DAESceneNode child : children) {
			DAESceneNode cResult = child.getBoneNamed(name);
			if (cResult != null) {
				return cResult;
			}
		}
		return null;
	}

	public DAESceneNode getBone(int index) {
		if (this.index == index) {
			return this;
		}
		for (DAESceneNode child : children) {
			DAESceneNode cResult = child.getBone(index);
			if (cResult != null) {
				return cResult;
			}
		}
		return null;
	}

	public void setWorldMatrix(Matrix4f world) {
		animatedWorldMatrix = world;
		skinningMatrix = world.mult(inverseBindMatrix, null);
		// skinningMatrix = Matrix4f.mul(world, new Matrix4f(), null);
	}

	public void applyAnimations(ArrayList<DAEAnimClip> animation) {
		for (DAESceneNode child : children) {
			this.animation = animation;
			child.applyAnimations(animation);
		}
	}

	/*
	 * public void applyAnimations(ArrayList<DAEAnimation> animation) { for
	 * (DAEAnimation anim : animation) { if
	 * (anim.channels.containsKey(nodeName)) { if (this.animation != null) {
	 * System.out.println(nodeName + " node replaced animation... Bone " +
	 * joint); System.out.println(anim.channels.get(nodeName).target +
	 * " is replacing " + this.animation.target); } this.animation =
	 * anim.channels.get(nodeName); } } if (this.animation == null) { //
	 * System.out.println(nodeName+" : no animation channel given"); } for
	 * (DAESceneNode child : children) { child.applyAnimations(animation); } }
	 */

	public static DAESceneNode createSkeleton(DAESceneNode root) {
		DAESceneNode skeleton = root.clone(null);
		skeleton.local = new Matrix4f();
		skeleton.total = new Matrix4f();

		for (DAESceneNode child : root.children) {
			addChildToSkeleton(skeleton, root, new Matrix4f(), child);
		}

		return skeleton;
	}

	public static void addChildToSkeleton(DAESceneNode newParent, DAESceneNode parent, Matrix4f runningMatrix, DAESceneNode node) {
		if (node.joint == null) {
			// This isn't a bone and should not be added
			// But check children for bones
			Matrix4f nextMat = runningMatrix.mult(parent.local, null);
			// System.out.println("Matrix passed on from Non-Joint Node "+nextMat);
			for (DAESceneNode child : node.children) {
				addChildToSkeleton(newParent, parent, nextMat, child);
			}
		} else {
			DAESceneNode nNode = node.clone(newParent);
			nNode.local = node.local.mult(runningMatrix, null);
			nNode.total = nNode.local.mult(newParent.total, null);
			for (DAESceneNode child : node.children) {
				addChildToSkeleton(nNode, node, new Matrix4f(), child);
			}
		}
	}

	/**
	 * Create a clone for Node -> Skeleton Bone formation
	 */
	public DAESceneNode clone(DAESceneNode newParent) {
		DAESceneNode copy = new DAESceneNode();
		copy.parent = newParent;
		if (newParent != null) {
			newParent.children.add(copy);
		}

		copy.animatedWorldMatrix = animatedWorldMatrix;
		copy.animation = animation;
		copy.index = index;
		copy.inverseBindMatrix = inverseBindMatrix;
		copy.joint = joint;
		copy.jointName = jointName;
		copy.local = null;
		copy.nodeName = nodeName;
		copy.skinningMatrix = skinningMatrix;
		copy.total = null;
		copy.valid = valid;
		return copy;
	}

	public String toString(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append("  ");
		}
		sb.append(joint);
		if (children.size() > 0) {
			sb.append("{").append("\n");

			for (DAESceneNode node : children) {
				sb.append(node.toString(indent + 1));
			}

		} else {
			sb.append("\n");
		}
		for (int i = 0; i < indent; i++) {
			sb.append("  ");
		}
		sb.append("}").append("\n");
		return sb.toString();

	}

	public String toString() {
		return toString(0);
	}

	public int getBoneCount() {
		int i = 1;
		for (DAESceneNode child : children) {
			i += child.getBoneCount();
		}
		return i;
	}
}
