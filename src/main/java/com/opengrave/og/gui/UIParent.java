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

import java.util.ArrayList;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.Util;
import com.opengrave.og.util.Vector3f;

public abstract class UIParent extends UIElement {
	boolean childrenChanged;

	public UIParent(ElementData ed) {
		super(ed);
	}

	protected ArrayList<UIElement> children = new ArrayList<UIElement>();

	@Override
	public void delete() {
		synchronized (children) {
			for (UIElement e : children) {
				e.delete();
			}
		}
		super.delete();
	}

	public void closePopup() {
		synchronized (children) {
			for (UIElement child : children) {
				if (child instanceof Popup) {
					children.remove(child);
					if (child instanceof EventListener) {
						EventListener el = (EventListener) child;
						EventDispatcher.removeHandler(el);
					}
					return;
				}
			}
		}
	}

	public void showPopup(Popup menu) {
		closePopup();
		synchronized (children) {
			this.addChildEnd(menu);
		}
	}

	public boolean hasPopup() {
		synchronized (children) {
			for (UIElement child : children) {
				if (child instanceof Popup) {
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<UIElement> getChildren() {
		ArrayList<UIElement> childrenCopy = new ArrayList<UIElement>();
		synchronized (children) {
			for (UIElement child : children) {
				childrenCopy.add(child);
			}
		}
		return childrenCopy;
	}

	public void addChildStart(UIElement child) {
		synchronized (children) {
			if (!children.contains(child)) {
				children.add(0, child);
				child.setParent(this);
			}
			setChanged();
			childrenChanged = true;
		}
	}

	public void addChildEnd(UIElement child) {
		synchronized (children) {
			if (!children.contains(child)) {
				children.add(child);
				child.setParent(this);
			}
			setChanged();
			childrenChanged = true;
		}
	}

	public void removeChild(UIElement child) {
		synchronized (children) {
			children.remove(child);
			childrenChanged = true;
		}
		setChanged();
	}

	public void removeAllChildren() {
		synchronized (children) {
			while (children.size() > 0) {
				children.remove(0);
			}
			childrenChanged = true;
		}
		setChanged();
	}

	public boolean containsChild(UIElement e) {
		synchronized (children) {
			return children.contains(e);
		}
	}

	public boolean containsChildSomewhere(UIElement e) {
		synchronized (children) {
			if (children.contains(e)) {
				return true;
			}
			for (UIElement ele : children) {
				if (ele instanceof UIParent) {
					UIParent par = (UIParent) ele;
					if (par.containsChildSomewhere(e)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// @Override
	// public void render(){
	// location = new Vector3f(total)
	// super.render();
	// for(UIElement child : children){
	// child.render();
	// }
	// }

	public void renderForPicking(int totalx, int totaly) {
		if (ed.hidden) {
			return;
		}

		location2d = new Vector3f(totalx, totaly, 0);
		Util.checkErr();
		super.renderForPicking(totalx, totaly);
		Util.checkErr();
		synchronized (children) {
			for (UIElement child : children) {
				Util.checkErr();
				child.renderForPicking(totalx + child.x, totaly + child.y);
				Util.checkErr();
			}
		}
	}

	@Override
	public void render(int totalx, int totaly) {
		if (ed.hidden) {
			return;
		}

		location2d = new Vector3f(totalx, totaly, 0);
		Util.checkErr();
		super.render(totalx, totaly);
		Util.checkErr();
		synchronized (children) {
			for (UIElement child : children) {
				child.render(totalx + child.x, totaly + child.y);
			}
		}
	}

	@Override
	public void update(float delta) {
		synchronized (children) {
			for (UIElement child : children) {
				child.update(delta);
			}
		}
	}

	@Override
	public void setChanged() {
		changed = true;
		childrenChanged = true;
		if (parent != null) {
			parent.setChanged();
		}
	}

	public void setAllChanged() {
		synchronized (children) {
			this.setChanged();
			for (UIElement child : children) {
				if (child instanceof UIParent) {
					((UIParent) child).setAllChanged();
				} else {
					child.setChanged();
				}
			}
		}
	}

	@Override
	public UIElement getElementById(String name) {
		if (ed.id.equalsIgnoreCase(name)) {
			return this;
		}
		synchronized (children) {
			for (UIElement child : children) {
				UIElement ele = child.getElementById(name);
				if (ele != null) {
					return ele;
				}
			}
		}
		return null;
	}

	@Override
	public <T extends UIElement> ArrayList<T> getElementsByType(Class<T> type) {
		ArrayList<T> newList = new ArrayList<T>();
		getElementsByType(type, newList);
		return newList;
	}

	// It is checked, but the compiler warns as if it isn't
	@SuppressWarnings("unchecked")
	public <T extends UIElement> void getElementsByType(Class<T> type, ArrayList<T> list) {
		synchronized (children) {
			for (UIElement child : children) {
				if (child instanceof UIParent) {
					((UIParent) child).getElementsByType(type, list);
				}
				if (type.isInstance(child)) {
					list.add((T) child);
				}
			}
		}
	}

}
