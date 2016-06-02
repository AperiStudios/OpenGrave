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
package com.opengrave.common.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.og.resources.Resources;

public class Config {

	HashMap<String, Object> data = new HashMap<String, Object>();
	String fileName;

	public Config(String name) {
		fileName = name;
		synchronized (data) {
			String contents = Resources.loadTextFile(name);
			if (contents.equals("")) {
				setDefaults();
			}
			for (String line : contents.split("\n")) {
				if (line.contains(":")) {
					String[] split = line.split(":", 2);
					String key = split[0];
					if (data.containsKey(key)) {
						System.out.println("Repeated key in config : " + key);
					}
					String value = split[1];
					if (value.matches("^-?\\d+$")) {
						data.put(key, Integer.parseInt(value));
					} else if (value.equalsIgnoreCase("true")) {
						data.put(key, true);
					} else if (value.equalsIgnoreCase("false")) {
						data.put(key, false);
					} else if (value.matches("^\".*\"$")) {
						data.put(key, value.substring(1, value.length() - 1));
					} else {
						System.out.println("Unable to parse value for pair : " + key + " : " + value);
					}
				}
			}
		}
	}

	public Config(BufferedReader reader) {
		synchronized (data) {
			String inputLine = "";
			try {
				while ((inputLine = reader.readLine()) != null) {
					if (inputLine.contains(":")) {
						String[] split = inputLine.split(":", 2);
						String key = split[0];
						if (data.containsKey(key)) {
							System.out.println("Repeated key in config : " + key);
						}
						String value = split[1];
						if (value.matches("^-?\\d+$")) {
							data.put(key, Integer.parseInt(value));
						} else if (value.equalsIgnoreCase("true")) {
							data.put(key, true);
						} else if (value.equalsIgnoreCase("false")) {
							data.put(key, false);
						} else if (value.matches("^\".*\"$")) {
							data.put(key, value.substring(1, value.length() - 1));
						} else {
							System.out.println("Unable to parse value for pair : " + key + " : " + value);
						}
					}
				}
			} catch (NumberFormatException e) {
				new DebugExceptionHandler(e);
			} catch (IOException e) {
				new DebugExceptionHandler(e);
			}
		}
	}

	public void setDefaults() {
		setInteger("max_fps", 30);
		setBoolean("show_debug_main_menu", false);
		setBoolean("server_allow_anon", false);
		setBoolean("server_allow_friend", true);
		save();
	}

	public void setInteger(String key, Integer i) {
		synchronized (data) {
			data.put(key, i);
		}
	}

	public void setBoolean(String key, Boolean b) {
		synchronized (data) {
			data.put(key, b);
		}
	}

	public void setString(String key, String value) {
		synchronized (data) {
			data.put(key, value);
		}
	}

	public void save() {
		if (fileName == null) {
			return;
		}
		synchronized (data) {

			StringBuilder sb = new StringBuilder();
			for (String key : data.keySet()) {
				sb.append(key);
				sb.append(":");
				Object value = data.get(key);
				if (value instanceof String) {
					sb.append("\"");
					String valueString = (String) value;
					sb.append(valueString);
					sb.append("\"");
				} else if (value instanceof Integer) {
					sb.append((Integer) value);
				} else if (value instanceof Boolean) {
					sb.append((Boolean) value);
				}
				sb.append("\n");
			}
			Resources.writeTextFile(fileName, sb.toString());
		}
	}

	public boolean getBoolean(String string, boolean defaultTo) {
		synchronized (data) {
			Object o = data.get(string);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		}
		return defaultTo;
	}

	public String getString(String string, String defaultTo) {
		synchronized (data) {
			Object o = data.get(string);
			if (o instanceof String) {
				return (String) o;
			}
		}
		return defaultTo;
	}

	public int getInteger(String string, int defaultTo) {
		synchronized (data) {
			Object o = data.get(string);
			if (o instanceof Integer) {
				return (Integer) o;
			}
		}
		return defaultTo;
	}

}
