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

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.og.engine.gait.Bone;
import com.opengrave.og.engine.gait.Skeleton;
import com.opengrave.og.models.DAEAnimChannelCollection;
import com.opengrave.og.models.DAEAnimClip;
import com.opengrave.og.models.DAEAnimation;
import com.opengrave.og.util.Matrix4f;

public class AnimatedObjectAnimation implements Comparable<AnimatedObjectAnimation> {
	float blendPerc = 0f;
	float endBlendPerc = 1f;
	float blendTime = 0f;
	int priority = 1000;
	boolean delete = false, once = false;
	float poseTime = 0f;
	String name;
	DAEAnimClip clip;
	float speed;

	public void blend(float delta) {
		if (blendPerc < endBlendPerc) {
			blendPerc += delta * (blendTime / 1000f);
			// Don't delete after a blend in.
			// if(blendPerc >= endBlendPerc){
			// delete=true;
			// }
		} else {
			blendPerc -= delta * (blendTime / 1000f);
			if (blendPerc <= endBlendPerc) {
				delete = true;
			}
		}
	}

	public void blendOut(float time) {
		endBlendPerc = 0f;
		this.blendTime = time;
	}

	public void blendIn(float time) {
		endBlendPerc = 1f;
		this.blendTime = time;
	}

	public AnimatedObjectAnimation(DAEAnimClip animClip, float speed, Skeleton skeleton) {
		name = animClip.id;
		this.clip = animClip;
		poseTime = clip.start;
		if (name.contains("-")) {
			String[] bits = name.split("-");
			if (bits.length > 1) {
				String number = bits[bits.length - 1];
				Integer i = null;
				try {
					i = Integer.parseInt(number);
				} catch (NumberFormatException e) {
					new DebugExceptionHandler(e);
				}
				if (i != null) {
					priority = i;
					name = "";
					String split = "";
					for (int i2 = 0; i2 < bits.length - 1; i2++) {
						name = name + bits[i2] + split;
						split = "-";
					}
				}
			}
		}
		// System.out.println("Starting Anim '"+name+"' priority:"+priority);
	}

	public void update(float delta) {
		// TODO Start time?
		blend(delta);
		poseTime += delta * speed;
		if (poseTime > clip.end) {
			// System.out.println("Clipped animation time "+((poseTime %
			// clip.end)+clip.start));
			if (once) {
				delete = true;
			} else {
				poseTime = clip.start + (poseTime % clip.end); // Keep float
																// that was
																// pushed over
																// the 'end'
																// marker
			}
		}

	}

	public Matrix4f getMatrixForBone(Bone bone) {
		if (bone.nodeName == null) {
			return null;
		}
		for (DAEAnimation anim : clip.animations) {
			if (anim.channels.containsKey(bone.nodeName)) {
				DAEAnimChannelCollection a = anim.channels.get(bone.nodeName);
				return a.get(poseTime);
			}
		}
		return null;
	}

	/**
	 * This bone is important to this animation.
	 * 
	 * @param bone
	 * @return
	 */
	public boolean isImportant(Bone bone) {
		if (bone.nodeName == null) {
			return false;
		}
		for (DAEAnimation anim : clip.animations) {
			if (anim.channels.containsKey(bone.nodeName)) {
				DAEAnimChannelCollection a = anim.channels.get(bone.nodeName);
				if (a.ignore == true) {
					return false;
				} else {
					return true;
				}
			}
		}
		return false;
	}

	public int compareTo(AnimatedObjectAnimation arg0) {
		AnimatedObjectAnimation other = (AnimatedObjectAnimation) arg0;
		if (other.priority == this.priority) {
			return this.name.compareTo(other.name);
		}
		return this.priority - other.priority;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public void setStopNextRound(boolean once) {
		this.once = once;
	}

}
