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

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFWKeyCallback;

import com.opengrave.common.event.EventDispatcher;

public class KeyboardRawHandler extends GLFWKeyCallback {
	private ArrayList<Integer> heldKeys = new ArrayList<Integer>();

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		boolean s = action != GLFW_RELEASE;
		synchronized (heldKeys) {
			if (s) {
				if (!heldKeys.contains(key)) {
					heldKeys.add(new Integer(key));
				}
			} else {
				if (heldKeys.contains(key)) {
					heldKeys.remove(new Integer(key));
				}
			}
		}
		char c = (char) key;
		String str = "" + c;
		System.out.println(s + " " + scancode + " " + key + " " + str);

		KeyboardRawPressEvent event = new KeyboardRawPressEvent(InputMain.lastFocused, str, s, scancode, (char) key);
		EventDispatcher.dispatchEvent(event);

	}

	public void update(float delta) {
		synchronized (heldKeys) {
			for (Integer k : heldKeys) {
				char c = (char) ((int) k);
				String str = "" + c;
				EventDispatcher.dispatchEvent(new KeyboardRawHeldEvent(str, k, delta, 1f));
			}
		}
	}
}
