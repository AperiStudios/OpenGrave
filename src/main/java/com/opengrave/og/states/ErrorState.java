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
package com.opengrave.og.states;

import com.opengrave.og.gui.ElementData;
import com.opengrave.og.gui.TextArea;
import com.opengrave.og.gui.VerticalContainer;

public class ErrorState extends BaseState {

	String error = "";
	private TextArea text;

	public ErrorState(String error) {
		this.error = error;

	}

	@Override
	public void start() {
		screen = new VerticalContainer(new ElementData());
		text = new TextArea(new ElementData());
		text.setString(error);
	}

	@Override
	public void stop() {
		screen.delete();
		screen = null;

	}

	@Override
	public void update(float delta) {

	}

}
