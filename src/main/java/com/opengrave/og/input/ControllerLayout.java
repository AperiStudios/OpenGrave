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
package com.opengrave.og.input;

import java.io.*;
import java.util.ArrayList;

import com.opengrave.og.MainThread;
import com.opengrave.og.util.Vector4f;

/**
 * This is stored information about the layout of a given gamepad, by ID
 * 
 * Axis pairs should not be deadzoned separately but as a length of x&y
 * 
 * @author triggerhapp
 * 
 */
public class ControllerLayout {
	private ArrayList<ControllerAxisPairs> pairs = new ArrayList<ControllerAxisPairs>();
	private ArrayList<ControllerAxisTrigger> triggers = new ArrayList<ControllerAxisTrigger>();
	private ArrayList<ControllerButton> buttons = new ArrayList<ControllerButton>();
	private String padName;

	public ControllerLayout(String padName) {
		this.padName = padName;
	}

	public String getName() {
		return padName;
	}

	public void addPair(int axis1, int axis2, String label, Vector4f col1, Vector4f col2) {
		synchronized (pairs) {
			pairs.add(new ControllerAxisPairs(axis1, axis2, label, col1, col2));
		}
	}

	public void addSingle(int axis, String label, Vector4f col1, Vector4f col2) {
		synchronized (triggers) {
			triggers.add(new ControllerAxisTrigger(axis, label, col1, col2));
		}
	}

	public void addButton(int buttonId, String label, int icon1, int icon2, Vector4f col1, Vector4f col2) {
		synchronized (buttons) {
			buttons.add(new ControllerButton(buttonId, label, icon1, icon2, col1, col2));
		}
	}

