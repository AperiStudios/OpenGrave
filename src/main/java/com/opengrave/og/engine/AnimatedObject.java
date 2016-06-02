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
package com.opengrave.og.engine;

import java.util.ArrayList;
import java.util.Collections;

import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.base.RenderableBoneAnimatedStatic;
import com.opengrave.og.engine.gait.BipedWalk;
import com.opengrave.og.engine.gait.Bone;
import com.opengrave.og.engine.gait.Skeleton;
import com.opengrave.og.engine.gait.Walk;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.models.DAEAnimClip;
import com.opengrave.og.models.DAEAnimCollection;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Quaternion;
import com.opengrave.og.util.Vector4f;

public class AnimatedObject extends BaseObject {

	private ArrayList<AnimatedObjectAnimation> animations = new ArrayList<AnimatedObjectAnimation>();
	// float speed = 0.0005f;
	private ArrayList<Matrix4f> skinningMatrixList;
	int boneCount = 128;
	public Skeleton skele;
	public Walk walk;

	// public AnimatedObject(RenderableBoneAnimatedStatic coll) {
	// this.renderable = coll;
	// boneCount = renderable.getSkeleton().getBoneCount();
	// }

	public Matrix4f getMatrix() {
		return new Matrix4f();
	}

	public AnimatedObject(CommonObject cobj) {
		super(cobj);
	}

	public void doUpdate(float delta) {
		// TODO Apply walking and gaits somewhere more sensible
		if (walk == null && this.cobj.getModelLabel().startsWith("mod/craig")) {
			setWalk(new BipedWalk(this, getSkeleton().getBone("UpperLeg.left"), getSkeleton().getBone("UpperLeg.right")));
		}
		moveBasedOnPath(delta);
		lookAt();
		ArrayList<AnimatedObjectAnimation> delete = new ArrayList<AnimatedObjectAnimation>();
		for (AnimatedObjectAnimation aoa : animations) {
			if (aoa.delete) {
				System.out.println("Animation ended naturally : " + aoa.name);
				delete.add(aoa);
			} else {
				aoa.update(delta);
			}

		}
		for (AnimatedObjectAnimation aoa : delete) {
			animations.remove(aoa);
		}
		// animations.addTime(delta * 0.05f);
		if (skele != null) {
			skele.location = location;
		}
		setupSkeleton(delta);
		renderable.update(delta);
	}

	public void setMaterialList(MaterialList matList) {
		this.matList = matList;
	}

	private void setupSkeleton(float delta) {
		skinningMatrixList = new ArrayList<Matrix4f>();
		for (int i = 0; i < boneCount; i++) {
			skinningMatrixList.add(new Matrix4f());
		}
		renderable.getBoundingBox().clear();
		// Pass one - position relative to ident matrix
		Skeleton skel = ((RenderableBoneAnimatedStatic) renderable).getSkeleton();
		if (skel == null || skel.root == null) {
			return;
		}
		positionBone(skel.root, new Matrix4f());
		if (walk != null) {
			walk.update(delta);
			setSkinningMatrixForBone(skele.root);
		}
	}

