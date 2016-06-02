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

import com.opengrave.og.Util;
import com.opengrave.og.engine.AnimatedObject;
import com.opengrave.og.engine.Surface;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class BipedWalk extends Walk {

	private Bone leg1, leg2;

	private AnimatedObject obj;
	private Gait gait;

	private Bone legPlanted = null, otherLeg = null;
	private Vector3f p;

	private int coolDown = 0;
	private int fails = 0;

	public BipedWalk(AnimatedObject obj, Bone leg1, Bone leg2) {
		System.out.println(leg1.jointName + " " + leg2.jointName);
		this.leg2 = leg2.getEndBone();
		this.leg1 = leg1.getEndBone();
		this.obj = obj;

		Skeleton skele = obj.getSkeleton();

		Bone boneHL1 = skele.getBone("Hip.left");
		boneHL1.yC = new AngleConstraint(-15f, 15f);
		boneHL1.zC = new AngleConstraint(-5f, 5f);
		// boneHL1.xC = new AngleConstraint(-5f, 5f);
		// Upper
		Bone boneHL2 = skele.getBone("UpperLeg.left");
		boneHL2.zC = new AngleConstraint(-80f, 80f);
		boneHL2.xC = new AngleConstraint(-20f, 40f);
		// Lower
		Bone boneHL3 = skele.getBone("LowerLeg.left");
		boneHL3.xC = new AngleConstraint(0f, 150f);

		// Hip
		Bone boneHR1 = skele.getBone("Hip.right");
		boneHR1.yC = new AngleConstraint(-15f, 15f);
		boneHR1.zC = new AngleConstraint(-5f, 5f);
		// boneHR1.xC = new AngleConstraint(-5f, 5f);
		// Upper
		Bone boneHR2 = skele.getBone("UpperLeg.right");
		boneHR2.zC = new AngleConstraint(-80f, 80f);
		boneHR2.xC = new AngleConstraint(-20f, 40f);
		// Lower
		Bone boneHR3 = skele.getBone("LowerLeg.right");
		boneHR3.xC = new AngleConstraint(0f, 150f);

		gait = new BipedGait();
	}

	public void update(float delta) {
		if (leg1 == null || leg2 == null) {
			return;
		}
		obj.skele.root.setWorldMatrixRecurse(Util.createMatrixFor(obj.location, null, null, null));
		if (legPlanted == null) {
			// No leg is planted. Assume no movement has happened yet
			plantLeg(leg1.getEndBone(), null);
			obj.skele.location.setZ(-0.3f);
		} else {
			// Get Model co-ordinate of pinned location
			// TODO Compare current location to bones default location, not center of obj
			Vector4f pin = Util.createMatrixFor(obj.location, null, null, null).inverse(null).mult4(new Vector4f(p.x, p.y, p.z, 1f), null);
			boolean pinSwap = false;
			if (Math.abs(pin.y) > gait.getStride(1f)) {
				pinSwap = true;
			} else if (Math.abs(pin.x) > gait.getSideStride(1f)) {
				pinSwap = true;
			}
			legPlanted.IK(2, obj, p);

			Vector3f otherLegLoc = obj.location.toVector3().sub(p, null);
			otherLegLoc = otherLegLoc.add(obj.location.toVector3(), null);
			Surface s = obj.getSurface();
			if (s != null) {
				otherLegLoc.z = s.getHeight(otherLegLoc.x, otherLegLoc.y);
			}
			otherLeg.IK(2, obj, otherLegLoc);
			if (pinSwap) {
				if (coolDown > 0) {
					fails++;
					if (fails > 5) {
						System.out.println("Armature pinning failed - reseting");
						removePlant();
						fails = 0;
						coolDown = 0;
					}
					return;
				}
				coolDown = 10;
				if (legPlanted == leg1.getEndBone()) {
					plantLeg(leg2.getEndBone(), null);
				} else {
					plantLeg(leg1.getEndBone(), null);
				}
			}
			if (coolDown > 0) {
				coolDown--;
			}

			// gait.moveOther(otherLeg,pin);

		}
	}

	public void plantLeg(Bone leg, Vector3f newLoc) {

		legPlanted = leg.getEndBone();
		if (legPlanted == leg1) {
			otherLeg = leg2;
		} else {
			otherLeg = leg1;
		}
		if (newLoc == null) {
			p = leg.getEndBone().getWorldPosition();
		} else {
			p = newLoc;
		}
		// p = new Vector3f(0f,0f,1f);
		// System.out.println("Biped walk leg "+leg.jointName+" planted at "+p);
	}

	@Override
	public void removePlant() {
		p = null;
		legPlanted = null;
		otherLeg = null;
	}

}