	public boolean hasPair(int axis1, int axis2) {
		synchronized (pairs) {
			for (ControllerAxisPairs pair : pairs) {
				if (pair.hasAxis(axis1) || pair.hasAxis(axis2)) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<ControllerAxisPairs> getPairs() {
		ArrayList<ControllerAxisPairs> list = new ArrayList<ControllerAxisPairs>();
		synchronized (pairs) {
			for (ControllerAxisPairs pair : pairs) {
				list.add(pair);
			}
		}
		return list;
	}

	public boolean hasPair(int axis) {
		synchronized (pairs) {
			for (ControllerAxisPairs pair : pairs) {
				if (pair.hasAxis(axis)) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<ControllerAxisTrigger> getTriggers() {
		ArrayList<ControllerAxisTrigger> list = new ArrayList<ControllerAxisTrigger>();
		synchronized (triggers) {
			for (ControllerAxisTrigger cat : triggers) {
				list.add(cat);
			}
		}
		return list;
	}

	public ArrayList<ControllerButton> getButtons() {
		ArrayList<ControllerButton> list = new ArrayList<ControllerButton>();
		synchronized (buttons) {
			for (ControllerButton cb : buttons) {
				list.add(cb);
			}
		}
		return list;
	}

	public ControllerAxisTrigger getTrigger(int index) {
		synchronized (triggers) {
			for (ControllerAxisTrigger cb : triggers) {
				if (cb.getAxis() == index) {
					return cb;
				}
			}
		}
		return null;
	}

	public ControllerButton getButton(int index) {
		synchronized (buttons) {
			for (ControllerButton cb : buttons) {
				if (cb.getIndex() == index) {
					return cb;
				}
			}
		}
		return null;
	}

	static String sanitiseFileString(String string) {
		if (string.toLowerCase().endsWith(".pad")) {
			string = string.substring(0, string.length() - 4);
		}
		return string.replaceAll("[^a-zA-Z0-9]", "");
	}

	public static ControllerLayout load(String string) {
		string = sanitiseFileString(string);
		ControllerLayout cl = null;
		File f = new File(MainThread.cache, "input/" + string + ".pad");
		String contName;
		try (BufferedReader in = new BufferedReader(new FileReader(f))) {
			contName = in.readLine();
			if (contName == null) {
				System.err.println("No controller name in '" + string + "' pad description file");
				return null;
			}
			cl = new ControllerLayout(contName);
			int countPairs = readInt(in);
			for (int i = 0; i < countPairs; i++) {
				int xId = readInt(in);
				int yId = readInt(in);
				String s = in.readLine();
				Vector4f col1 = fromHex(in.readLine());
				Vector4f col2 = fromHex(in.readLine());
				cl.addPair(xId, yId, s, col1, col2);
			}
			int countTriggers = readInt(in);
			for (int i = 0; i < countTriggers; i++) {
				int axis = readInt(in);
				String s = in.readLine();
				Vector4f col1 = fromHex(in.readLine());
				Vector4f col2 = fromHex(in.readLine());
				cl.addSingle(axis, s, col1, col2);
			}
			int countButtons = readInt(in);
			for (int i = 0; i < countButtons; i++) {
				int index = readInt(in);
				int icon1 = readInt(in);
				int icon2 = readInt(in);
				String label = in.readLine();
				Vector4f col1 = fromHex(in.readLine());
				Vector4f col2 = fromHex(in.readLine());
				cl.addButton(index, label, icon1, icon2, col1, col2);
			}
		} catch (FileNotFoundException e) {
			System.err.println("No pad description file for '" + string + "'");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.err.println("Expected integer number while reading '" + string + "' pad description file");
		}
		return cl;
	}

	private static int readInt(BufferedReader in) throws NumberFormatException, IOException {
		return Integer.parseInt(in.readLine());
	}

	public void save(String string) {
		string = sanitiseFileString(string);
		File f = new File(MainThread.cache, "input/" + string + ".pad");
		try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
			out.write(string);
			out.newLine();
			synchronized (pairs) {
				out.write("" + pairs.size());
				out.newLine();
				for (ControllerAxisPairs pair : pairs) {
					out.write("" + pair.xId);
					out.newLine();
					out.write("" + pair.yId);
					out.newLine();
					out.write("" + pair.getLabel());
					out.newLine();
					out.write("" + asHex(pair.getColour1()));
					out.newLine();
					out.write("" + asHex(pair.getColour2()));
					out.newLine();
				}
			}
			synchronized (triggers) {
				out.write("" + triggers.size());
				out.newLine();
				for (ControllerAxisTrigger trigger : triggers) {
					out.write("" + trigger.getAxis());
					out.newLine();
					out.write("" + trigger.getLabel());
					out.newLine();
					out.write("" + asHex(trigger.getColour1()));
					out.newLine();
					out.write("" + asHex(trigger.getColour2()));
					out.newLine();
				}
			}
			synchronized (buttons) {
				out.write("" + buttons.size());
				out.newLine();
				for (ControllerButton button : buttons) {
					out.write("" + button.getIndex());
					out.newLine();
					out.write("" + button.getIcon1());
					out.newLine();
					out.write("" + button.getIcon2());
					out.newLine();
					out.write("" + button.getLabel());
					out.newLine();
					out.write("" + asHex(button.getColour1()));
					out.newLine();
					out.write("" + asHex(button.getColour2()));
					out.newLine();
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("No cache directory 'pads'");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static Vector4f fromHex(String s) throws NumberFormatException {
		Vector4f vec = new Vector4f();
		if (s.length() != 6) {
			throw new NumberFormatException();
		}
		vec.x = Integer.parseInt(s.substring(0, 2), 16) / 255f;
		vec.y = Integer.parseInt(s.substring(2, 4), 16) / 255f;
		vec.z = Integer.parseInt(s.substring(4, 6), 16) / 255f;
		vec.w = 1f;
		return vec;
	}

	private static String asHex(Vector4f colour) {
		return String.format("%02X%02X%02X", (int) (colour.x * 255), (int) (colour.y * 255), (int) (colour.z * 255));
	}

	public ControllerAxisPairs getPair(int axis) {
		synchronized (pairs) {
			for (ControllerAxisPairs pair : pairs) {
				if (pair.hasAxis(axis)) {
					return pair;
				}
			}
		}
		return null;
	}

	public ControlDescription getComponent(int index) {
		ControllerButton cb = getButton(index);
		if (cb != null) {
			return cb;
		}
		ControllerAxisTrigger ct = getTrigger(index);
		if (ct != null) {
			return ct;
		}
		ControllerAxisPairs cp = getPair(index); // 4chan party bus not included
		if (cp != null) {
			return cp;
		}
		return null;
	}

}
