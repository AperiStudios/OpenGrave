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
package com.opengrave.common.event;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.ModSession;
import com.opengrave.common.config.Config;
import com.opengrave.og.Util;
import com.opengrave.og.resources.Resources;

public class Mod {

	private ModSession session;
	static HashMap<String, Mod> loadedMods = new HashMap<String, Mod>();
	LuaValue library = null;

	public static boolean isLoaded(String modId) {
		return loadedMods.containsKey(modId);
	}

	public static ArrayList<Mod> getAll() {
		ArrayList<Mod> mods = new ArrayList<Mod>();
		for (Mod mod : loadedMods.values()) {
			mods.add(mod);
		}
		return mods;
	}

	public static Mod getMod(String modId) {
		if (modId == null || modId.length() == 0) {
			return null;
		}
		if (loadedMods.containsKey(modId)) {
			return loadedMods.get(modId);
		}
		String urlBase = "https://aperistudios.co.uk/hg/mod/";
		File modDir = new File(Resources.cache, "mods");
		File thisModDir = new File(modDir, modId);
		if (thisModDir.isFile()) {
			// Someone's playing silly buggers
			System.out.println("Error '" + thisModDir.getAbsolutePath() + "' is a file, no files should be present on the top level of /mods");
			System.exit(1);
		} else if (!thisModDir.isDirectory()) {
			thisModDir.mkdir();
		}
		String name = "Unnamed Mod", req, supp;
		try {
			HttpsURLConnection conn = Util.openConnection(urlBase + modId + "/info.config");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			Config c = new Config(in);
			name = c.getString("name", "Unnamed Mod");
			req = c.getString("requires", "");
		} catch (FileNotFoundException e) {
			System.out.println("No Mod with id '" + modId + "' - skipping");
			return null;
		} catch (MalformedURLException e) {
			new DebugExceptionHandler(e, urlBase, modId);
			return null;
		} catch (IOException e) {
			new DebugExceptionHandler(e, urlBase, modId);
			return null;
		}

		ArrayList<String> fileList = new ArrayList<String>();
		try {
			HttpsURLConnection conn = Util.openConnection(urlBase + modId + "/checksums");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine = "";
			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.replaceFirst(" +", " ");
				fileList.add(inputLine);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String string : fileList) {
			String[] s = string.split(" ");
			String upstreamHash = s[0];
			File file = new File(thisModDir, s[1]);
			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			String currentHash = Util.getDigest(file.getAbsolutePath());
			if (!upstreamHash.equals(currentHash)) {
				Util.downloadAndSave(thisModDir, urlBase + modId + "/" + s[1], s[1]);
			}

		}
		// Files are all up to date.
		ArrayList<String> dependsOn = new ArrayList<String>();
		for (String s : req.split("\n")) {
			if (s != null) {
				dependsOn.add(s);
			}
		}
		Mod mod = new Mod(modId, thisModDir, dependsOn);
		loadedMods.put(modId, mod);
		return mod;
	}

	private String id;
	private ArrayList<String> dependsOn = new ArrayList<String>();
	private ArrayList<String> itemTypeIds = new ArrayList<String>();
	private File dir;
	private ModSession sess;

	public Mod(String id, File dir, ArrayList<String> dependsOn) {
		this.id = id;
		this.dependsOn = dependsOn;
		this.dir = dir;
	}

	public String getId() {
		return id;
	}

	public ArrayList<String> getDependsOn() {
		return dependsOn;
	}

	public void start() {

	}

	public LuaValue load(Globals globals) {
		File ref = new File(dir, "main.lua");
		LuaValue file = globals.load(ref.getPath());
		return file;
	}

	public File getMainFile() {
		File ref = new File(dir, "main.lua");
		if (ref.isFile()) {
			return ref;
		} else {
			System.out.println("No Main file for mod '" + id + "'");

		}
		return null;

	}

	public void setLibrary(LuaValue returnedLibrary) {
		library = returnedLibrary;
	}

	public LuaValue getLibrary() {
		return library;
	}

	public void setSession(ModSession modSession) {
		this.sess = modSession;
	}

	public ModSession getSession() {
		return sess;
	}

}
