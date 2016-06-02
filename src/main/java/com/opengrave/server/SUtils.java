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
package com.opengrave.server;

import java.util.HashMap;

import com.opengrave.common.Colour;
import com.opengrave.common.Util;

public class SUtils {

	public static HashMap<String, String> customMessages = new HashMap<String, String>();

	public static String getMessageFormat(String key, String msg1) {
		return getMessageFormat(key, msg1, "");
	}

	public static void setMessageFormat(String key, String value) {
		if (key == null || key.equals("") || value == null || value.equals("")) {
			return;
		}
		customMessages.put(key, value);
	}

	public static String getMessageFormat(String key, String msg1, String msg2) {
		key = key.toLowerCase();
		if (customMessages.containsKey(key)) {
			String message = customMessages.get(key);
			message = Util.replace(Util.replace(message, "%1", msg1), "%2", msg2);
			return message;
		}
		return "Unknown custom message : (" + key + ", " + msg1 + ", " + msg2 + ")";

	}

	public static String simplifyColours(String message) {
		String sep = "§";
		String f = "";
		Colour lastCol = Colour.none;
		boolean first = true;
		for (String s : message.split(sep)) {
			if (first) {
				f = s;
				first = false;
			} else {
				String single = s.substring(0, 7);
				String rest = s.substring(7, s.length());
				Colour col = new Colour(single);
				if (rest.length() >= 1) {
					if (col.equals(lastCol)) {
						f = f + rest;
					} else {
						f = f + sep + single + rest;
						lastCol = col;
					}
				}
			}
		}
		return f;
	}

	public static String strip(String message) {
		return message.replaceAll("§.......", "");
	}

}
