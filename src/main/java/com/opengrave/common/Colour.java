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

public class Colour {

	public static Colour dC;
	public static Colour none = new Colour("axxxxxx");
	private int r, g, b;
	private boolean isBold, isItalic, isUnderline, isStrikethrough, isMagic;
	private String format;
	private boolean isDefault;
	public static String bit = "§";

	public boolean isBold() {
		return isBold;
	}

	public boolean isItalic() {
		return isItalic;
	}

	public boolean isUnderline() {
		return isUnderline;
	}

	public boolean isStrikethrough() {
		return isStrikethrough;
	}

	public boolean isMagic() {
		return isMagic;
	}

	public int getRed() {
		if (isDefault) {
			return dC.getRed();
		}
		return r;
	}

	public int getGreen() {
		if (isDefault) {
			return dC.getGreen();
		}
		return g;
	}

	public int getBlue() {
		if (isDefault) {
			return dC.getBlue();
		}
		return b;
	}

	public String getString() {
		if (isDefault) {
			return dC.getString();
		}
		return format;
	}

	public Colour(String format, int red, int green, int blue) {
		this.format = format;
		this.r = red;
		this.g = green;
		this.b = blue;
		isBold = false;
		isItalic = false;
		isUnderline = false;
		isStrikethrough = false;
		isMagic = false;
	}

	public Colour(String format, int red, int green, int blue, boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean magic) {
		this.format = format;
		this.r = red;
		this.g = green;
		this.b = blue;
		this.isBold = bold;
		this.isItalic = italic;
		this.isUnderline = underline;
		this.isStrikethrough = strikethrough;
		this.isMagic = magic;
	}

	public Colour(String hex) {
		// A colour to compare. NOT TO SEND
		this.format = "";
		int a = hex.charAt(0) - 'a';
		boolean[] bits = new boolean[7];
		for (int i = 6; i >= 0; i--) {
			bits[i] = (a & (1 << i)) != 0;
		}
		isBold = bits[0];
		isItalic = bits[1];
		isUnderline = bits[2];
		isStrikethrough = bits[3];
		isMagic = bits[4];
		if (hex.substring(1, 7).equalsIgnoreCase("xxxxxx")) {
			isDefault = true;
		} else {
			r = Integer.parseInt(hex.substring(1, 3), 16);
			g = Integer.parseInt(hex.substring(3, 5), 16);
			b = Integer.parseInt(hex.substring(5, 7), 16);
		}
	}

	public Colour(String string, boolean bold, boolean italic, boolean under, boolean strike, boolean magic) {
		// with for Defaults
		isDefault = true;
		this.isBold = bold;
		this.isItalic = italic;
		this.isUnderline = under;
		this.isStrikethrough = strike;
		this.isMagic = magic;
	}

	public String toInternal() {
		return "§" + toStream();
	}

	public String toStream() {
		if (isDefault) {
			return getCode() + "xxxxxx";
		}
		return getCode() + getHex(r) + getHex(g) + getHex(b);
	}

	private String getCode() {
		int a = (int) 'a';
		if (isBold) {
			a = a + 1;
		}
		if (isItalic) {
			a = a + 2;
		}
		if (isUnderline) {
			a = a + 4;
		}
		if (isStrikethrough) {
			a = a + 8;
		}
		if (isMagic) {
			a = a + 16;
		}
		return new String(Character.toChars(a));
	}

	private String getHex(int value) {
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toHexString(value));
		if (sb.length() < 2) {
			sb.insert(0, "0");
		} else if (sb.length() > 2) {

		}
		return sb.toString();
	}

	public Colour with(boolean bold, boolean italic, boolean under, boolean strike, boolean magic) {
		if (isDefault) {
			return new Colour("", bold, italic, under, strike, magic);
		}
		return new Colour("", r, g, b, bold, italic, under, strike, magic);
	}

	public String getHex() {
		return getHex(r) + getHex(g) + getHex(b);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Colour) {
			Colour col = (Colour) obj;
			return col.getBlue() == getBlue() && col.getRed() == getRed() && col.getGreen() == getGreen() && col.isBold() == isBold()
					&& col.isItalic() == isItalic() && col.isMagic() == isMagic() && col.isStrikethrough() == isStrikethrough()
					&& col.isUnderline() == isUnderline();
		}
		return false;
	}

	public float getR() {
		return getRed() / 256f;
	}

	public float getG() {
		return getGreen() / 256f;
	}

	public float getB() {
		return getBlue() / 256f;
	}
}
