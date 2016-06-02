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
import com.opengrave.og.base.Pickable;
import com.opengrave.og.gui.callback.ButtonPressedEvent;
import com.opengrave.og.gui.callback.UIElementFocusEvent;
import com.opengrave.og.input.MouseButtonRenderableEvent;

public abstract class Button extends UIParent implements EventListener {

	public Button(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onPress(MouseButtonRenderableEvent event) {
		if (disabled) {
			return;
		}
		if (isThis(event.getObject()) && event.getButton() == 0) {
			event.setConsumed();
			EventDispatcher.dispatchEvent(new ButtonPressedEvent(this));
		}
	}

	public boolean isThis(Pickable object) {
		return object == this;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onFocus(UIElementFocusEvent event) {
		if (event.getElement() == this) {
			if (!focus) {
				focus = true;
				setChanged();
			}
			setChanged();
		} else {
			if (focus) {
				focus = false;
				setChanged();
			}
		}
	}

	@Override
	public void update(float delta) {

	}

	@Override
	public void repopulateQuads() {
		UIQuad q = new UIQuad().setPos(0, 0, width, height).setColour(getColour().x, getColour().y, getColour().z, getColour().w)
				.setTexture(0f, 1f, 0f, 1f, textureIndex);
		addQuad(q);
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return true;
	}

}
