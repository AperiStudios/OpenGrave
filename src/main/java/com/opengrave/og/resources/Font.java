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
package com.opengrave.og.resources;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.opengrave.og.MainThread;
import com.opengrave.og.Util;

public class Font {

	public Texture texture;
	Integer twidth;
	Integer theight;
	Integer chars;
	public Integer fontheight;
	public HashMap<Integer, FontData> fontData = new HashMap<Integer, FontData>();

	public Font() {
		fontheight = 0;
	}

	public Font(java.awt.Font font, boolean aA) {
		int yoffset = 0, xoffset = 0, count = 0, heightrow = 0, longestline = 0;
		int texH = 0, texW = 0;
		// Pass one, get sizes etc
		fontheight = 0;
		int charLimit = 4000; // TODO Theoretically we could do all Unicode in one image. Realistically, we need a better system of placing glyphs (no wasted space) and large texture support.
		// For now, limit to basic latin unicode
		int lineLimit = (int) (Math.sqrt(charLimit) * 1.3f); // Most chars are taller than wide, try to make up for it here without knowing
		for (int i = 0; i < charLimit; i++) {
			count++;
			if (count >= lineLimit) {
				count = 0;
				xoffset = 0;
				yoffset += heightrow;
				fontheight = Math.max(heightrow, fontheight);
			}
			if (i == 127) {
				continue;
			}
			char c = (char) i;
			BufferedImage ch = createCharImage(font, c, aA);
			if (ch == null) {
				continue;
			}

			longestline = Math.max(longestline, xoffset + ch.getWidth());
			heightrow = Math.max(ch.getHeight(), heightrow);
			xoffset += ch.getWidth();
		}
		texH = yoffset + heightrow;
		texW = longestline;
		BufferedImage image = new BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB);
		Graphics2D grap = image.createGraphics();
		grap.setBackground(new Color(1f, 1f, 1f, 0f));
		grap.clearRect(0, 0, texW, texH);
		xoffset = 0;
		yoffset = 0;
		System.out.println("Font texture sized " + texW + " " + texH);
		// TextureEditable ted = new TextureEditable(Math.max(texH, texW), 1f, 1f, 1f, 0f);
		// Pass two, create a texture
		for (int i = 0; i < charLimit; i++) {
			count++;
			if (count >= lineLimit) {
				count = 0;
				xoffset = 0;
				yoffset += heightrow + 1;
			}
			if (i == 127) {
				continue;
			}
			char c = (char) i;
			BufferedImage ch = createCharImage(font, c, aA);
			if (ch == null) {
				continue;
			}
			FontData fd = new FontData();
			fd.x = xoffset;
			fd.y = yoffset;
			fd.x2 = xoffset + ch.getWidth();
			fd.y2 = yoffset + ch.getHeight();
			fd.width = ch.getWidth();
			fd.height = ch.getHeight();
			fontData.put(i, fd);
			grap.drawImage(ch, xoffset, yoffset, null);
			xoffset += ch.getWidth() + 1;
		}
		System.out.println("Font image created");

		int[] pixels = new int[texW * texH];
		image.getRGB(0, 0, texW, texH, pixels, 0, texW);
		TextureAtlasEditable ted = new TextureAtlasEditable(Math.max(texH, texW), pixels, texW, texH);
		System.out.println("Created texture for font sized " + ted.size);
		texture = ted;
		int max = ted.size;
		for (FontData fd : fontData.values()) {
			fd.x = fd.x / max;
			fd.y = fd.y / max;
			fd.x2 = fd.x2 / max;
			fd.y2 = fd.y2 / max;

		}
	}

	public BufferedImage createCharImage(java.awt.Font font, char c, boolean aA) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		if (aA) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		g.dispose();
		int charWidth = metrics.charWidth(c);
		int charHeight = metrics.getHeight();
		if (charWidth <= 0) {
			return null;
		}
		image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
		g = image.createGraphics();
		g.setBackground(new Color(1f, 1f, 1f, 0f));
		g.clearRect(0, 0, charWidth, charHeight);
		if (aA) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		g.setFont(font);
		g.setPaint(new Color(1f, 1f, 1f, 1f));
		g.drawString(String.valueOf(c), 0, metrics.getAscent());
		g.dispose();
		return image;
	}

	public boolean loadFont(String name) {
		boolean passed = true;
		BufferedReader br;
		File f = new File(MainThread.cache, name);
		if (!f.exists()) {
			System.out.println("Cannot open file " + f.getAbsolutePath());
			return false;
		}
		FileReader fr;
		try {
			fr = new FileReader(f.getAbsolutePath());
		} catch (FileNotFoundException e) {
			System.out.println("Cannot open file " + f.getAbsolutePath());
			return false;
		}

		br = new BufferedReader(fr);
		try (Scanner scan = new Scanner(br)) {
			String sCurrentLine;
			sCurrentLine = scan.next();
			while (sCurrentLine != null) {
				if (sCurrentLine.equals("page")) {
					scan.next();
					String raw = scan.next();
					ArrayList<String> texNames = new ArrayList<String>();
					texNames.add("tex/" + raw.substring(6, raw.length() - 1));
					texture = Resources.loadTextures(texNames);
					Util.checkErr();
					scan.findWithinHorizon("\n", 0);
				} else if (sCurrentLine.equals("common")) {
					fontheight = getNumber(scan.next());
					scan.next();
					twidth = getNumber(scan.next());
					theight = getNumber(scan.next());
					scan.findWithinHorizon("\n", 0);
				} else if (sCurrentLine.equals("chars")) {
					chars = getNumber(scan.next());
					scan.findWithinHorizon("\n", 0);
				} else if (sCurrentLine.equals("char")) {
					FontData fd = new FontData();
					int chara = getNumber(scan.next());
					fd.character = chara;
					fd.x = getNumber(scan.next()) / (twidth * 1f);
					fd.y = getNumber(scan.next()) / (theight * 1f);
					fd.width = getNumber(scan.next());
					fd.height = getNumber(scan.next());
					fd.x2 = fd.x + fd.width / (twidth * 1f);
					fd.y2 = fd.y + fd.height / (theight * 1f);
					scan.findWithinHorizon("\n", 0);
					fontData.put(chara, fd);
				}
				sCurrentLine = scan.next();
			}
		} catch (java.util.NoSuchElementException e) {
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
			}
		}
		return passed;
	}

	private Integer getNumber(String s) {
		s = s.replaceAll("[^0-9]", "");
		return Integer.parseInt(s);
	}

}
