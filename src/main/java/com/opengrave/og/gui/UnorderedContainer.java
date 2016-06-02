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

/***
 * A container that has no specified layout. The assumption is that each child
 * will set their own location and size
 * 
 * @author triggerhapp
 * 
 */
public class UnorderedContainer extends Container {

	public UnorderedContainer(ElementData ed) {
		super(ed);
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		// if (this.width == width && this.height == height &&
		// !this.childrenChanged) { return; }
		this.width = width;
		this.height = height;
		synchronized (children) {
			for (UIElement child : children) {
				ElementData ed = child.getElementData();
				int cw = ed.maximum_width, ch = ed.maximum_height;
				if (cw > width) {
					cw = width;
				}
				if (ch > height) {
					ch = height;
				}
				if (cw == -1) {
					cw = width;
				}
				if (ch == -1) {
					ch = height;
				}
				child.setSize(cw, ch, cw, ch);
				if (ed.positionTypeX == ElementData.PositionTypeX.FIXED) {
					child.setLocation(ed.x, child.getY());
				} else if (ed.positionTypeX == ElementData.PositionTypeX.LEFT) {
					child.setLocation(0, child.getY());
				} else if (ed.positionTypeX == ElementData.PositionTypeX.CENTER) {
					child.setLocation((width / 2) - (cw / 2), child.getY());
				} else if (ed.positionTypeX == ElementData.PositionTypeX.RIGHT) {
					child.setLocation(width - cw, child.getY());
				} else {
					child.setLocation(0, child.getY());
				}
				if (ed.positionTypeY == ElementData.PositionTypeY.FIXED) {
					child.setLocation(child.getX(), ed.y);
				} else if (ed.positionTypeY == ElementData.PositionTypeY.TOP) {
					child.setLocation(child.getX(), 0);
				} else if (ed.positionTypeY == ElementData.PositionTypeY.CENTER) {
					child.setLocation(child.getX(), (height / 2) - (ch / 2));
				} else if (ed.positionTypeY == ElementData.PositionTypeY.BOTTOM) {
					child.setLocation(child.getX(), height - ch);
				} else {
					child.setLocation(child.getX(), 0);
				}

			}
			setAllChanged();
		}
	}
}
