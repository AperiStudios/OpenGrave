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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.gui.callback.TextSelectedEvent;
import com.opengrave.og.input.MouseButtonRenderableEvent;
import com.opengrave.og.resources.Font;
import com.opengrave.og.resources.FontData;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Vector4f;

public abstract class BaseText extends UIElement implements TextInterface, EventListener {
	Pattern colour, link;
	boolean picking = false;
	private boolean background;

	public BaseText(ElementData ed) {
		super(ed);
		EventDispatcher.addHandler(this);
	}

	private String contents = "";
	private Font f = null;

	public Font getFont() {
		if (f == null) {
			return Resources.defaultFont;
		}
		return f;
	}

	@Override
	public void setFont(Font font) {
		changed = true;
		f = font;
	}

	@Override
	public String getString() {
		return contents;
	}

	@Override
	public void setString(String contents) {
		changed = true;
		this.contents = contents;
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onMousePress(MouseButtonRenderableEvent event) {
		if (event.getObject() == this && event.getButton() == 0) {
			for (UIQuad quad : this.getQuads()) {
				if (quad.isInside(event.getRX(), event.getRY())) {
					if (quad.meta == null) {
						continue;
					}
					event.setConsumed();
					TextSelectedEvent tsevent = new TextSelectedEvent();
					tsevent.area = this;
					tsevent.link = quad.meta;
					EventDispatcher.dispatchEvent(tsevent);
				}
			}
		}
	}

	@Override
	public void repopulateQuads() {
		if (background) {
			addQuad(new UIQuad().setPos(0, 0, width, height).setColour(ed.disabledColour));
		}
		checkRegexs();
		picking = false;
		Font font = getFont();
		if (font == null) {
			return;
		}
		texture = font.texture;
		int x = 0, y = 0, index = 0;
		if (contents == null) {
			contents = "";
		}
		Vector4f col = getTextColour();
		String meta = null;
		for (index = 0; index < contents.length(); index++) {
			String remainder = contents.substring(index);
			Matcher mc = colour.matcher(remainder);
			Matcher ml = link.matcher(remainder);
			if (remainder.startsWith("@") && meta != null) {
				meta = null;
				continue;
			}
			if (mc.find()) {
				String hex = mc.group(1);
				col = new Vector4f(Integer.parseInt(hex.substring(0, 1), 16) / 15f, Integer.parseInt(hex.substring(1, 2), 16) / 15f, Integer.parseInt(
						hex.substring(2, 3), 16) / 15f, 1f);
				index += 3;
				continue;
			} else if (ml.find()) {
				picking = true;
				String match = ml.group(1);
				meta = match;
				index += match.length() + 2;
				continue;
			}
			char c = contents.charAt(index);
			int i = (int) c;
			if (i == 10) { // New Line
				y += font.fontheight;
				x = 0;
			} else {
				FontData fd = font.fontData.get(i);
				if (fd == null) {
					// System.out.println("Null char in font : " + i);
					continue;
				}
				UIQuad q = new UIQuad();
				q.setPos(x, y, x + fd.width, y + fd.height).setTexture(fd.x, fd.x2, fd.y, fd.y2, 0).setColour(col.x, col.y, col.z, col.w);
				q.meta = meta;
				x += fd.width;
				addQuad(q);
			}
		}
	}

	private void checkRegexs() {
		if (colour == null) {
			colour = Pattern.compile("^#([0-9a-fA-F]{3})");
		}
		if (link == null) {
			link = Pattern.compile("^@<(.*?)>");
		}
	}

	/**
	 * Read through the current string and give the smallest width possible
	 * without adding extra new lines
	 * 
	 * @return
	 */
	public int getTextMinWidth() {
		checkRegexs();
		int x = 0, index = 0;
		int maxWidth = 1;
		if (contents == null) {
			contents = "";
		}
		for (index = 0; index < contents.length(); index++) {
			String remainder = contents.substring(index);
			// First, Check if we're at the start of a colour code
			Matcher mc = colour.matcher(remainder);
			Matcher ml = link.matcher(remainder);
			if (mc.find()) {
				// String hex = mc.group(0);
				// Vector4f col = new
				// Vector4f(Integer.parseInt(hex.substring(0,1),16)/15f,
				// Integer.parseInt(hex.substring(1,2),16)/15f,
				// Integer.parseInt(hex.substring(2,3),16)/15f,1f);
				index += 3;
				continue;
			} else if (ml.find()) {
				String match = ml.group(1);
				index += match.length();
				continue;
			}
			char c = contents.charAt(index);
			int i = (int) c;
			if (i == 10) { // New Line
				if (x > maxWidth) {
					maxWidth = x;
				}
				x = 0;
			} else {
				FontData fd = getFont().fontData.get(i);
				if (fd == null) {
					continue;
				}
				x += fd.width;
			}
		}
		if (x > maxWidth) {
			maxWidth = x;
		}
		return maxWidth;
	}

	@Override
	protected boolean shouldRenderForPicking() {
		return picking;
	}

	public void drawBackground(boolean b) {
		background = b;
	}
}
