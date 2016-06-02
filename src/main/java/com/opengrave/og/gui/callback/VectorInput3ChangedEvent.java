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

import com.opengrave.common.event.Event;
import com.opengrave.og.gui.VectorInput3;

public class VectorInput3ChangedEvent extends Event {

	private VectorInput3 vector;

	public VectorInput3ChangedEvent(VectorInput3 vectorInput3) {
		this.vector = vectorInput3;
	}

	public VectorInput3 getVectorInput() {
		return vector;
	}

	@Override
	public String getEventName() {
		return "vectorinput3changedevent";
	}

}