	private void positionBone(Bone bone, Matrix4f parentWorld) {
		if (bone == null) {
			return;
		}
		Matrix4f local = null;
		Collections.sort(animations);
		// First pass - find the highest priority anim that cares about this
		// bone.
		ArrayList<AnimatedObjectAnimation> mergedAnims = new ArrayList<AnimatedObjectAnimation>();
		for (AnimatedObjectAnimation aoa : animations) {
			// if (aoa.name.equalsIgnoreCase("walk")
			// || aoa.name.equalsIgnoreCase("run")) {
			// mergedAnims.add(aoa);
			// continue;
			// }
			local = aoa.getMatrixForBone(bone);
			if (aoa.isImportant(bone)) {
				break;
			}
		}
		// Ignore. Can't seem to get this block to perform
		if (false == true && local == null && mergedAnims.size() > 0) {
			float tW = 0f;
			for (AnimatedObjectAnimation aoa : mergedAnims) {
				tW += aoa.blendPerc;
			}
			Vector4f trans = new Vector4f();
			Quaternion quat = null; // makeQuat(mat);
			for (AnimatedObjectAnimation aoa : mergedAnims) {
				Quaternion quat2 = new Quaternion();
				Matrix4f matBone = aoa.getMatrixForBone(bone);
				quat2.setFromMatrix(matBone);

				if (trans == null) {
					trans = new Vector4f(matBone.get(0, 3), matBone.get(1, 3), matBone.get(2, 3), matBone.get(3, 3));
				}
				// aoa.getMatrixForBone(bone).
				// Vector4f quat2 = makeQuat(aoa.getMatrixForBone(bone));
				// quat2.x = quat2.x * (aoa.blendPerc/tW);
				// quat2.y = quat2.y * (aoa.blendPerc/tW);
				// quat2.z = quat2.z * (aoa.blendPerc/tW);
				// quat2.w = quat2.w * (aoa.blendPerc/tW);
				if (quat == null) {
					quat = quat2;
				} else {
					nlerp(quat, quat2, tW);
				}
				// quat = Vector4f.add(quat, quat2, null);
			}
			// Quaternion.
			local = quatToMatrix(quat);
			local.set(0, 3, trans.x);
			local.set(1, 3, trans.y);
			local.set(2, 3, trans.z);
			local.set(3, 3, trans.w);
		}

		// Second pass - find out if an anim poses this in the same direction
		// all the way through
		// TODO : Test if the exact same transform is used in every anim - a
		// surefire way to correctly pose the model
		if (local == null) {
			if (animations.size() > 0) {
				local = animations.get(animations.size() - 1).getMatrixForBone(bone);
			}
		}
		// Final fallback - bind pose of bone. Most likely wrong since the
		// exporter assumed y+ as up.
		if (local == null) {
			local = bone.local;
		}
		Matrix4f world = parentWorld.mult(local, null);
		bone.setWorldMatrix(local, world);
		Vector4f v = world.mult4(new Vector4f(0f, 0f, 0f, 0f), null);
		renderable.getBoundingBox().addVector4f(v);
		skinningMatrixList.set(bone.index, bone.skinningMatrix);
		for (Bone child : bone.getChildren()) {
			positionBone(child, world);
		}
	}

	public void setSkinningMatrixForBone(Bone bone) {
		skinningMatrixList.set(bone.index, bone.skinningMatrix);
		for (Bone child : bone.getChildren()) {
			setSkinningMatrixForBone(child);
		}
	}

	private void nlerp(Quaternion quat, Quaternion quat2, float blend) {
		float dot = quat.dot(quat2);
		float blendI = 1.0f - blend;
		if (dot < 0.0f) {
			quat.x = blendI * quat.x - blend * quat2.x;
			quat.y = blendI * quat.y - blend * quat2.y;
			quat.z = blendI * quat.z - blend * quat2.z;
			quat.w = blendI * quat.w - blend * quat2.w;
		} else {
			quat.x = blendI * quat.x + blend * quat2.x;
			quat.y = blendI * quat.y + blend * quat2.y;
			quat.z = blendI * quat.z + blend * quat2.z;
			quat.w = blendI * quat.w + blend * quat2.w;
		}
		quat = quat.normalise(null);
	}

	public final Matrix4f quatToMatrix(Quaternion q) {
		Matrix4f m = new Matrix4f();
		float sqw = q.w * q.w;
		float sqx = q.x * q.x;
		float sqy = q.y * q.y;
		float sqz = q.z * q.z;

		// invs (inverse square length) is only required if quaternion is not
		// already normalised
		float invs = 1 / (sqx + sqy + sqz + sqw);
		m.set(0, 0, (sqx - sqy - sqz + sqw) * invs); // since sqw + sqx + sqy + sqz
		// =1/invs*invs
		m.set(1, 1, (-sqx + sqy - sqz + sqw) * invs);
		m.set(2, 2, (-sqx - sqy + sqz + sqw) * invs);

		float tmp1 = q.x * q.y;
		float tmp2 = q.z * q.w;
		m.set(1, 0, 2f * (tmp1 + tmp2) * invs);
		m.set(0, 1, 2f * (tmp1 - tmp2) * invs);

		tmp1 = q.x * q.z;
		tmp2 = q.y * q.w;
		m.set(2, 0, 2f * (tmp1 - tmp2) * invs);
		m.set(0, 2, 2f * (tmp1 + tmp2) * invs);
		tmp1 = q.y * q.z;
		tmp2 = q.x * q.w;
		m.set(2, 1, 2f * (tmp1 + tmp2) * invs);
		m.set(1, 2, 2f * (tmp1 - tmp2) * invs);
		return m;
	}

