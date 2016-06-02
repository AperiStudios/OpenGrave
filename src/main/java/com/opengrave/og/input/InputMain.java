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
package com.opengrave.og.input;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerListener;

import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.EventHandler;
import com.opengrave.common.event.EventHandlerPriority;
import com.opengrave.common.event.EventListener;
import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.base.Picking;
import com.opengrave.og.base.PickingResult;
import com.opengrave.og.engine.Location;
import com.opengrave.og.gui.UIElement;
import com.opengrave.og.gui.callback.UIElementFocusEvent;
import com.opengrave.og.gui.callback.UIElementMouseOverEvent;
import com.opengrave.og.states.BaseState;

public class InputMain implements EventListener {
	UIElement lastScreen = null;
	UIElement lastMouseOverElement = null;
	public static UIElement lastFocused = null;
	public static boolean CONTROL_WITH_MKB = true, CONTROL_WITH_GPAD = true;
	Location lastLoc = null;
	// boolean[] buttonsDown = new boolean[12];
	public static ArrayList<ControlBinding> bindings = new ArrayList<ControlBinding>();
	public static Controller c = null;
	public static ControllerLayout cl = null;
	public ArrayList<Float> controlLastValue = new ArrayList<Float>();
	private MouseRenderableHoverEvent lastHover;
	private MouseHandler mouseCallback;
	private KeyboardRawHandler keyCallback;
	private MouseButtonHandler mouseButtonCallback;
	private static InputMain instance;

	public InputMain() {
		instance = this;
		ControllerListener controllerPluggedListener = new ControllerPluggedListener();
		ControllerEnvironment.getDefaultEnvironment().addControllerListener(controllerPluggedListener);
		EventDispatcher.addHandler(this);
		mouseCallback = new MouseHandler();
		keyCallback = new KeyboardRawHandler();
		mouseButtonCallback = new MouseButtonHandler();
		glfwSetCursorPosCallback(MainThread.window, mouseCallback);
		glfwSetKeyCallback(MainThread.window, keyCallback);
		glfwSetMouseButtonCallback(MainThread.window, mouseButtonCallback);
		setupBasicControls();
	}

	public static int getMaxOptions() {
		// Assume cancel
		for (int i = 1; i < 10; i++) {
			String menu = "menu_" + i;
			ControlDescription cs = InputMain.getControlIcon(menu);
			if (cs == null) {
				return i - 1;
			}
		}
		return 9;
	}

