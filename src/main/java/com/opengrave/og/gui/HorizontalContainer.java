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

public class HorizontalContainer extends Container {

	public HorizontalContainer(ElementData ed) {
		super(ed);
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		if (this.width != width || this.height != height || this.childrenChanged) {
			int maxh = 0;
			synchronized (children) {
				this.childrenChanged = false;
				this.width = width;
				this.height = height;
				int spaceLeft = width;
				int childrenLeft = children.size();
				if (childrenLeft == 0) {
					return;
				}
				int optimalSize = spaceLeft / childrenLeft;
				int startx = 0;
				for (UIElement e : children) {
					if (e.isHidden()) {
						childrenLeft--;
						continue;
					}
					e.setSize(optimalSize, height, optimalSize, height);
					e.setLocation(startx, 0);
					spaceLeft = spaceLeft - e.width;
					startx = startx + e.width;

					childrenLeft--;
					if (childrenLeft == 0) {
						optimalSize = spaceLeft;
					} else {
						optimalSize = spaceLeft / childrenLeft;
					}
					if (e.height > maxh) {
						maxh = e.height;
					}
				}
				this.width = this.width - spaceLeft;
				this.height = maxh;
				setAllChanged();
			}
		}
	}

}
