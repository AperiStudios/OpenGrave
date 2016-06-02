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

import java.io.*;
import java.util.HashMap;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.og.MainThread;

public class SourceFile {

	private String source = "";
	private static HashMap<String, SourceFile> loadedFiles = new HashMap<String, SourceFile>();
	public boolean usesLighting = false;
	private boolean isFrag, isVert;

	public static SourceFile loadFile(String name) {
		if (loadedFiles.containsKey(name)) {
			return loadedFiles.get(name);
		}

		FileReader in = null;
		File f = new File(MainThread.cache, name);
		try {
			in = new FileReader(f.getAbsolutePath());
		} catch (FileNotFoundException e1) {
			System.out.println("Cannot open file " + f.getAbsolutePath());
			return null;
		}
		BufferedReader br = new BufferedReader(in);
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			new DebugExceptionHandler(e, name);
		}
		SourceFile sf = new SourceFile();
		if (name.toLowerCase().endsWith(".fs")) {
			sf.isFrag = true;
		} else if (name.toLowerCase().endsWith(".vs")) {
			sf.isVert = true;
		}
		while (line != null) {
			if (line.toLowerCase().startsWith("#uses")) {
				String fileName = line.split(" ", 2)[1];
				fileName = fileName.replaceAll("\"", "").replaceAll("'", ""); // Remove
																				// any
																				// quotes
				if (fileName.equalsIgnoreCase("lighting")) {
					sf.usesLighting = true;
				}
				// SourceFile includeFile = loadFile(fileName);
				// sf.source = sf.source + "\n" + includeFile.source + "\n";

			} else if (line.toLowerCase().startsWith("#include")) {
				String part = line.split(" ", 2)[1];
				String shaderType = "";
				if (sf.isFrag) {
					shaderType = "fs";
				} else if (sf.isVert) {
					shaderType = "vs";
				}
				String lightingVersion = "none";
				if (sf.usesLighting) {
					// Include lighting header - based on config
					if (MainThread.config.getBoolean("shadows", true)) {
						// We have shadows
						lightingVersion = "basic";
						int count = MainThread.config.getInteger("lightCount", 0);
						if (count >= 1 && count <= 4) {
							lightingVersion = "four";
						} else if (count >= 5 && count <= 8) {
							lightingVersion = "eight";
						} else if (count >= 9) {
							lightingVersion = "more";
						}
					}
				}
				if (part.equalsIgnoreCase("header")) {
					if (sf.usesLighting) {
						SourceFile includeFile = loadFile("sdr/inc/lighting-" + lightingVersion + "-" + shaderType + ".h");
						sf.source = sf.source + "\n" + includeFile.source + "\n";
					}

				} else if (part.equalsIgnoreCase("source")) {
					if (sf.usesLighting) {
						SourceFile includeFile = loadFile("sdr/inc/lighting-" + lightingVersion + "-" + shaderType + ".s");
						sf.source = sf.source + "\n" + includeFile.source + "\n";
					}
				}
			} else {
				sf.source = sf.source + line + "\n";
			}
			try {
				line = br.readLine();
			} catch (IOException e) {
				new DebugExceptionHandler(e, name);
				line = null;
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			new DebugExceptionHandler(e, name);
		}
		// System.out.print
		// System.out.println(sf.source);
		loadedFiles.put(name, sf);
		return sf;
	}

	public String getSource() {
		return source;
	}
}
