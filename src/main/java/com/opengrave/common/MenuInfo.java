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
package com.opengrave.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.opengrave.og.input.InputMain;
import com.opengrave.og.util.Vector4f;

public class MenuInfo {
	public enum Cancel {
		First, Last
	}

	Cancel cancelPos = Cancel.First;
	Vector4f cancelCol = new Vector4f(1f, 1f, 1f, 1f);
	String cancelName = "Cancel";
	HashMap<String, ArrayList<PopupMenuOption>> options = new HashMap<String, ArrayList<PopupMenuOption>>();

	public ArrayList<PopupMenuOption> getList(String context, int page) {
		ArrayList<PopupMenuOption> bits = new ArrayList<PopupMenuOption>();
		if (cancelPos == Cancel.First) {
			PopupMenuOption pmo = new PopupMenuOption("cancel", "tex/guicross.png", cancelName);
			pmo.setControl("menu_cancel");
			bits.add(pmo);
		}
		int maxOpts = (InputMain.getMaxOptions());
		if (options.containsKey(context)) {
			int offset = page * maxOpts; // Space to cancel and
											// next
			for (int i = 0; i < maxOpts && (i + offset) < options.get(context).size(); i++) {
				PopupMenuOption pmo = new PopupMenuOption(options.get(context).get(offset + i));
				pmo.setControl("menu_" + (i + 1));
				bits.add(pmo);
			}
			if (maxOpts < options.get(context).size()) {
				// We need a next
				PopupMenuOption pmo = new PopupMenuOption("menu:" + context + ":" + (page + 1), "tex/guinext.png", "More");
				pmo.setControl("menu_next");
				bits.add(pmo);
			}
		}

		if (cancelPos == Cancel.Last) {
			PopupMenuOption pmo = new PopupMenuOption("cancel", "tex/guicross.png", cancelName).setColour(cancelCol.x, cancelCol.y, cancelCol.z);
			pmo.setControl("menu_cancel");
			bits.add(pmo);
		}
		return bits;
	}

	public void removeOptions(String key) {
		this.options.remove(key);
	}

	public void addOptions(String key, ArrayList<PopupMenuOption> options) {
		ArrayList<PopupMenuOption> pmo = new ArrayList<PopupMenuOption>();
		if (this.options.containsKey(key)) {
			pmo = this.options.get(key);
		}
		for (PopupMenuOption o : options) {
			pmo.add(o);
		}
		this.options.put(key, pmo);
	}

	public void addOptions(String key, PopupMenuOption... options) {
		ArrayList<PopupMenuOption> pmo = new ArrayList<PopupMenuOption>();
		if (this.options.containsKey(key)) {
			pmo = this.options.get(key);
		}
		for (PopupMenuOption o : options) {
			pmo.add(o);
		}
		this.options.put(key, pmo);
	}

	public void addOption(String key, PopupMenuOption option) {
		ArrayList<PopupMenuOption> pmo = new ArrayList<PopupMenuOption>();
		if (this.options.containsKey(key)) {
			pmo = this.options.get(key);
		}
		pmo.add(option);

		this.options.put(key, pmo);
	}

	public void setCancel(Cancel last, String string, Vector4f col) {
		cancelName = string;
		cancelPos = last;
		cancelCol = col;
	}

	public Set<String> getContexts() {
		return this.options.keySet();
	}

	public ArrayList<PopupMenuOption> getFullList(String string) {
		return options.get(string);
	}

}
