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

public abstract class Gait {
	// public static Leg humanLeft;
	// public static Leg humanRight;
	public static void init() {
		/*
		 * // Human left leg
		 * humanLeft = new Leg();
		 * // Hip
		 * Bone boneHL1 = new Bone();
		 * boneHL1.name = "Hip.left";
		 * boneHL1.yC = new AngleConstraint(345f, 15f);
		 * boneHL1.zC = new AngleConstraint(355f, 5f);
		 * humanLeft.addBone(boneHL1);
		 * // Upper
		 * Bone boneHL2 = new Bone();
		 * boneHL2.name = "UpperLeg.left";
		 * boneHL2.xC = new AngleConstraint(0f, 80f);
		 * boneHL2.yC = new AngleConstraint(355f, 35f);
		 * boneHL1.addBone(boneHL2);
		 * // Lower
		 * Bone boneHL3 = new Bone();
		 * boneHL3.name = "LowerLeg.left";
		 * boneHL3.xC = new AngleConstraint(0f, 280f);
		 * boneHL2.addBone(boneHL3);
		 * 
		 * 
		 * 
		 * humanRight = new Leg();
		 * // Hip
		 * Bone boneHR1 = new Bone();
		 * boneHR1.name = "Hip.right";
		 * boneHR1.yC = new AngleConstraint(345f, 15f);
		 * boneHR1.zC = new AngleConstraint(355f, 5f);
		 * humanRight.addBone(boneHR1);
		 * // Upper
		 * Bone boneHR2 = new Bone();
		 * boneHR2.name = "UpperLeg.right";
		 * boneHR2.xC = new AngleConstraint(0f, 80f);
		 * boneHR2.yC = new AngleConstraint(355f, 35f);
		 * boneHR1.addBone(boneHR2);
		 * // Lower
		 * Bone boneHR3 = new Bone();
		 * boneHR3.name = "LowerLeg.right";
		 * boneHR3.xC = new AngleConstraint(0f, 280f);
		 * boneHR2.addBone(boneHR3);
		 */
	}

	public abstract float getStride(float scaleY);

	public abstract float getSideStride(float scaleX);
}
