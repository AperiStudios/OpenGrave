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

import java.util.Comparator;

public class PointLightSorter implements Comparator<PointLightNode> {

	@Override
	public int compare(PointLightNode arg0, PointLightNode arg1) {
		double depth0 = arg0.getDepth(), depth1 = arg1.getDepth();
		if (depth0 < depth1) {
			return -1;
		} else if (depth0 == depth1) {
			return 0;
		} else {
			return 1;
		}
	}

}
