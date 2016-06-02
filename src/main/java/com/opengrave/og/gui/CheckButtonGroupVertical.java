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

public class CheckButtonGroupVertical extends VerticalContainer implements EventListener {

	public CheckButtonGroupVertical(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void checkButtonPressed(ButtonPressedEvent event) {
		synchronized (children) {
			if (children.contains(event.getButton())) {
				for (UIElement element : children) {
					if (element instanceof CheckButton) {
						CheckButton cb = (CheckButton) element;
						cb.setChecked(false);
					}
				}
			}
		}
	}

}
