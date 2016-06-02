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
package com.opengrave.og.states;

import com.opengrave.common.MenuInfo;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.MainThread;
import com.opengrave.og.gui.ElementData;
import com.opengrave.og.gui.PopupMenu;
import com.opengrave.og.gui.callback.PopupOptionChosen;
import com.opengrave.og.input.InputMain;
import com.opengrave.og.resources.GUIXML;

public class SettingState extends BaseState implements EventListener {

	// CheckButton allowOtherJoinSession;
	// CheckButton showDebugMenu;
	// CheckButton capFPS;
	// NumberRoller capFPSNum;
	// Button menu, auto;
	private MenuInfo settingMenu;

	@Override
	public void start() {
		EventDispatcher.addHandler(this);
		GUIXML mainMenuFile = new GUIXML("gui/loading.xml");
		screen = mainMenuFile.getGUI();
		/*
		 * allowOtherJoinSession = (CheckButton) screen.getElementById("otherjoinsession");
		 * allowOtherJoinSession.setChecked(HGMainThread.config.getBoolean("otherjoinsession", true));
		 * showDebugMenu = (CheckButton) screen.getElementById("debugmenu");
		 * showDebugMenu.setChecked(HGMainThread.config.getBoolean("debugmenu", false));
		 * capFPS = (CheckButton) screen.getElementById("capfps");
		 * capFPS.setChecked(HGMainThread.config.getBoolean("capfps", true));
		 * capFPSNum = (NumberRoller) screen.getElementById("capfpsnum");
		 * capFPSNum.setNumber(HGMainThread.config.getInteger("fpslimit", 40));
		 * capFPSNum.setRange(15, 120);
		 * menu = (Button) screen.getElementById("menu");
		 * auto = (Button) screen.getElementById("auto");
		 */
		popup();
	}

	public void popup() {
		String allow = MainThread.config.getBoolean("otherjoinsession", false) ? "tex/guitick.png" : "tex/guicross.png";
		String vsync = MainThread.config.getBoolean("capfps", false) ? "tex/guitick.png" : "tex/guicross.png";
		String bgmute = MainThread.config.getBoolean("bgmute", false) ? "tex/guitick.png" : "tex/guicross.png";
		String sfxmute = MainThread.config.getBoolean("sfxmute", false) ? "tex/guitick.png" : "tex/guicross.png";
		settingMenu = new MenuInfo();
		settingMenu.addOptions("main", new PopupMenuOption("menu:gameplay", "none", "Gameplay"), new PopupMenuOption("menu:graphics", "none", "Graphics"),
				new PopupMenuOption("menu:sound", "none", "Sound"), new PopupMenuOption("menu:controls", "none", "Controls"));

		settingMenu.addOptions("gameplay", new PopupMenuOption("allowothers", allow, "Friends only in MP"));

		// TODO Quality levels, FPS Cap, Shadow QL
		settingMenu.addOptions("graphics", new PopupMenuOption("vsync", vsync, "VSync"));

		settingMenu.addOptions("sound", new PopupMenuOption("bgmute", bgmute, "Mute Music"), new PopupMenuOption("sfxmute", sfxmute, "Mute Sounds"));

		settingMenu.addOptions("controls", new PopupMenuOption("gamepad", "none", "Reconfigure Gamepad"), new PopupMenuOption("controls", "none",
				"Assign Controls"));
		PopupMenu pm = new PopupMenu(new ElementData());
		pm.setMenuOptions("main", 0, settingMenu, MainThread.lastW / 2, MainThread.lastH / 2, this);
		screen.showPopup(pm);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onMenuChosen(PopupOptionChosen event) {
		if (!isActive()) {
			return;
		}
		if (event.getReference() == this) {
			String id = event.getId();
			if (id.equals("allowothers")) {
				boolean b = !MainThread.config.getBoolean("otherjoinsession", false);
				event.getPopupMenuChoice().setImage(b ? "tex/guitick.png" : "tex/guicross.png");
				MainThread.config.setBoolean("otherjoinsession", b);
			} else if (id.equals("vsync")) {
				boolean b = !MainThread.config.getBoolean("capfps", false);
				event.getPopupMenuChoice().setImage(b ? "tex/guitick.png" : "tex/guicross.png");
				MainThread.config.setBoolean("capfps", b);
				MainThread.main.fpsCap = b ? 60 : -1;
			} else if (id.equals("bgmute")) {
				boolean b = !MainThread.config.getBoolean("bgmute", false);
				event.getPopupMenuChoice().setImage(b ? "tex/guitick.png" : "tex/guicross.png");
				MainThread.config.setBoolean("bgmute", b);
			} else if (id.equals("sfxmute")) {
				boolean b = !MainThread.config.getBoolean("sfxmute", false);
				event.getPopupMenuChoice().setImage(b ? "tex/guitick.png" : "tex/guicross.png");
				MainThread.config.setBoolean("sfxmute", b);
			} else if (id.equals("cancel")) {
				if (event.getMenuName().equals("main")) {
					// Closed top leve
					MainThread.changeState(new MenuState());
				} else {
					popup();
					event.setConsumed();
				}
			} else if (id.equals("gamepad")) {
				if (InputMain.cl == null || InputMain.c == null) {
					return;
				}
				MainThread.changeState(new ControllerSetupState(InputMain.cl, InputMain.c));
			} else if (id.equals("controls")) {
				MainThread.changeState(new BindingState());
			}
		}
	}

	@Override
	public void stop() {
		MainThread.config.save();
	}

	@Override
	public void update(float delta) {

	}
}
