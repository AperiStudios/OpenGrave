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

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.GL15;

import com.opengrave.og.Util;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.base.Renderable2D;
import com.opengrave.og.base.Vertex2D;
import com.opengrave.og.engine.RenderView;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public abstract class UIElement extends Renderable2D implements Pickable {
	UIParent parent;
	ElementData ed;
	protected int x, y, width = -1, height = -1;
	private FloatBuffer dataBuffer;
	private ArrayList<UIQuad> quads = new ArrayList<UIQuad>();
	private int bufferID = 0;
	protected boolean focus = false, disabled = false, reshuffle = false;

	public UIElement(ElementData ed) {
		this.ed = ed;
	}

	public Vector4f getColour() {
		if (focus) {
			return ed.activeColour;
		} else if (disabled) {
			return ed.disabledColour;
		}
		return ed.defaultColour;
	}

	public Vector4f getTextColour() {
		return ed.textColour;
	}

	public abstract void repopulateQuads();

	protected abstract boolean shouldRenderForPicking();

	/**
	 * Describes whether this element will accept focus (and therefore input) a
	 * non-focusable element will never get input
	 * 
	 * @return
	 */
	public abstract boolean isFocusable();

	public abstract void update(float delta);

	/**
	 * Ask the widget to re-adjust itself to this size. This is a request, and
	 * can be ignored. Rendering may go outside the bounds of the widgets
	 * accepted bounds, but will look ugly
	 * 
	 * @param width
	 * @param height
	 */
	public abstract void setSize(int width, int height, int maxwidth, int maxheight);

	public void render(int totalx, int totaly) {
		if (ed.hidden) {
			return;
		}
		location2d = new Vector3f(totalx, totaly, 0);
		Util.checkErr();
		if (texture == null || !texture.isValid()) {
			// First, check if ElementData wants a different texture
			if (getElementData().attributes.containsKey("texture")) {
				texture = Resources.loadTexture(getElementData().attributes.get("texture"));
			} else {
				texture = Resources.loadTexture("blank");
			}
		}
		render(null, RenderStyle.NORMAL);
		Util.checkErr();
	}

	public void renderForPicking(int totalx, int totaly) {
		if (ed.hidden) {
			return;
		}
		if (shouldRenderForPicking()) {
			location2d = new Vector3f(totalx, totaly, 0);
			renderForPicking(null, (Pickable) this);
		}
	}

	public void doBuffer() {
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, getBufferID());
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataBuffer, GL15.GL_STATIC_DRAW);
	}

	public int getBufferID() {
		if (bufferID == 0) {
			bufferID = GL15.glGenBuffers();
		}
		return bufferID;
	}

	public void addQuad(UIQuad q) {
		synchronized (quads) {
			quads.add(q);
		}
	}

	@Override
	public void recreate() {
		synchronized (quads) {
			quads.clear();
			repopulateQuads();
			for (UIQuad q : quads) {
				addVertex(new Vertex2D(q.x1, q.y1, q.tx1, q.ty1, q.tz, q.r1, q.g1, q.b1, q.a1));
				addVertex(new Vertex2D(q.x1, q.y2, q.tx1, q.ty2, q.tz, q.r2, q.g2, q.b2, q.a2));
				addVertex(new Vertex2D(q.x2, q.y2, q.tx2, q.ty2, q.tz, q.r3, q.g3, q.b3, q.a3));

				addVertex(new Vertex2D(q.x2, q.y1, q.tx2, q.ty1, q.tz, q.r4, q.g4, q.b4, q.a4));
				addVertex(new Vertex2D(q.x1, q.y1, q.tx1, q.ty1, q.tz, q.r1, q.g1, q.b1, q.a1));
				addVertex(new Vertex2D(q.x2, q.y2, q.tx2, q.ty2, q.tz, q.r3, q.g3, q.b3, q.a3));
			}
		}
	}

	public ArrayList<UIQuad> getQuads() {
		ArrayList<UIQuad> newList = new ArrayList<UIQuad>();
		synchronized (quads) {
			for (UIQuad q : quads) {
				newList.add(q);
			}
		}
		return newList;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void delete() {
		if (bufferID > 0) {
			GL15.glDeleteBuffers(getBufferID());
			bufferID = 0;
		}
	}

	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public UIElement getElementById(String name) {
		if (ed.id.equalsIgnoreCase(name)) {
			return this;
		}
		return null;
	}

	public ElementData getElementData() {
		return ed;
	}

	public void setColourScheme(ElementData ed) {
		this.ed.defaultColour = ed.defaultColour;
		this.ed.activeColour = ed.activeColour;
		this.ed.disabledColour = ed.disabledColour;
	}

	public boolean isHidden() {
		return ed.hidden;
	}

	public void hide(boolean hidden) {
		ed.hidden = hidden;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		setChanged();
	}

	public RenderView getContext() {
		return null;
	}

	public <T extends UIElement> ArrayList<T> getElementsByType(Class<T> type) {
		return null;
	}

	public String getID() {
		return ed.id;
	}

	public void setParent(UIParent uiParent) {
		parent = uiParent;
	}

	@Override
	public void setChanged() {
		changed = true;
		if (parent != null) {
			parent.setChanged();
		}
	}

	public int getParentX() {
		return x + parent.getX();
	}

	public int getParentY() {
		return y + parent.getY();
	}

	public void attributesChanged() {

	}
}
