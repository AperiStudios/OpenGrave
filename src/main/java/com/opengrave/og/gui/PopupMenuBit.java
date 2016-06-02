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

import com.opengrave.common.PopupMenuOption;
import com.opengrave.og.input.InputMain;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.resources.TextureAtlas;

public class PopupMenuBit extends UIParent {
	public static TextureAtlas textureStatic;

	public static final int sizex = 100, sizey = 160;

	private PopupMenuOption pmo;

	private BaseText text;
	private Image image;
	private ImageInput input;
	private boolean changedImage = false;

	private String inputString;

	public PopupMenuBit(ElementData ed) {
		super(ed);
		texture = textureStatic;
		text = new TextArea(new ElementData(ed));
		image = new Image(new ElementData(ed));
		input = new ImageInput(new ElementData(ed));
		this.addChildEnd(image);
		this.addChildEnd(text);
		this.addChildEnd(input);
	}

	public void setImage(String string) {
		pmo.setIcon(string);
		changedImage = true;
	}

	public void setMenuOptions(PopupMenuOption pmo, String type) {
		text.setString(pmo.getLabel());
		changedImage = true;
		input.set(InputMain.getControlIcon(pmo.getControl()));
		this.inputString = type;
		this.pmo = pmo;
	}

	public String getInputString() {
		return inputString;
	}

	public PopupMenuOption getMenuOptions() {
		return pmo;
	}

	@Override
	public void repopulateQuads() {
		if (pmo != null && changedImage) {
			image.setTexture(Resources.loadTextures(pmo.getIcon()));
			changedImage = false;
		}

		UIQuad q = new UIQuad();
		q.setPos(0, 0, width, height).setColour(ed.defaultColour).setTexture(0, 1, 0, 1, 1);
		addQuad(q);
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return true;
	}

	@Override
	public boolean isFocusable() {
		return false;
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		this.width = sizex;
		this.height = sizey;
		image.setLocation(10, 10);
		image.setSize(80, 80, 80, 80);
		text.setSize(width, 60, width, 60); // We can rely on TA to resize up to fit
		input.setSize(0, 0, 0, 0); // Also sets its own size
		if (this.height < text.height) {
			text.y = 100;
		} else {
			int diff = 60 - text.height;
			text.y = 100 + (diff / 2);
		}
		if (this.width < text.width) {
			text.x = 0;
		} else {
			int diff = 100 - text.width;
			text.x = diff / 2;
		}
		if (this.height < input.height) {
			input.y = 130;
		} else {
			int diff = 60 - input.height;
			input.y = 130 + (diff / 2);
		}
		if (this.width < input.width) {
			input.x = 0;
		} else {
			int diff = 100 - input.width;
			input.x = diff / 2;
		}

	}

}
