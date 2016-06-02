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
package com.opengrave.og.resources;

import java.util.HashMap;

/**
 * Tokenized strings can be a string of anything characters where specific
 * single-length characters are to be seperated out as extra information
 * 
 * @author triggerhapp
 * 
 */
public class TokenizedString {

	HashMap<String, String> tokenData = new HashMap<String, String>();

	public TokenizedString(String string, String tokens) {
		String lastToken = "";
		String data = "";
		for (int pos = 0; pos < string.length(); pos++) {
			String s = string.substring(pos, pos + 1);
			if (tokens.contains(s)) {
				// It's a new token
				tokenData.put(lastToken, data);
				data = "";
				lastToken = s;
			} else {
				data = data + s;
			}
		}
		tokenData.put(lastToken, data);
	}

	public String getTokenData(String s) {
		if (!tokenData.containsKey(s)) {
			return null;
		}
		return tokenData.get(s);
	}
}
