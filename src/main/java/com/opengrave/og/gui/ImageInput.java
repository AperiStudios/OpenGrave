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

import javax.swing.JOptionPane;

import net.java.games.input.Controller;

import com.opengrave.common.MenuInfo;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.MainThread;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.gui.callback.PopupOptionChosen;
import com.opengrave.og.input.*;
import com.opengrave.og.resources.Font;
import com.opengrave.og.resources.TextureAtlas;
import com.opengrave.og.util.Vector4f;

public class ImageInput extends Image implements TextInterface, EventListener {
	public static TextureAtlas textureStatic;
	private TextArea ta;
	private ImageInputInner inner;
	Vector4f realColour = new Vector4f(1f, 1f, 1f, 1f);
	public static MenuInfo menuStatic;

	public ImageInput(ElementData ed) {
		super(ed);
		this.texture = textureStatic;
		ta = new TextArea(new ElementData(ed));
		inner = new ImageInputInner(new ElementData(ed));
		addChildEnd(inner);

		addChildEnd(ta);
		if (menuStatic == null) {
			menuStatic = new MenuInfo();
			menuStatic.addOptions("col", new PopupMenuOption("colred", "none", "Red").setColour(1f, 0f, 0f),
					new PopupMenuOption("colblue", "none", "Blue").setColour(0f, 0f, 1f),
					new PopupMenuOption("colgreen", "none", "Green").setColour(0f, 1f, 0f),
					new PopupMenuOption("colyellow", "none", "Yellow").setColour(1f, 1f, 0f), new PopupMenuOption("colgrey", "none", "Grey"),
					new PopupMenuOption("colblack", "none", "Black").setColour(0.2f, .2f, .2f));
			menuStatic.addOptions("colicon", new PopupMenuOption("coliconred", "none", "Red").setColour(1f, 0f, 0f), new PopupMenuOption("coliconblue", "none",
					"Blue").setColour(0f, 0f, 1f), new PopupMenuOption("colicongreen", "none", "Green").setColour(0f, 1f, 0f), new PopupMenuOption(
					"coliconyellow", "none", "Yellow").setColour(1f, 1f, 0f), new PopupMenuOption("coliconmagenta", "none", "Magenta").setColour(1f, 0f, 1f),
					new PopupMenuOption("coliconwhite", "none", "White"), new PopupMenuOption("coliconblack", "none", "Black").setColour(.2f, .2f, .2f),
					new PopupMenuOption("colicontrans", "none", "Transparent"));
			menuStatic.addOptions("icon", new PopupMenuOption("iconnone", "none", "None"), new PopupMenuOption("iconsquare", "tex/buttonicon1.png", "Square"),
					new PopupMenuOption("iconcircle", "tex/buttonicon2.png", "Circle"), new PopupMenuOption("iconcross", "tex/buttonicon3.png", "Cross"),
					new PopupMenuOption("icontri", "tex/buttonicon4.png", "Triangle"), new PopupMenuOption("iconhome", "tex/buttonicon5.png", "Home"));
			menuStatic.addOptions("buttonicon", new PopupMenuOption("buttonround", "tex/buttonround.png", "Round"), new PopupMenuOption("buttontrigger",
					"tex/buttontrigger.png", "Tall"), new PopupMenuOption("buttondpad", "tex/buttondpad.png", "D Pad"), new PopupMenuOption("buttonlong",
					"tex/buttonroundlong.png", "Long"), new PopupMenuOption("buttonstick", "tex/buttonstick.png", "Stick"));
			menuStatic.addOptions("main", new PopupMenuOption("menu:buttonicon:0", "none", "Button"), new PopupMenuOption("menu:col:0", "none", "Colours"),
					new PopupMenuOption("menu:icon:0", "none", "Icon"), new PopupMenuOption("menu:colicon:0", "none", "Icon Colours"), new PopupMenuOption(
							"changetext", "none", "Label"));
		}

	}

	@Override
	public void attributesChanged() {
		if (ed.attributes.containsKey("input")) {
			set(InputMain.getControlIcon(ed.attributes.get("input")));
		}
	}

	@Override
	public String getString() {
		return ta.getString();
	}

	@Override
	public void setString(String s) {
		ta.setString(s);
	}

	@Override
	public void setSize(int width, int height, int mwidth, int mheight) {
		if (textureIndex == -1) { // Keyboard binding
			this.height = 32;
			ta.setSize(width, height, width, height);
			this.width = ta.width + 4;
			if (this.width < 32) {
				this.width = 32;
			}
			int diff = this.width - ta.width;
			ta.x = diff / 2;
			diff = this.height - ta.height;
			ta.y = diff / 2;
			setChanged();
			return;
		}
		this.width = 32;
		if (textureIndex == 3) {
			this.width = 64;
		}
		this.height = 32;
		ta.setSize(width, height, width, height); // We can rely on TA to resize up to fit
		if (this.height < ta.height) {
			this.height = ta.height;
			ta.y = 0;

		} else {
			int diff = this.height - ta.height;
			ta.y = diff / 2;
		}
		if (this.width < ta.width) {
			ta.x = 0;
		} else {
			int diff = this.width - ta.width;
			ta.x = diff / 2;
		}
		if (textureIndex == 4) {
			ta.y = -4;
		}
		inner.x = 8;
		inner.y = 8;
		inner.width = 16;
		inner.height = 16;
		setChanged();
	}

	boolean isListening = false, isListeningPress = false, isListeningClick = false;
	private Controller cont;
	private int index;
	private ControllerLayout controllerLayout;

