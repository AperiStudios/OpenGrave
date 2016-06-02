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

import java.util.ArrayList;

public class ControlBinding implements Comparable<ControlBinding> {
	private String controlName;
	private ArrayList<InputBinding> inputs = new ArrayList<InputBinding>();

	public ControlBinding(String controlName) {
		this.controlName = controlName;
	}

	public String getControlName() {
		return controlName;
	}

	public boolean hasInput(InputBinding input) {
		synchronized (inputs) {
			return inputs.contains(input);
		}
	}

	public boolean hasInput(String key) {
		return hasInput(new KeyBinding(key));
	}

	public boolean hasInput(String padName, int index) {
		if (InputMain.cl == null) {
			return false;
		}
		if (!InputMain.cl.getName().equalsIgnoreCase(padName)) {
			return false;
		}
		return hasInput(new PadBinding(padName, index));
	}

	public ArrayList<InputBinding> getList() {
		ArrayList<InputBinding> list = new ArrayList<InputBinding>();
		synchronized (inputs) {
			list.addAll(inputs);
		}
		return list;
	}

	public void addInput(ArrayList<InputBinding> arrayList) {
		synchronized (inputs) {
			inputs.addAll(arrayList);
		}
	}

	public void addInput(InputBinding bind) {
		synchronized (inputs) {
			inputs.add(bind);
		}

	}

	public void removeInput(ArrayList<InputBinding> arrayList) {
		synchronized (inputs) {
			if (arrayList == null) {
				inputs.clear();
			} else {
				for (InputBinding ib : arrayList) {
					inputs.remove(ib);
				}
			}
		}
	}

	public PadBinding getInputController(String name) {
		synchronized (inputs) {
			for (InputBinding ib : inputs) {
				if (ib instanceof PadBinding) {
					PadBinding pb = (PadBinding) ib;
					if (pb.getPadName().equalsIgnoreCase(name)) {
						return pb;
					}
				}
			}
		}
		return null;
	}

	public KeyBinding getInputKeyboard() {
		synchronized (inputs) {
			for (InputBinding ib : inputs) {
				if (ib instanceof KeyBinding) {
					KeyBinding kb = (KeyBinding) ib;
					return kb;
				}
			}
		}
		return null;
	}

	@Override
	public int compareTo(ControlBinding arg0) {
		return getControlName().compareTo(arg0.getControlName());
	}
}
