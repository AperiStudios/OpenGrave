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
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengrave.og.util.Matrix4f;

public class DAEAnimChannelCollection {

	public boolean ignore = false;
	ArrayList<DAEAnimChannel> channels = new ArrayList<DAEAnimChannel>();
	public ArrayList<DAEAnimKeyframeData> data = new ArrayList<DAEAnimKeyframeData>();
	Pattern numberInBrackets;
	public String target;

	public void add(DAEAnimChannel animchannel) {
		channels.add(animchannel);
	}

	public void prepare() {
		if (numberInBrackets == null) {
			numberInBrackets = Pattern.compile("\\((\\d+)\\)");
		}
		if (channels.size() == 0) {
			System.out.println("Failed to prepare animation keyframes. No channels");
			return;
		}
		// Get a list of all timestamps in all of the given channel sections
		// that make this
		ArrayList<Float> timeStamps = new ArrayList<Float>();
		for (DAEAnimChannel channel : channels) {
			for (Float time : channel.sampler.input.floatList) {
				if (!timeStamps.contains(time)) {
					timeStamps.add(time);
				}
			}
		}
		// Organise timestamps to be in order again
		Collections.sort(timeStamps, new Comparator<Float>() {

			public int compare(Float f1, Float f2) {
				if (f1 < f2) {
					return -1;
				} else if (f1 == f2) {
					return 0;
				} else {
					return 1;
				}
			}
		});
		// Iterate through timestamps
		for (Float time : timeStamps) {
			DAEAnimKeyframeData keyframe = new DAEAnimKeyframeData();
			keyframe.time = time;
			for (DAEAnimChannel channel : channels) {
				if (channel.target.toLowerCase().startsWith("transform(")) {

					Float value = channel.sampler.interpTime(time);
					int first = -1, second = -1;
					Matcher m = numberInBrackets.matcher(channel.target);
					if (m.find()) {
						first = Integer.parseInt(m.group(1));
					}
					if (m.find()) {
						second = Integer.parseInt(m.group(1));
					}
					if (first == -1 || second == -1) {
						System.out.println("Failed getting Matrix indexes for " + channel.target);
					}
					keyframe.transform = keyframe.transform.put(first, second, value);
				} else if (channel.target.toLowerCase().endsWith("transform")) {
					keyframe.transform = channel.sampler.interpTimeMatrix(time);
				}
				// TODO Deal with exporters who output in other targets.
			}

			// Avoid a common bug with scale
			if (keyframe.transform != null) {
				keyframe.transform.put(3, 3, 1f);
			}
			data.add(keyframe);
		}
		Matrix4f first = data.get(0).transform;
		boolean allEqual = true;
		for (DAEAnimKeyframeData keyframe : data) {
			if (first.equals(keyframe.transform)) {
				allEqual = false;
				break;
			}
		}
		if (allEqual) {
			ignore = true;
		}

		// Delete Loaded data from Files.
		channels = null;
	}

	public float lastKeyframeTime() {
		return data.get(data.size() - 1).time;
	}

	public Matrix4f get(float poseTime) {
		int lastIndex = data.size() - 1;
		int startIndex = 0, endIndex = lastIndex;
		float startTime = 0f, endTime = data.get(lastIndex).time;
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).time < poseTime) {
				startIndex = i;
				startTime = data.get(i).time;
			} else if (data.get(i).time == poseTime) {
				return data.get(i).transform;
			} else {
				endIndex = i;
				endTime = data.get(i).time;
				break;
			}
		}
		float diffTime = endTime - startTime;

		float interp = (poseTime - startTime) / diffTime;
		Matrix4f m1 = data.get(startIndex).transform;
		Matrix4f m2 = data.get(endIndex).transform;
		if (diffTime <= 0f) {
			return m1;
		}
		Matrix4f result = m1.interp(m2, interp, null);

		return result;
	}

}
