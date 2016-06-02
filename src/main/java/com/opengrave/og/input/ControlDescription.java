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

import com.opengrave.og.util.Vector4f;

public interface ControlDescription {
	public abstract int getIcon1();

	public abstract int getIcon2();

	public abstract Vector4f getColour1();

	public abstract Vector4f getColour2();

	public abstract String getLabel();

	public abstract void putColour1(Vector4f c);

	public abstract void putColour2(Vector4f c);

	public abstract void putLabel(String string);

	public abstract void putIcon1(int textureIndex);

	public abstract void putIcon2(int textureIndex);

}
