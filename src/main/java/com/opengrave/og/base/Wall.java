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
package com.opengrave.og.base;

import java.io.IOException;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.Node;
import com.opengrave.og.engine.RenderView;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix4f;

public abstract class Wall extends Node implements Pickable {

	// Location areaLocation;
	// Location location;
	Renderable3DStatic renderable;
	Location location = new Location();
	MaterialList matList;

	private RenderStyle style;

	public abstract float getLength();

	public abstract void setLength(float length);

	public abstract String getMetaData();

	public abstract void setMetaData(String s);

	public float getAngle() {
		return location.getRotate().z;
	}

	public void setAngle(float angle) {
		location.getRotate().z = angle;
	}

	public void setX(float number) {
		location.setFullX(number);
	}

	public void setY(float number) {
		location.setFullY(number);
	}

	public void setZ(float number) {
		location.setZ(number);
	}

	public void setLocation(Location l) {
		location = new Location(l);
	}

	public float getX() {
		return location.getFullXAsFloat();
	}

	public float getY() {
		return location.getFullYAsFloat();
	}

	public float getZ() {
		return location.getZ();
	}

	public String getMaterialLabel() {
		if (matList == null) {
			return "blank";
		}
		return matList.toString();
	}

	public void setRenderStyle(RenderStyle renderStyle) {
		style = renderStyle;
	}

	public void doRender(Matrix4f parent) {
		renderable.setMaterialList(matList);
		renderable.render(parent, style);
	}

	public static Wall createWall(OGInputStream reader) {
		Wall wall = null;
		String type;
		try {
			type = reader.readString();
			String label = reader.readString();
			// TODO More wall types?
			if (type == "boring") {
				wall = new BoringWall();
			} else {
				wall = new BoringWall();
			}
			wall.location = new Location(reader.readLocation());
			wall.matList = reader.readMaterialList();
			wall.setMetaData(label);
		} catch (IOException e) {
			new DebugExceptionHandler(e);

		}

		return wall;
	}

	public void save(OGOutputStream stream) {
		try {
			stream.writeString(getType());
			stream.writeString(getMetaData());
			stream.writeLocation(location);
			stream.writeMaterialList(matList);
			// stream.writeString(getMaterialLabel());
		} catch (IOException e) {
			new DebugExceptionHandler(e);

		}

	}

	public abstract String getType();

	@Override
	public RenderView getContext() {
		return context;
	}

	@Override
	public void doUpdate(float delta) {
		renderable.setContext(context);
		renderable.update(delta);

	}

	@Override
	public void doRenderShadows(Matrix4f parent, Shadow shadow) {
		renderable.renderShadows(parent, shadow);
	}

	@Override
	public void doRenderForPicking(Matrix4f parent) {
		renderable.renderForPicking(parent, this);

	}

	@Override
	public void doRenderSemiTransparent(Matrix4f parent) {

	}

	public void setMaterialList(MaterialList mList) {
		matList = mList;
	}

	public Location getLocation() {
		return location;
	}

}
