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
package com.opengrave.og.gui.callback;

import com.opengrave.common.event.ConsumableEvent;
import com.opengrave.og.MainThread;
import com.opengrave.og.gui.PopupMenu;
import com.opengrave.og.gui.PopupMenuBit;

public class PopupOptionChosen extends ConsumableEvent {

	private PopupMenu pm;
	private String id;
	private Object reference;
	private PopupMenuBit bit;

	public PopupOptionChosen(PopupMenu pm, String id, Object reference, PopupMenuBit bit) {
		this.pm = pm;
		this.id = id;
		this.reference = reference;
		this.bit = bit;
	}

	public String getId() {
		return id;
	}

	public Object getReference() {
		return reference;
	}

	@Override
	public String getEventName() {
		return "popupoptionchosen";
	}

	public PopupMenuBit getPopupMenuChoice() {
		return bit;
	}

	public void closeMenu() {
		MainThread.getGameState().screen.closePopup();
	}

	public String getMenuName() {
		return pm.getPageContext();
	}

}