	public void setControlInfo(Controller cont, int index) {
		isListeningPress = true;
		if (!isListening) {
			EventDispatcher.addHandler(this);
			isListening = true;
		}
		this.cont = cont;
		this.index = index;
		this.colour = new Vector4f(realColour.x / 2f, realColour.y / 2f, realColour.z / 2f, 1f);
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onClick(MouseButtonRenderableEvent event) {
		if (!isListeningClick) {
			return;
		}
		if (isThis(event.getObject())) {
			event.isConsumed();
			PopupMenu pm = new PopupMenu(new ElementData());
			pm.setMenuOptions("main", 0, menuStatic, event.getScreenX(), event.getScreenY(), this);
			MainThread.getGameState().screen.showPopup(pm);
		}
	}

	private boolean isThis(Pickable object) {
		return object == this || object == ta || object == inner;
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onJoystick(JoystickRawAxisEvent event) {
		if (!isListeningPress) {
			return;
		}
		if (event.getPad().equals(cont)) {
			if (event.getAxisIndex() == index) {
				if (event.getValue() > .5f) {
					this.colour = realColour;
				} else {
					this.colour = new Vector4f(realColour.x / 2f, realColour.y / 2f, realColour.z / 2f, 1f);
				}
			}
		}
	}

	public void setColours(Vector4f colour1, Vector4f colour2) {
		realColour = colour1;
		this.colour = realColour;
		inner.colour = colour2;
		ta.ed.textColour = colour2;
		setChanged();

	}

	public void setIcons(int icon1, int icon2) {
		this.textureIndex = icon1;
		inner.textureIndex = icon2;
		setChanged();

	}

	public void setCustomisation(ControllerLayout controllerLayout) {
		this.pickable = true;
		isListeningClick = true;
		if (!isListening) {
			EventDispatcher.addHandler(this);
			isListening = true;
		}
		this.controllerLayout = controllerLayout;
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onMenuChosen(PopupOptionChosen event) {
		if (!isListeningClick) {
			return;
		}
		if (event.getReference() == this) {
			String id = event.getId();
			if (id.equalsIgnoreCase("colred")) {
				realColour = new Vector4f(1f, 0f, 0f, 1f);
			} else if (id.equalsIgnoreCase("colblue")) {
				realColour = new Vector4f(0f, 0f, 1f, 1f);
			} else if (id.equalsIgnoreCase("colgreen")) {
				realColour = new Vector4f(0f, 1f, 0f, 1f);
			} else if (id.equalsIgnoreCase("colyellow")) {
				realColour = new Vector4f(1f, 1f, 0f, 1f);
			} else if (id.equalsIgnoreCase("colgrey")) {
				realColour = new Vector4f(0.8f, 0.8f, 0.8f, 1f);
			} else if (id.equalsIgnoreCase("colblack")) {
				realColour = new Vector4f(0.2f, 0.2f, 0.2f, 1f);
			} else if (id.equalsIgnoreCase("coliconred")) {
				inner.colour = new Vector4f(1f, 0f, 0f, 1f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("coliconblue")) {
				inner.colour = new Vector4f(0f, 0f, 1f, 1f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("colicongreen")) {
				inner.colour = new Vector4f(0f, 1f, 0f, 1f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("coliconyellow")) {
				inner.colour = new Vector4f(1f, 1f, 0f, 1f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("coliconwhite")) {
				inner.colour = new Vector4f(1f, 1f, 1f, 1f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("coliconblack")) {
				inner.colour = new Vector4f(0f, 0f, 0f, 1f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("coliconmagenta")) {
				inner.colour = new Vector4f(1f, 0f, 1f, 1f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("colicontrans")) {
				inner.colour = new Vector4f(0f, 0f, 0f, 0f);
				ta.ed.textColour = inner.colour;
			} else if (id.equalsIgnoreCase("iconnone")) {
				inner.textureIndex = 0;
			} else if (id.equalsIgnoreCase("iconsquare")) {
				inner.textureIndex = 1;
			} else if (id.equalsIgnoreCase("iconcircle")) {
				inner.textureIndex = 2;
			} else if (id.equalsIgnoreCase("iconcross")) {
				inner.textureIndex = 3;
			} else if (id.equalsIgnoreCase("icontri")) {
				inner.textureIndex = 4;
			} else if (id.equalsIgnoreCase("iconhome")) {
				inner.textureIndex = 5;
			} else if (id.equalsIgnoreCase("changetext")) {
				String s = (String) JOptionPane.showInputDialog(null, "Button label (maximum length of 4)", "Label", JOptionPane.PLAIN_MESSAGE, null, null,
						getString());
				if (s != null) {
					setString(s);
				}
			} else if (id.equalsIgnoreCase("buttonround")) {
				textureIndex = 0;
			} else if (id.equalsIgnoreCase("buttontrigger")) {
				textureIndex = 1;
			} else if (id.equalsIgnoreCase("buttondpad")) {
				textureIndex = 2;
			} else if (id.equalsIgnoreCase("buttonlong")) {
				textureIndex = 3;
			} else if (id.equalsIgnoreCase("buttonstick")) {
				textureIndex = 4;
			}
			ControlDescription buttInfo = controllerLayout.getComponent(index);
			buttInfo.putColour1(realColour);
			buttInfo.putColour2(inner.colour);
			buttInfo.putIcon1(textureIndex);
			buttInfo.putIcon2(inner.textureIndex);
			buttInfo.putLabel(ta.getString());
			this.colour = new Vector4f(realColour.x / 2f, realColour.y / 2f, realColour.z / 2f, 1f);
		}
	}

	public void set(ControlDescription c) {
		if (c == null) {
			return;
		}
		setIcons(c.getIcon1(), c.getIcon2());
		setColours(c.getColour1(), c.getColour2());
		setString(c.getLabel());
	}

	public void set(InputBinding ib) {
		set(InputMain.getControlIcon(ib));

	}

	@Override
	public void setFont(Font f) {
		ta.setFont(f);
	}
}