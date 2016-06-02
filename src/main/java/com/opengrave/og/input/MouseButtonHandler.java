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

import org.lwjgl.glfw.GLFWMouseButtonCallback;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.og.MainThread;
import com.opengrave.og.base.Picking;
import com.opengrave.og.base.PickingResult;
import com.opengrave.og.gui.UIElement;

public class MouseButtonHandler extends GLFWMouseButtonCallback {
	ArrayList<Integer> heldButtons = new ArrayList<Integer>();
	ArrayList<Integer> pressedButtons = new ArrayList<Integer>();

	@Override
	public void invoke(long window, int button, int action, int mods) {
		boolean s = action != GLFW_RELEASE;
		synchronized (heldButtons) {
			if (s) {
				if (!heldButtons.contains(button)) {
					heldButtons.add(new Integer(button));
					pressedButtons.add(new Integer(button));
				}
			} else {
				if (heldButtons.contains(button)) {
					heldButtons.remove(new Integer(button));
				}
			}
		}
	}

	public void update(float delta) {

		int x = (int) InputMain.getMouse().x;
		int y = (int) InputMain.getMouse().y;
		PickingResult pr = Picking.pick(x, y);
		if (pr == null || pr.picked == null) {
			return;
		}
		synchronized (heldButtons) {
			for (Integer i : pressedButtons) {
				MouseButtonRenderableEvent event;
				if (pr.picked instanceof UIElement) {
					int rx = x - ((UIElement) pr.picked).getParentX();
					int ry = (MainThread.lastH - y) - ((UIElement) pr.picked).getParentY();
					event = new MouseButtonRenderableEvent(pr.picked, pr.worldLoc, i, rx, ry, x, MainThread.lastH - y);
				} else {
					int rx = x - pr.picked.getContext().totalx;
					int ry = (MainThread.lastH - y) - pr.picked.getContext().totaly;
					event = new MouseButtonRenderableEvent(pr.picked, pr.worldLoc, i, rx, ry, x, MainThread.lastH - y);
				}
			}
			if (pr.worldLoc != null) {
				for (int i : heldButtons) {
					EventDispatcher.dispatchEvent(new MouseRenderableDragEvent(pr.picked, pr.worldLoc, i));
				}
				EventDispatcher.dispatchEvent(new MouseRenderableHoverEvent(pr.picked, pr.worldLoc));

			}
		}
		pressedButtons.clear();
	}

}
