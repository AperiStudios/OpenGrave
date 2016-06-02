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
package com.opengrave.og.engine.gait;

import java.util.ArrayList;

import com.opengrave.og.engine.AnimatedObject;
import com.opengrave.og.models.DAEAnimClip;
import com.opengrave.og.util.Matrix3f;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class Bone {
	private ArrayList<Bone> children = new ArrayList<Bone>();
	AngleConstraint xC, yC, zC;
	public int index = -1;
	boolean valid = false;
	Bone parent = null;
	public Matrix4f total = new Matrix4f();
	public Matrix4f local = new Matrix4f(), local2;
	public String nodeName = null;
	public String joint = null, jointName = null;
	public Matrix4f skinningMatrix, inverseBindMatrix, worldMatrix;
	public float length;
	public ArrayList<DAEAnimClip> animation = null;

	public Vector4f jointPoint; // The position of the joint in parents space
	private Vector4f worldPosition; // The position of the origin of the bone in world space

	// Position the furthest bone in this position.
	public boolean IK(int numParents, AnimatedObject obj, Vector3f target) {
		if (getChildren().size() > 0) {
			getChildren().get(0).IK(numParents + 1, obj, target);
		} else {
			int count = 0;
			Vector3f endPos = this.getWorldPosition();
			double dist = target.sub(endPos, null).length();
			double threshold = 0.001;
			Bone curr = this.getParent(); // Start with 2nd bone in system
			int parentcount = 0;
			while (count < 5 * numParents && dist > threshold) {
				// System.out.println("Shaping parent No "+parentcount+" '"+curr.jointName+"'");
				Vector3f currP = curr.getWorldPosition();
				Vector3f v1 = new Vector3f(endPos.x - currP.x, endPos.y - currP.y, endPos.z - currP.z).normalise(null);
				Vector3f v2 = new Vector3f(target.x - currP.x, target.y - currP.y, target.z - currP.z).normalise(null);
				if (Float.isNaN(v1.x) || Float.isNaN(v2.x)) {
					break;
				}
				float dp = v1.dot(v2);
				float angle = (float) Math.acos(dp);
				if (dp >= 1) {
					break;
				}
				Vector3f rotAxis = v1.cross(v2, null).normalise(null);

				Matrix4f mat43 = curr.getParent().worldMatrix.mult(curr.getLocalMatrix(), null);
				// curr.getParent().getMatrix(curr.getMatrix(new Matrix4f())); // TODO parent * local matrix3f
				mat43 = mat43.inverse(null);
				Matrix3f mat3 = new Matrix3f(mat43);

				// Vector3f rotations = MatrixToEuler(mat3);

				// if(xC != null){
				// rotations.x = xC.constrain(rotations.x);
				// }
				// if(yC != null){
				// rotations.y = yC.constrain(rotations.y);
				// }
				// if(zC != null){
				// rotations.z = zC.constrain(rotations.z);
				// }

				// mat3 = EulerToMatrix(rotations);

				rotAxis = mat3.mult3(rotAxis, null);
				rotAxis = rotAxis.normalise(null);
				float angleDeg = (float) Math.toDegrees(angle);
				if (curr.jointName.equalsIgnoreCase("lowerleg.right")) {
					// System.out.println(angleDeg+" "+rotAxis);
				}
				boolean invalid = false;
				if ((rotAxis.x > 0.95f && rotAxis.x < 1.05f) || (rotAxis.x < -0.095f && rotAxis.x > -1.05f)) {
					if (rotAxis.x < 0f) {
						angleDeg = 360f - angleDeg;
						angle = (float) (2f * Math.PI) - angle;
					}
					rotAxis = new Vector3f(1f, 0f, 0f);
					if (curr.xC == null || !curr.xC.isInside(angleDeg)) {
						invalid = true;
					}
				} else if ((rotAxis.y > 0.95f && rotAxis.y < 1.05f) || (rotAxis.y < -0.95f && rotAxis.y > -1.05f)) {
					if (rotAxis.y < 0f) {
						angleDeg = 360f - angleDeg;
						angle = (float) (2f * Math.PI) - angle;
					}
					rotAxis = new Vector3f(0f, 1f, 0f);

					if (curr.yC == null || !curr.yC.isInside(angle)) {
						invalid = true;
					}
				} else if ((rotAxis.z > 0.95f && rotAxis.z < 1.05f) || (rotAxis.z < -0.95f && rotAxis.z > -1.05f)) {
					if (rotAxis.z < 0f) {
						angleDeg = 360f - angleDeg;
						angle = (float) (2f * Math.PI) - angle;
					}
					rotAxis = new Vector3f(0f, 0f, 1f);

					if (curr.zC == null || !curr.zC.isInside(angle)) {
						invalid = true;
					}
				}

				dist = target.sub(getWorldPosition(), null).length();
				if (dist < threshold) {
					return true;
				}
				if (!invalid && !Float.isNaN(rotAxis.x) && rotAxis.length() != 0) {
					Matrix4f rotMat = curr.local2.rotate(angle, rotAxis, null);
					// obj.setBoneMatrix(curr, rotMat);
					curr.rotateBoneMatrix(rotMat);
				}
				endPos = getWorldPosition();

				if (curr.getParent().getParent() == null || parentcount >= numParents) {
					curr = this.getParent();
					parentcount = 0;
				} else {
					curr = curr.getParent();
					parentcount++;
				}
				count++;
			}
			if (dist < threshold) {
				return true;
			}
		}
		return false;

	}

	/*
	 * private Matrix3f EulerToMatrix(Vector3f r) {
	 * Matrix3f a = new Matrix3f();
	 * a.m00 = 1f;
	 * float ac = (float) Math.cos(r.x), as = (float) Math.sin(r.x);
	 * a.m11 = ac;
	 * a.m12 = -as;
	 * a.m21 = as;
	 * a.m22 = ac;
	 * 
	 * Matrix3f b = new Matrix3f();
	 * b.m11 = 1f;
	 * float bc = (float) Math.cos(r.y), bs = (float) Math.sin(r.y);
	 * b.m00 = bc;
	 * b.m20 = bs;
	 * b.m02 = -bs;
	 * b.m22 = bc;
	 * 
	 * Matrix3f c = new Matrix3f();
	 * c.m22 = 1f;
	 * float cc = (float) Math.cos(r.z), cs = (float) Math.sin(r.z);
	 * c.m00 = cc;
	 * c.m10 = -cs;
	 * c.m01 = cs;
	 * c.m11 = cc;
	 * return Matrix3f.mul(Matrix3f.mul(a, b, null), c, null);
	 * }
	 * 
	 * private Vector3f MatrixToEuler(Matrix3f mat3) {
	 * return new Vector3f((float) Math.atan2(mat3.m21, mat3.m22), (float) Math.atan2(mat3.m20, Math.sqrt(mat3.m21 * mat3.m21 + mat3.m22 * mat3.m22)),
	 * (float) Math.atan2(mat3.m10, mat3.m00));
	 * }
	 */

	private Matrix4f getLocalMatrix() {
		if (local2 == null) {
			return local;
		}
		return local2;
	}

	public Bone getRootBone() {
		if (parent == null) {
			return this;
		}
		return parent.getRootBone();
	}

	public Vector3f getWorldPosition() {
		// v = Matrix4f.transform(getWorldMatrix(m), v, null);
		// return new Vector3f(v.x,v.y,v.z);
		return new Vector3f(this.worldPosition.x, this.worldPosition.y, this.worldPosition.z);
	}

	public Bone getBone(String string) {
		if (jointName.equalsIgnoreCase(string)) {
			return this;
		} else {
			for (Bone b : getChildren()) {
				Bone r = b.getBone(string);
				if (r != null) {
					return r;
				}
			}
		}
		return null;
	}

	public Bone getParent() {
		return parent;
	}

	/**
	 * 
	 * @param local
	 *            the local matrix
	 * @param world
	 *            the world matrix of this bone
	 */
	public void setWorldMatrix(Matrix4f local, Matrix4f world) {
		local2 = local;
		skinningMatrix = world.mult(inverseBindMatrix, null);
		worldMatrix = world;
		this.worldPosition = world.mult4(new Vector4f(0f, 0f, 0f, 1f), null);
		for (Bone child : children) {
			child.setWorldMatrixRecurse(world);
		}
	}

	/**
	 * 
	 * @param parentWorld
	 *            the parent bones world matrix. Uses its pre-stored local matrix
	 */
	public void setWorldMatrixRecurse(Matrix4f parentWorld) {
		Matrix4f m = local;
		if (local2 != null) {
			m = local2;
		}
		Matrix4f world = parentWorld.mult(m, null);
		skinningMatrix = world.mult(inverseBindMatrix, null);
		worldMatrix = world;
		this.worldPosition = world.mult4(new Vector4f(0f, 0f, 0f, 1f), null);
		for (Bone child : children) {
			child.setWorldMatrixRecurse(world);
		}

	}

	public ArrayList<Bone> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Bone> children) {
		this.children = children;
	}

	public Bone getEndBone() {
		if (children.size() == 0) {
			return this;
		}
		return children.get(0).getEndBone();
	}

	public void setLocal(Matrix4f m) {
		local2 = m;
		Matrix4f world = getParent().worldMatrix.mult(m, null);
		skinningMatrix = world.mult(inverseBindMatrix, null);
		worldMatrix = world;
		this.worldPosition = world.mult4(new Vector4f(0f, 0f, 0f, 1f), null);
		for (Bone child : children) {
			child.setWorldMatrixRecurse(world);
		}
	}

	public void rotateBoneMatrix(Matrix4f rot) {
		// Matrix4f m = Matrix4f.mul(, m4, null);
		// v = v.normalise(null);
		// Vector3f v3 = new Vector3f(v.x, v.y, v.z);
		Vector3f v3 = new Vector3f(jointPoint.x, jointPoint.y, jointPoint.z);
		Matrix4f m = new Matrix4f();
		m = m.translate(v3, null);
		m = m.mult(rot, null);

		setLocal(m);

		// Matrix4f world = Matrix4f.mul(b.getParent().get, m, null);
		// for(Bone child : b.getChildren()){
		// child.setWorldMatrixRecurse(world);
		// }
	}

}
