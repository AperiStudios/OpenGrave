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
import com.opengrave.og.engine.Location;
import com.opengrave.og.gui.callback.FloatNumberRollerChangedEvent;
import com.opengrave.og.gui.callback.VectorInput3ChangedEvent;
import com.opengrave.og.util.Vector3f;

public class VectorInput3 extends HorizontalContainer implements EventListener {

	FloatNumberRoller xr, yr, zr;

	public VectorInput3(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
		xr = new FloatNumberRoller(ed);
		yr = new FloatNumberRoller(ed);
		zr = new FloatNumberRoller(ed);
		addChildEnd(xr);
		addChildEnd(yr);
		addChildEnd(zr);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onFloatNumberRollerChanged(FloatNumberRollerChangedEvent event) {
		FloatNumberRoller nr = event.getNumberRoller();
		if (nr.equals(xr) || nr.equals(yr) || nr.equals(zr)) {
			VectorInput3ChangedEvent event2 = new VectorInput3ChangedEvent(this);
			EventDispatcher.dispatchEvent(event2);
		}
	}

	public Vector3f getVector() {
		return new Vector3f(xr.getNumber(), yr.getNumber(), zr.getNumber());
	}

	public void setVector(Vector3f vec) {
		xr.setNumber(vec.x);
		yr.setNumber(vec.y);
		zr.setNumber(vec.z);
	}

	public Location asLocation() {
		Location l = new Location();
		l.setFullX(xr.getNumber());
		l.setFullY(yr.getNumber());
		l.setZ(zr.getNumber());
		return l;
	}

}
