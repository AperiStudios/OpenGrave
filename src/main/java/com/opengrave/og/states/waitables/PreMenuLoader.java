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
package com.opengrave.og.states.waitables;

import java.io.File;
import java.util.ArrayList;

import com.opengrave.common.event.*;
import com.opengrave.og.MainThread;
import com.opengrave.og.input.ControllerController;
import com.opengrave.og.resources.Font;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.states.PreMenuState;

public class PreMenuLoader extends Loader implements Runnable, EventListener {

	private String message = null;
	private String mods = null;
	private boolean done = false;
	private ControllerController cc;

	public PreMenuLoader() {
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onAuth(ClientAuthStatusEvent event) {
		mods = event.getMods();
	}

	@Override
	public void run() {
		// message = "Loading font... Wait a minute... Wat";
		java.awt.Font font = new java.awt.Font(java.awt.Font.SERIF, java.awt.Font.PLAIN, 14);
		Font fontReal = new Font(font, true);
		Resources.defaultFont = fontReal;
		message = "Logging In";
		while (mods == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		message = "Preparing Mod List";
		File modDir = new File(Resources.cache, "mods");
		if (modDir.isFile()) {
			System.out.println("Error '/mod' is a file, must be a directory!");
			System.exit(1);
		} else if (!modDir.isDirectory()) {
			modDir.mkdir();
		}
		// Download mod files
		ArrayList<String> modsToLoad = new ArrayList<String>();
		for (String modNum : mods.split(",")) {
			modsToLoad.add(modNum);
		}
		while (modsToLoad.size() > 0) {
			String id = modsToLoad.remove(0);
			Mod mod = Mod.getMod(id);
			if (mod == null) {
				continue;
			}
			for (String dependsOn : mod.getDependsOn()) {
				if (!Mod.isLoaded(dependsOn)) { // Save us from infinite loop
					modsToLoad.add(dependsOn); // and lead us not into null
												// pointer
				}
			}
		}
		File dir = new File(MainThread.cache, "input");
		File[] listing = dir.listFiles();
		cc = new ControllerController();
		if (listing != null) {
			for (File file : listing) {
				if (file.getName().toLowerCase().endsWith(".pad")) {
					cc.addController(file.getName());
				}
			}
		}
		// Mods cached, Loading etc should ideally be done before session start,
		// not here
		done = true;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void finish() {
		MainThread.changeState(new PreMenuState(cc));
	}

	@Override
	public void start() {
		EventDispatcher.addHandler(this);
	}

	@Override
	public String getStatusMessage() {
		return message;
	}

}