	public AnimatedObjectAnimation getAnimation(String label) {
		for (AnimatedObjectAnimation aoa : animations) {
			if (aoa.name.equalsIgnoreCase(label)) {
				return aoa;
			}
		}
		return null;
	}

	public void startAnimation(String label, float speed, boolean once) {
		boolean blend = false;
		if (label.equalsIgnoreCase("walk") || label.equalsIgnoreCase("run")) {
			return;
			// These should technically be mutually exclusive. We'll attempt to
			// blend between them
			// blend = true;
		}
		System.out.println("Start Anim request : " + label);
		if (getAnimation(label) != null) {
			getAnimation(label).setSpeed(speed);
			getAnimation(label).setStopNextRound(once);
			return;
		}
		System.out.println("Not already running : " + label);
		DAEAnimClip animClip = getAnimClip(label);
		if (animClip == null) {
			return;
		}
		System.out.println("Starting... : " + label);
		if (blend) {
			AnimatedObjectAnimation anim = getAnimation("walk");
			if (anim != null) {
				anim.blendOut(3f);
			}
			anim = getAnimation("idle");
			if (anim != null) {
				anim.blendOut(3f);
			}
			anim = getAnimation("run");
			if (anim != null) {
				anim.blendOut(3f);
			}
		}
		AnimatedObjectAnimation aoa = new AnimatedObjectAnimation(animClip, speed, ((RenderableBoneAnimatedStatic) renderable).getSkeleton());
		if (blend) {
			aoa.blendIn(3f);
		}
		aoa.once = once;
		animations.add(aoa);
		System.out.println("Animation started : " + label);

	}

	public void stopAnimation(String label) {
		AnimatedObjectAnimation anim = getAnimation(label);
		if (anim != null) {
			animations.remove(anim);
			System.out.println("Animation stopped : " + label);
		}
	}

	public DAEAnimClip getAnimClip(String label) {
		for (DAEAnimClip clip : ((RenderableBoneAnimatedStatic) renderable).getSkeleton().root.animation) {
			int loc = clip.id.lastIndexOf("-");
			String bit = clip.id;
			if (loc > 0) {
				bit = clip.id.substring(0, loc);
			}
			if (bit.equalsIgnoreCase(label)) {
				return clip;
			}
		}
		return null;
	}

	@Override
	public void doRender(Matrix4f parent) {
		if (!visible) {
			return;
		}
		renderable.setMaterialList(matList);
		((RenderableBoneAnimatedStatic) renderable).setSkinningMatrix(skinningMatrixList);
		renderable.setContext(context);

		renderable.render(parent, style);
	}

	@Override
	public void doRenderShadows(Matrix4f parent, Shadow shadow) {
		((RenderableBoneAnimatedStatic) renderable).setSkinningMatrix(skinningMatrixList);
		renderable.setContext(context);
		renderable.renderShadows(parent, shadow);
	}

	@Override
	public void doRenderForPicking(Matrix4f parent) {
		((RenderableBoneAnimatedStatic) renderable).setSkinningMatrix(skinningMatrixList);
		renderable.setContext(context);
		renderable.renderForPicking(parent, this);
	}

	@Override
	public void renderableLabelChanged(String s) {
		DAEAnimCollection f = Resources.getAnimatedModel(s);
		this.renderable = f.getRenderable();
		if (f.getRenderable().getSkeleton() == null) {
			System.out.println("No skeleton for anim model");
		} else {
			this.skele = f.getRenderable().getSkeleton();
		}
	}

	@Override
	public String getType() {
		return "animated";
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f matrix) {
	}

	@Override
	public RenderView getContext() {
		return context;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return renderable.getBoundingBox();
	}

	public RenderableBoneAnimatedStatic getRenderable() {
		return (RenderableBoneAnimatedStatic) renderable;
	}

	public Skeleton getSkeleton() {
		return skele;
	}

	public void setWalk(Walk walk) {
		this.walk = walk;
	}

	public Walk getWalk() {
		return walk;
	}

	public Surface getSurface() {
		return s;
	}

}
