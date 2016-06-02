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
package com.opengrave.common;

import java.util.ArrayList;

public class Util {

	public static int compareColours(Colour c1, Colour c2) {
		int dist = Math.abs((c1.getRed() - c2.getRed()) ^ 2) + Math.abs((c1.getGreen() - c2.getGreen()) ^ 2) + Math.abs((c1.getBlue() - c2.getBlue()) ^ 2);
		return dist;
	}

	public static Colour getClosestColour(ArrayList<Colour> colourList, Colour colour) {
		int dist = Integer.MAX_VALUE;
		Colour c = null;
		for (Colour c2 : colourList) {
			int newDist = compareColours(colour, c2);
			if (newDist < dist) {
				dist = newDist;
				c = c2;
			}
		}
		return c;
	}

	public static String replace(final String aInput, final String aOldPattern, final String aNewPattern) {
		if (aOldPattern.equals("")) {
			return aInput;
		}

		final StringBuffer result = new StringBuffer();

		int startIdx = 0;
		int idxOld = 0;
		while ((idxOld = aInput.indexOf(aOldPattern, startIdx)) >= 0) {
			result.append(aInput.substring(startIdx, idxOld));
			result.append(aNewPattern);
			startIdx = idxOld + aOldPattern.length();
		}
		result.append(aInput.substring(startIdx));
		return result.toString();
	}
}