	public static void saveCustomBindings() {
		HashMap<String, ArrayList<InputBinding>> savingbindings = new HashMap<String, ArrayList<InputBinding>>();
		// Fill a list with every single binding
		synchronized (InputMain.bindings) {
			for (ControlBinding cb : InputMain.bindings) {
				for (InputBinding ib : cb.getList()) {
					if (!savingbindings.containsKey(cb.getControlName())) {
						savingbindings.put(cb.getControlName(), new ArrayList<InputBinding>());
					}
					savingbindings.get(cb.getControlName()).add(ib);
				}
			}
		}

		HashMap<String, ArrayList<InputBinding>> defaultBindings = loadDefaultBindings();

		for (String key : defaultBindings.keySet()) {
			if (savingbindings.containsKey(key)) {
				for (InputBinding ib : defaultBindings.get(key)) {
					if (savingbindings.get(key).contains(ib)) {
						ArrayList<InputBinding> list = savingbindings.get(key);
						System.out.println("Removing default binding from customs " + key + " " + ib.toString());
						System.out.println(list.size());
						while (list.remove(ib)) { // Not sure why there'd be multiple, but whatever :)
						}
						System.out.println(list.size());
						if (savingbindings.get(key).size() == 0) {
							savingbindings.remove(key);
						}
					} else {
						// If we got here, it is in defaults but not in current.
						// Put in a "remove this binding" special key
						if (savingbindings.get("-" + key) == null) {
							savingbindings.put("-" + key, new ArrayList<InputBinding>());
						}
						savingbindings.get("-" + key).add(ib);
					}
				}
			} else {
				for (InputBinding ib : defaultBindings.get(key)) {
					// Removed all bindings for this option. Not likely to happen. but just in case
					if (savingbindings.get("-" + key) == null) {
						savingbindings.put("-" + key, new ArrayList<InputBinding>());
					}
					savingbindings.get("-" + key).add(ib);
					continue;
				}
			}
		}

		File f = new File(MainThread.cache, "input/custom.bind");
		try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
			for (String s : savingbindings.keySet()) {
				for (InputBinding ib : savingbindings.get(s)) {
					StringBuilder sb = new StringBuilder();
					boolean bother = false;
					sb.append(s);
					if (ib instanceof KeyBinding) {
						KeyBinding kb = (KeyBinding) ib;
						sb.append(" key ").append(kb.getKey());
						bother = true;
					} else if (ib instanceof PadBinding) {
						PadBinding pb = (PadBinding) ib;
						sb.append(" pad ").append(pb.getPadName()).append(" ").append(pb.getIndex());
						bother = true;
					} else if (ib instanceof MouseBtnBinding) {
						MouseBtnBinding mbb = (MouseBtnBinding) ib;
						sb.append(" msb ").append(mbb.getButtonIndex());
						bother = true;
					}
					System.out.println("Adding to Customs " + s + " " + ib.toString());

					sb.append("\n");
					if (bother) {
						out.write(sb.toString());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static HashMap<String, ArrayList<InputBinding>> loadDefaultBindings() {
		HashMap<String, ArrayList<InputBinding>> loadingbindings = new HashMap<String, ArrayList<InputBinding>>();
		File dir = new File(MainThread.cache, "input");
		File[] listing = dir.listFiles();
		if (listing != null) {
			for (File file : listing) {
				String fn = file.getName().toLowerCase();
				if (fn.startsWith("default") && fn.endsWith(".bind")) {
					loadBindings(file, loadingbindings);
				}
			}
		}
		return loadingbindings;
	}

	public static HashMap<String, ArrayList<InputBinding>> loadBindings(File file, HashMap<String, ArrayList<InputBinding>> list) {
		if (list == null) {
			list = new HashMap<String, ArrayList<InputBinding>>();
		}
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = in.readLine()) != null) {
				String[] split = line.split(" ", 2);
				if (split.length == 2) {
					String key = split[0];
					InputBinding ib = makeBinding(split[1]);
					if (ib != null) {
						if (!list.containsKey(key)) {
							list.put(key, new ArrayList<InputBinding>());
						}
						list.get(key).add(ib);
					}
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("No input binding file '" + file.getPath() + "'");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private static InputBinding makeBinding(String string) {
		InputBinding ib = null;
		String[] split = string.split(" ");
		if (split[0].equalsIgnoreCase("key") && split.length == 2) {
			String key = split[1];
			ib = new KeyBinding(key);
		} else if (split[0].equalsIgnoreCase("pad") && split.length == 3) {
			String pad = split[1];
			Integer number = Integer.parseInt(split[2]);
			ib = new PadBinding(pad, number);
		} else if (split[0].equalsIgnoreCase("msb") && split.length == 2) {
			Integer number = Integer.parseInt(split[1]);
			ib = new MouseBtnBinding(number);
		}
		return ib;
	}

	public static void setupBasicControls() {

		// Load all default bindings
		HashMap<String, ArrayList<InputBinding>> defaults = loadDefaultBindings();
		for (String s : defaults.keySet()) {
			ControlBinding cb = new ControlBinding(s);
			for (InputBinding ib : defaults.get(s)) {
				System.out.println(cb.getControlName() + " " + ib);
				cb.addInput(ib);
			}
			synchronized (bindings) {
				bindings.add(cb);
			}
		}

		// Load Custom bindings over the top. Be aware of negatives
		defaults = loadBindings(new File(MainThread.cache, "input/custom.bind"), null);
		synchronized (bindings) {
			for (String s : defaults.keySet()) {
				System.out.println(s + " " + defaults.get(s));
				if (s.startsWith("-")) {
					s = s.substring(1); // Remove the minus
					for (ControlBinding cb : bindings) {
						if (cb.getControlName().equalsIgnoreCase(s)) {
							cb.removeInput(defaults.get("-" + s));
						}
					}
				} else {
					ControlBinding cb = getControlBinding(s);
					cb.addInput(defaults.get(s));
				}
			}
		}
	}

	public void doEet(BaseState state, float delta) {
		Util.checkErr();
		if (state.screen != lastScreen) {
			lastScreen = MainThread.getGameState().screen;
			lastMouseOverElement = null;
			lastFocused = null;
		}
		if (lastFocused != null && state.screen != null) {

		}
		Picking.pickRender(state);
		if (CONTROL_WITH_GPAD) {
			Controller[] list = ControllerEnvironment.getDefaultEnvironment().getControllers();
			for (int coun = list.length - 1; coun > -1; coun--) {
				Controller c = list[coun];
				if (!c.poll()) {
					c = null;
					cl = null;
					CONTROL_WITH_GPAD = false;
					CONTROL_WITH_MKB = true;
					// Currently linux does not ever get an updated list of
					// controllers or connection/disconnection messages.
					// Until one or the other works we just assume to revert
					// back to MKB
					break;
				}
				if (InputMain.c == null || c == InputMain.c) {

					Component[] comp = c.getComponents();
					for (int count = 0; count < comp.length; count++) {
						JoystickRawAxisEvent event = new JoystickRawAxisEvent(c, count, comp[count].getPollData(), delta, comp[count].getIdentifier());
						EventDispatcher.dispatchEvent(event);
					}
				}
			}
			PickingResult pr = Picking.pick(MainThread.lastW / 2, MainThread.lastH / 2);
			if (pr.picked instanceof UIElement) {
				// No idea what should be done here.
				// Realistically we should have one RenderView marked as "primary" and do 1/2 W & H of that, but it's so much extra work!
			} else {
				if (pr.worldLoc != null) {
					EventDispatcher.dispatchEvent(new MouseRenderableHoverEvent(pr.picked, pr.worldLoc));
				}
			}
		}
		Util.checkErr();
		if (CONTROL_WITH_MKB) {
			Util.checkErr();
			/*
			 * while (Keyboard.next()) {
			 * Util.checkErr();
			 * Integer keycode = Keyboard.getEventKey();
			 * Character k = Keyboard.getEventCharacter();
			 * boolean s = Keyboard.getEventKeyState();
			 * Util.checkErr();
			 * System.out.println(Keyboard.getKeyName(keycode) + " " + s);
			 * KeyboardRawPressEvent event = new KeyboardRawPressEvent(lastFocused, Keyboard.getKeyName(keycode), s, keycode, k);
			 * EventDispatcher.dispatchEvent(event);
			 * }
			 */
			/*
			 * while (Mouse.next()) {
			 * int x = Mouse.getEventX(), y = Mouse.getEventY();
			 * int button = Mouse.getEventButton();
			 * boolean s = Mouse.getEventButtonState();
			 * int wheel = Mouse.getEventDWheel();
			 * pr = Picking.pick(x, y);
			 * if (pr.picked instanceof UIElement) {
			 * UIElement ele = (UIElement) pr.picked;
			 * if (button == 0) {
			 * UIElementFocusEvent eventFocus = new UIElementFocusEvent(ele);
			 * EventDispatcher.dispatchEvent(eventFocus);
			 * }
			 * } else {
			 * 
			 * }
			 * if (button >= 0) {
			 * System.out.println(button + ":" + s);
			 * if (s) {
			 * if (!buttonsDown[button]) {
			 * buttonsDown[button] = true;
			 * }
			 * } else {
			 * if (pr.picked != null) {
			 * MouseButtonRenderableEvent event;
			 * if (pr.picked instanceof UIElement) {
			 * int rx = x - ((UIElement) pr.picked).getParentX();
			 * int ry = (HGMainThread.lastH - y) - ((UIElement) pr.picked).getParentY();
			 * event = new MouseButtonRenderableEvent(pr.picked, pr.worldLoc, button, rx, ry, x, HGMainThread.lastH - y);
			 * } else {
			 * int rx = x - pr.picked.getContext().totalx;
			 * int ry = (HGMainThread.lastH - y) - pr.picked.getContext().totaly;
			 * event = new MouseButtonRenderableEvent(pr.picked, pr.worldLoc, button, rx, ry, x, HGMainThread.lastH - y);
			 * }
			 * EventDispatcher.dispatchEvent(event);
			 * }
			 * 
			 * if (buttonsDown[button]) {
			 * buttonsDown[button] = false;
			 * }
			 * }
			 * }
			 * if (wheel != 0) {
			 * if (pr.picked != null) {
			 * MouseWheelRenderableEvent event = new MouseWheelRenderableEvent(pr.picked, pr.worldLoc, wheel);
			 * EventDispatcher.dispatchEvent(event);
			 * }
			 * }
			 * if (pr.worldLoc == null) {
			 * lastLoc = null;
			 * } else {
			 * lastLoc = new Location(pr.worldLoc);
			 * }
			 * 
			 * }
			 */
			PickingResult pr = Picking.pick((int) mouseCallback.x, (int) mouseCallback.y);
			keyCallback.update(delta);
			mouseButtonCallback.update(delta);

			Util.checkErr();
			// We have a resting position. Now to check out our focus/mouse over
			if (pr.picked instanceof UIElement) {
				UIElement ele = (UIElement) pr.picked;
				if (ele.isFocusable() && !ele.equals(lastMouseOverElement)) {
					if (lastMouseOverElement != null) {
						EventDispatcher.dispatchEvent(new UIElementMouseOverEvent(lastMouseOverElement, false));

					}

					EventDispatcher.dispatchEvent(new UIElementMouseOverEvent(ele, true));
					lastMouseOverElement = ele;
				}
			} else {
				if (!CONTROL_WITH_GPAD) {
					if (pr.worldLoc != null) {
						for (int i = 0; i < 12; i++) {
							// if (buttonsDown[i]) {
							// EventDispatcher.dispatchEvent(new MouseRenderableDragEvent(pr.picked, pr.worldLoc, i));
							// }
						}
						EventDispatcher.dispatchEvent(new MouseRenderableHoverEvent(pr.picked, pr.worldLoc));

					}
				}
			}
			Util.checkErr();
		}
		Picking.pickCleanup();
		Util.checkErr();
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onFocus(MouseRenderableHoverEvent event) {
		// Cache for later usage
		lastHover = event;
	}

	public MouseRenderableHoverEvent getLastHovered() {
		return lastHover;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onMouseButtonRenderable(MouseButtonRenderableEvent event) {
		if (!event.isConsumed()) {
			// Check to see if it's a disembodied click.
			MouseButtonEvent event2 = new MouseButtonEvent(event.getButton());
			EventDispatcher.dispatchEvent(event2);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onMouseWheelRenderable(MouseWheelRenderableEvent event) {
		if (!event.isConsumed()) {
			MouseWheelEvent event2 = new MouseWheelEvent(event.getDelta());
			EventDispatcher.dispatchEvent(event2);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onUIElementFocus(UIElementFocusEvent event) {
		lastFocused = event.getElement();
	}

	/*
	 * Input Section for keeping track of key/pad bindings
	 */
	public static ControlBinding getControlBinding(String string) {
		synchronized (bindings) {
			for (ControlBinding cb : bindings) {
				if (cb.getControlName().equals(string)) {
					return cb;
				}
			}
			ControlBinding cb = new ControlBinding(string);
			bindings.add(cb);
			return cb;
		}
	}

	public static ControlBinding getControlBindingFor(InputBinding ib) {
		synchronized (bindings) {
			for (ControlBinding cb : bindings) {
				if (cb.hasInput(ib)) {
					return cb;
				}
			}
		}
		return null;
	}

	/*
	 * Now the Input section which turns raw events into InputCapturedEvent(s)
	 */
	/**
	 * Key state change -> InputChange via keybindings
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onKeyPress(KeyboardRawPressEvent event) {
		KeyBinding kb = new KeyBinding(event.getKey());
		ControlBinding cb = getControlBindingFor(kb);
		if (cb != null) {
			EventDispatcher.dispatchEvent(new InputChangeEvent(cb, kb, event.getState(), event.getState() ? 1f : 0f));
		}
	}

	/**
	 * Held keys -> InputHeld via keybindings
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onKeyHold(KeyboardRawHeldEvent event) {
		if (event.isConsumed()) {
			return;
		}
		KeyBinding kb = new KeyBinding(event.getKey());
		ControlBinding cb = getControlBindingFor(kb);
		if (cb != null) {
			EventDispatcher.dispatchEvent(new InputHeldEvent(cb, kb, event.getDelta(), event.getMagnitude()));
		}
	}

	/**
	 * Pad state & held -> InputChange and InputHeld via keybindings
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onKeyHold(JoystickRawAxisEvent event) {
		if (event.isConsumed()) {
			return;
		}
		if (cl == null) {
			return;
		}
		// Ensure space in list of last values
		while (event.getAxisIndex() >= controlLastValue.size()) {
			controlLastValue.add(0f);
		}
		// Check if it's a trigger. Differs from otgher axis
		ControllerAxisTrigger trig = cl.getTrigger(event.getAxisIndex());
		// First send of raw press/release events
		if (!isPressed(controlLastValue.get(event.getAxisIndex()), trig != null) && isPressed(event.getValue(), trig != null)) {
			// Gone from off to on
			EventDispatcher.dispatchEvent(new JoystickRawChangeEvent(cl.getName(), event.getAxisIndex(), true, event.getValue()));
		} else if (isPressed(controlLastValue.get(event.getAxisIndex()), trig != null) && !isPressed(event.getValue(), trig != null)) {
			// Gone from on to off
			EventDispatcher.dispatchEvent(new JoystickRawChangeEvent(cl.getName(), event.getAxisIndex(), false, event.getValue()));
		}
		controlLastValue.set(event.getAxisIndex(), event.getValue());

		// Now try to bind to control
		PadBinding pb = new PadBinding(cl.getName(), event.getAxisIndex());
		ControlBinding cb = getControlBindingFor(pb);
		if (cb == null) {
			return;
		}

		// Send events for push/release. Technically allows axis too, but may be
		// nonsensical.
		event.setConsumed();
		ControllerAxisPairs hat = cl.getPair(event.getAxisIndex());
		if (hat != null) {
			EventDispatcher.dispatchEvent(new InputHeldEvent(cb, pb, event.getDelta(), hat.getValueWithDeadZone(event.getPad(), event.getAxisIndex(), 0.2f)));
		} else {
			EventDispatcher.dispatchEvent(new InputHeldEvent(cb, pb, event.getDelta(), event.getValue()));
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onJoyStickPress(JoystickRawChangeEvent event) {
		if (event.isConsumed() || cl == null) {
			return;
		}
		PadBinding pb = new PadBinding(cl.getName(), event.getAxis());
		ControlBinding cb = getControlBindingFor(pb);
		if (cb == null) {
			return;
		}
		event.setConsumed();
		EventDispatcher.dispatchEvent(new InputChangeEvent(cb, pb, event.getState(), event.getValue()));
	}

	public static boolean isPressed(float value, boolean isAxis) {
		if ((value <= -0.6f && !isAxis) || value >= 0.6f) {
			return true;
		}
		return false;
	}

	public static ControlDescription getControlIcon(String string) {
		ControlBinding cb = InputMain.getControlBinding(string);
		if (cb != null) {
			if (CONTROL_WITH_GPAD && cl != null) {
				PadBinding pb = cb.getInputController(cl.getName());
				if (pb != null) {
					return cl.getComponent(pb.getIndex());
				}
			} else if (CONTROL_WITH_MKB) {
				KeyBinding kb = cb.getInputKeyboard();
				return new KeyDescription(kb.getKey());
			}
		}
		return null;
	}

	public static ControlDescription getControlIcon(InputBinding ib) {
		if (ib instanceof PadBinding && CONTROL_WITH_GPAD && cl != null) {
			PadBinding pb = (PadBinding) ib;
			if (pb.getPadName().equalsIgnoreCase(cl.getName())) { // The one requested is the selected controller layout. Woop
				return cl.getComponent(pb.getIndex());
			}
		} else if (ib instanceof KeyBinding && CONTROL_WITH_MKB) {
			return new KeyDescription(((KeyBinding) ib).getKey());
		}

		return null;
	}

	public static MouseHandler getMouse() {
		return instance.mouseCallback;
	}
}
