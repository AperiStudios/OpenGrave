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
package com.opengrave.og.gui;

import net.java.games.input.Controller;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.input.JoystickRawAxisEvent;
import com.opengrave.og.resources.Font;

public class AxisInput extends UIParent implements TextInterface, EventListener {
	float axis1, axis2;
	TextArea ta;

	public AxisInput(ElementData ed) {
		super(ed);
		ta = new TextArea(new ElementData(ed));
		addChildEnd(ta);
	}

	boolean isListening = false;
	// private Controller cont;
	private int index, index2 = -1;

	public void setControlInfo(Controller cont, int index) {
		if (!isListening) {
			EventDispatcher.addHandler(this);
			isListening = true;
		}
		this.index = index;
	}

	public void setControlInfo2(Controller cont, int index) {
		if (!isListening) {
			EventDispatcher.addHandler(this);
			isListening = true;
		}
		this.index2 = index;
	}

	@Override
	public void repopulateQuads() {
		int val = 16 + (int) (axis1 * 16);
		addQuad(new UIQuad().setPos(0, val, 31, val + 1).setColour(1f, 0f, 0f, 1f));
		if (index2 >= 0) {
			val = 16 + (int) (axis2 * 16);
			addQuad(new UIQuad().setPos(val, 0, val + 1, 31).setColour(0f, 0f, 1f, 1f));
		}
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return false;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = 64;
		this.height = 64;
	}

	@Override
	public String getString() {
		return ta.getString();
	}

	@Override
	public void setString(String s) {
		ta.setString(s);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onJoyStickWiggle(JoystickRawAxisEvent event) {
		// if (event.getPad().equals(cont)) {
		if (event.getAxisIndex() == index) {
			axis1 = event.getValue();
			setChanged();
		} else if (event.getAxisIndex() == index2) {
			axis2 = event.getValue();
			setChanged();
		}
		// }
	}

	@Override
	public void setFont(Font f) {
		ta.setFont(f);
	}

}
