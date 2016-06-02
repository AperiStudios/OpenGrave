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
import com.opengrave.og.util.Matrix4f;

public class DAEAnimSample {

	String id = null;
	DAEAnimSource input, output, interp;

	public DAEAnimSample(Element sampler, ArrayList<DAEAnimSource> sources) {
		id = sampler.getAttribute("id");
		for (Element inputNode : XML.getChildren(sampler, "input")) {
			String sem = inputNode.getAttribute("semantic");
			String sourceName = inputNode.getAttribute("source").substring(1);
			DAEAnimSource source = getSource(sources, sourceName);
			if (source != null) {
				if (sem.equalsIgnoreCase("INPUT")) {
					input = source;
				} else if (sem.equalsIgnoreCase("OUTPUT")) {
					output = source;
					output.createMatrixes();
				} else if (sem.equalsIgnoreCase("INTERPOLATION")) {
					interp = source;
				}
			}
		}
	}

	public DAEAnimSource getSource(ArrayList<DAEAnimSource> sources, String sourceName) {
		for (DAEAnimSource source : sources) {
			if (source.id.equalsIgnoreCase(sourceName)) {
				return source;
			}
		}
		return null;
	}

	public Float interpTime(Float time) {
		int indexBefore = -1, indexAfter = -1;
		for (int index = 0; index < input.floatList.size(); index++) {
			float timeNow = input.floatList.get(index);
			if (timeNow == time) {
				return output.floatList.get(index);
			} else if (timeNow < time) {
				indexBefore = index;
			} else {
				indexAfter = index;
				float valueBefore = output.floatList.get(indexBefore);
				float valueAfter = output.floatList.get(indexAfter);
				if (valueBefore == valueAfter) {
					return valueBefore;
				}
				float timeBefore = input.floatList.get(indexBefore);
				float timeAfter = input.floatList.get(indexAfter);
				float timeDiff = timeAfter - timeBefore;

				float timeNowDiff = time - timeBefore;
				float interp = timeNowDiff / timeDiff;
				return (valueBefore * (1f - interp)) + (valueAfter * interp);
			}
		}
		// Big error here
		return 0f;
	}

	public Matrix4f interpTimeMatrix(Float time) {
		int indexBefore = -1, indexAfter = -1;
		for (int index = 0; index < input.floatList.size(); index++) {
			float timeNow = input.floatList.get(index);
			if (timeNow == time) {
				return output.matrixList.get(index);
			} else if (timeNow < time) {
				indexBefore = index;
			} else {
				indexAfter = index;
				Matrix4f valueBefore = output.matrixList.get(indexBefore);
				Matrix4f valueAfter = output.matrixList.get(indexAfter);
				if (valueBefore == valueAfter) {
					return valueBefore;
				}
				float timeBefore = input.floatList.get(indexBefore);
				float timeAfter = input.floatList.get(indexAfter);
				float timeDiff = timeAfter - timeBefore;

				float timeNowDiff = time - timeBefore;
				float interp = timeNowDiff / timeDiff;
				// TODO I'm sure somethings wrong here :)
				System.out.println("Between " + valueBefore + " and " + valueAfter + " with interp " + interp);
				System.out.println(output.floatList.get(index));
				Matrix4f newM = valueBefore.interp(valueAfter, interp, null);

				return newM;
			}
		}
		return null;
	}

}
