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

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.gui.callback.CheckButtonPressedEvent;
import com.opengrave.og.resources.TextureAtlas;

public class CheckButton extends TextButton implements EventListener {
	public static TextureAtlas textureStatic;

	private boolean checked = false;

	public CheckButton(ElementData ed) {
		super(ed);
		texture = CheckButton.textureStatic;
	}

	public boolean getChecked() {
		return checked;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onButtonPress(ButtonPressedEvent event) {
		if (event.getButton().equals(this)) {
			this.checked = !this.checked;
			setChanged();
			EventDispatcher.dispatchEvent(new CheckButtonPressedEvent(this));
		}
	}

	@Override
	public void repopulateQuads() {
		UIQuad q = new UIQuad().setPos(0, 0, width, height).setColour(getColour());
		addQuad(q); // Background quad
		q = new UIQuad().setPos(2, 2, height - 2, height - 2).setTexture(0f, 1f, 0f, 1f, checked ? 1 : 2).setColour(1f, 0f, 0f, 1f);
		addQuad(q); // Tickbox quad
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = width;
		this.height = height;
		children.get(0).setSize(width, height, width, height);
		children.get(0).setLocation(30, 0);
		setChanged();
	}

	/**
	 * This does NOT throw an event - used for init'ing the state at gui
	 * creation and when checkbuttongroup* change chosen selection
	 * 
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
		setChanged();

	}
}
