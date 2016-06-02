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

public class VerticalContainer extends Container {
	public VerticalContainer(ElementData ed) {
		super(ed);
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		if (this.width != width || this.height != height || this.childrenChanged) {
			int maxw = 0;
			synchronized (children) {
				this.childrenChanged = false;
				this.width = width;
				this.height = height;
				int spaceLeft = height;
				if (height > mheight) { // Don't limit height
					this.height = Integer.MAX_VALUE;
					spaceLeft = Integer.MAX_VALUE;
				}
				int childrenLeft = children.size();
				if (childrenLeft == 0) {
					return;
				}
				int optimalSize = spaceLeft;
				if (childrenLeft != 0) {
					optimalSize = spaceLeft / childrenLeft;
				}
				int starty = 0;
				for (UIElement e : children) {
					if (e.isHidden()) {
						childrenLeft--;
						continue;
					}
					if (e.getElementData().maximum_height > 0) {
						if (e.getElementData().maximum_height < optimalSize) {
							optimalSize = e.getElementData().maximum_height;
						}
					}
					e.setSize(width, optimalSize, width, optimalSize);
					e.setLocation(0, starty);
					spaceLeft = spaceLeft - e.height;
					starty = starty + e.height;

					childrenLeft--;
					if (childrenLeft == 0) {
						optimalSize = spaceLeft;
					} else {
						optimalSize = spaceLeft / childrenLeft;
					}
					if (e.width > maxw) {
						maxw = e.width;
					}
				}
				this.height = this.height - spaceLeft;
				this.width = maxw;
				setAllChanged();
			}
		}
	}
}
