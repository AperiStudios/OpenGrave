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

import java.awt.FontFormatException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.og.models.DAEAnimCollection;
import com.opengrave.og.models.DAEFile;
import com.opengrave.og.models.DAEStaticCollection;

public class Resources {

	private static HashMap<String, Texture> loadedTextures = new HashMap<String, Texture>();
	private static HashMap<String, Font> loadedFonts = new HashMap<String, Font>();
	private static HashMap<String, DAEFile> loadedModels = new HashMap<String, DAEFile>();
	private static HashMap<String, DAEStaticCollection> loadedStaticModels = new HashMap<String, DAEStaticCollection>();
	private static HashMap<String, DAEAnimCollection> loadedAnimatedModels = new HashMap<String, DAEAnimCollection>();
	private static HashMap<String, ShaderProgram> loadedShaders = new HashMap<String, ShaderProgram>();

	public static Font defaultFont = null;

	public static File cache;

	public static ShaderProgram loadShader(String sfv, String sff) {
		String label = sfv + ":" + sff;
		if (loadedShaders.containsKey(label)) {
			return loadedShaders.get(label);
		}
		ShaderProgram sdr = ShaderProgram.makeShaderProgram(sfv, sff);
		loadedShaders.put(label, sdr);
		return sdr;
	}

	public static Font getFont(File f, int size) {
		Font font = null;
		String[] nameSplit = f.getName().split(".");
		int last = nameSplit.length - 1;
		if (nameSplit[last].equalsIgnoreCase("ttf")) { // True type
			String id = f.getPath() + ":" + size;
			if (loadedFonts.containsKey(id)) {
				return loadedFonts.get(id);
			}
			java.awt.Font ttf = null;
			try {
				ttf = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, f).deriveFont(size);
			} catch (FontFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (ttf == null) {
				return null;
			}
			font = new Font(ttf, true);
			loadedFonts.put(id, font);
			return font;
		} else if (nameSplit[last].equalsIgnoreCase("fnt")) { // Shit old font format
			String id = f.getPath();
			if (loadedFonts.containsKey(id)) {
				return loadedFonts.get(id);
			}
			font = loadFont(f.getPath());
			loadedFonts.put(id, font);
			return font;
		}
		return font;
	}

	public static void loadFonts() {
		// defaultFont = new Font();// Empty font
		/*
		 * File f = new File(cache, "fnt/");
		 * if (f.exists()) {
		 * for (File f2 : f.listFiles()) {
		 * if (f2.isFile() && f2.getAbsolutePath().endsWith(".fnt")) {
		 * Font fnt = loadFont("fnt/" + f2.getName());
		 * if (fnt != null) {
		 * if (firstFont == null) {
		 * firstFont = fnt;
		 * } else if (f2.getName().contains("Mono")) {
		 * // Prefer Mono
		 * firstFont = fnt;
		 * }
		 * }
		 * }
		 * }
		 * }
		 */
	}

	public static void loadModels() {
		// Don't autoload models here. Models are loaded by either the Client State or the Mod that needs them
	}

	public static DAEAnimCollection getAnimatedModel(String label) {
		if (loadedAnimatedModels.containsKey(label)) {
			return loadedAnimatedModels.get(label);
		}
		DAEAnimCollection collection = new DAEAnimCollection(label);
		loadedAnimatedModels.put(label, collection);
		return collection;
	}

	public static DAEStaticCollection getStaticModel(String label) {
		if (loadedStaticModels.containsKey(label)) {
			return loadedStaticModels.get(label);
		}
		DAEStaticCollection collection = new DAEStaticCollection(label);
		loadedStaticModels.put(label, collection);
		// "mod/tree.dae:trunk,branches:canopy"
		return collection;
	}

	public static DAEFile loadModelFile(String name) {
		if (loadedModels.containsKey(name)) {
			return loadedModels.get(name);
		}
		DAEFile file = new DAEFile();
		try {
			file.parseData(name);
		} catch (ParserConfigurationException e) {
			new DebugExceptionHandler(e, name);
		} catch (SAXException e) {
			new DebugExceptionHandler(e, name);
		} catch (IOException e) {
			new DebugExceptionHandler(e, name);
		}
		loadedModels.put(name, file);
		return file;
	}

	public static Font loadFont(String name) {
		if (loadedFonts.containsKey(name)) {
			return loadedFonts.get(name);
		}
		Font font = new Font();
		font.loadFont(name);
		loadedFonts.put(name, font);
		return font;
	}

	public static Texture loadTexture(String s) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(s);
		return loadTextures(list);
	}

	public static Texture loadTextures(String... names) {
		String nameId = "";
		for (String name : names) {
			nameId = nameId + ":" + name;
		}
		if (loadedTextures.containsKey(nameId)) {
			return (TextureAtlas) loadedTextures.get(nameId);
		}
		ArrayList<String> list = new ArrayList<String>();
		for (String name : names) {
			list.add(name);
		}
		TextureAtlas texture = TextureAtlas.create(list);

		if (texture != null && texture.isValid()) {
			Resources.loadedTextures.put(nameId, texture);
		} else {
			System.out.println("Failed to load " + nameId);
		}
		return texture;
	}

	public static TextureAtlas loadTextures(ArrayList<String> names) {
		String nameId = "";
		for (String name : names) {
			nameId = nameId + ":" + name;
		}
		if (loadedTextures.containsKey(nameId)) {
			return (TextureAtlas) loadedTextures.get(nameId);
		}
		ArrayList<String> list = new ArrayList<String>();
		for (String name : names) {
			list.add(name);
		}
		TextureAtlas texture = TextureAtlas.create(list);

		if (texture != null && texture.isValid()) {
			Resources.loadedTextures.put(nameId, texture);
		} else {
			System.out.println("Failed to load " + nameId);
		}
		return texture;
	}

	public static void loadTextures() {

	}

	public static void writeTextFile(String uri, String contents) {
		File file = new File(cache, uri);
		try (PrintWriter out = new PrintWriter(file)) {
			out.print(contents);
		} catch (FileNotFoundException e) {
			// new DebugExceptionHandler(e, uri, contents);
		}
	}

	public static String loadTextFile(String string) {
		File file = new File(cache, string);
		if (file.exists()) {
			FileReader in = null;
			StringBuilder sb = new StringBuilder();
			try {
				in = new FileReader(file.getAbsolutePath());
			} catch (FileNotFoundException e1) {
				System.out.println("Cannot open file " + file.getAbsolutePath());
				return null;
			}
			BufferedReader buff = new BufferedReader(in);
			String line = null;
			try {
				while ((line = buff.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} catch (IOException e) {
				new DebugExceptionHandler(e, string);
			} finally {
				try {
					buff.close();
				} catch (IOException e) {
				}
			}

			return sb.toString();
		}
		return "";
	}

	public static void removeShadersWithLighting() {
		for (String key : loadedShaders.keySet()) {
			ShaderProgram sp = loadedShaders.get(key);
			if (sp.usesLighting()) {
				loadedShaders.remove(key);
			}
		}

	}

}
