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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import javax.script.*;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.script.LuaScriptEngineFactory;

import com.opengrave.api.hgcore;
import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.ModSession;

/**
 * Runs as a thread to dispatch events to handlers. Handlers must register on
 * construction but need never notify of their destruction or garbage
 * collection. Uses WeakReference to hold onto handlers and dead ones are
 * dropped. Doubles as Mod handler, any mod loading, calling etc must be done
 * from this class via Events. No events handling functions should attempt to
 * use OpenGL commands as this will simply not work (This thread does not hold
 * an OpenGL context)
 * 
 * @author triggerhapp
 * 
 */
public class EventDispatcher implements Runnable, EventListener {
	private static Object lock = new Object();

	private ArrayList<WeakReference<EventListener>> javaListeners = new ArrayList<WeakReference<EventListener>>();
	private ArrayList<Mod> modsLoaded = new ArrayList<Mod>();
	private HashMap<String, HashMap<LUAEventPriority, ArrayList<LuaValue>>> luaListeners = new HashMap<String, HashMap<LUAEventPriority, ArrayList<LuaValue>>>();
	private HashMap<Class<? extends EventListener>, HashMap<JavaEventPriority, ArrayList<Method>>> classInfo = new HashMap<Class<? extends EventListener>, HashMap<JavaEventPriority, ArrayList<Method>>>();
	public static EventDispatcher events = new EventDispatcher();

	public static ModSession loadingSession;
	private ArrayList<Event> eventsRemaining = new ArrayList<Event>();
	private boolean running;
	Thread t;
	ScriptEngine engine;

	private SimpleBindings sb;

	private hgcore hgcore1;

	/**
	 * Adds an event handler.
	 */
	public static void addHandler(EventListener handler) {
		synchronized (events.classInfo) {
			events.prepareClass(handler.getClass());
		}
		synchronized (events.javaListeners) {
			events.javaListeners.add(new WeakReference<EventListener>(handler));
		}
	}

	public static void removeHandler(EventListener state) {
		synchronized (events.javaListeners) {
			@SuppressWarnings("unused")
			int dud = 0;
			for (int i = 0; i < events.javaListeners.size(); dud++) {
				WeakReference<EventListener> handlerRef = events.javaListeners.get(i);
				EventListener handler = handlerRef.get();
				if (handler == null || handler == state) {
					events.javaListeners.remove(i);
					continue;
				}
				i++;
			}
		}
	}

	public static void addHandler(LuaValue modLib, String eventType, String priority, LuaValue handler) {
		// if (loadingMod == null) {
		// System.out.println("Cannot register LUA events outside of main!");
		// return;
		// }
		Mod thisMod = null;
		for (Mod mod : Mod.getAll()) {
			if (modLib.equals(mod.getLibrary())) {
				thisMod = mod;
			}
		}
		synchronized (events.luaListeners) {
			if (!events.luaListeners.containsKey(thisMod.getId())) {
				events.luaListeners.put(thisMod.getId(), new HashMap<LUAEventPriority, ArrayList<LuaValue>>());
			}
			LUAEventPriority lep = new LUAEventPriority(eventType, priority);
			if (!events.luaListeners.get(thisMod.getId()).containsKey(lep)) {
				events.luaListeners.get(thisMod.getId()).put(lep, new ArrayList<LuaValue>());
			}
			if (!events.luaListeners.get(thisMod.getId()).get(lep).contains(handler)) {
				events.luaListeners.get(thisMod.getId()).get(lep).add(handler);
			}
		}
	}

	/***
	 * Adds Methods and parameters to a hashmap inside of a hashmap. Not thread
	 * safe, be sure to synchronize this.classInfo first!
	 * 
	 * @param class1
	 */
	@SuppressWarnings("unchecked")
	private void prepareClass(Class<? extends EventListener> klass) {
		if (classInfo.containsKey(klass)) {
			return;
		}
		HashMap<JavaEventPriority, ArrayList<Method>> methodHash = new HashMap<JavaEventPriority, ArrayList<Method>>();
		classInfo.put(klass, methodHash);
		Method[] methods = klass.getMethods();
		for (Method method : methods) {
			Class<?>[] list = method.getParameterTypes();
			Annotation[] annotations = method.getDeclaredAnnotations();
			EventHandlerPriority priority = null;
			for (Annotation annotation : annotations) {
				if (annotation instanceof EventHandler) {
					priority = ((EventHandler) annotation).priority();
				}
			}
			if (priority == null) {
				continue;
			}
			if (list.length == 1) {
				if (Event.class.isAssignableFrom(list[0])) {
					Class<? extends Event> event = (Class<? extends Event>) list[0];
					JavaEventPriority jep = new JavaEventPriority(event, priority);
					if (methodHash.containsKey(jep)) {
						methodHash.get(jep).add(method);
					} else {
						ArrayList<Method> methodList = new ArrayList<Method>();
						methodList.add(method);
						methodHash.put(jep, methodList);
					}
				}
			}
		}
	}

	/**
	 * Dispatch an event to the registered handlers. If the event was thrown
	 * during handling an event, do it immediately
	 */
	public static void dispatchEvent(Event event) {
		if (Thread.currentThread() == events.t) {
			// We're being called from within another Event
			// Dispatch immediatly
			events.sendEvent(event);
		} else {
			synchronized (lock) {
				events.eventsRemaining.add(event);
				lock.notifyAll();
			}
		}
	}

	private void dispatchEventTo(Event event, EventListener handler, EventHandlerPriority priority) {
		JavaEventPriority jep = new JavaEventPriority(event.getClass(), priority);
		synchronized (this.classInfo) {
			if (!classInfo.get(handler.getClass()).containsKey(jep)) {
				// Silently skip classes which don't handle this event type
				return;
			}
			for (Method m : classInfo.get(handler.getClass()).get(jep)) {
				try {
					m.invoke(handler, event);
				} catch (IllegalAccessException e) {
					new DebugExceptionHandler(e, m.getName(), m.getClass());
				} catch (IllegalArgumentException e) {
					new DebugExceptionHandler(e, m.getName(), m.getClass());
				} catch (InvocationTargetException e) {
					new DebugExceptionHandler(e.getCause());
				}
			}
		}
	}

	@Override
	public void run() {
		running = true;
		sb = new SimpleBindings();
		hgcore1 = new hgcore();
		hgcore1.bind(sb);
		addHandler(this);
		engine = new LuaScriptEngineFactory().getScriptEngine();
		if (engine == null) {
			System.out.println("LUA Engine null");
			System.exit(1);
		}
		hgcore1.bind(engine);
		while (running) {
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e1) {
				new DebugExceptionHandler(e1);
			}
			// Oddly formed while allows us to accept changes and not lock
			// threads while working on each separate event
			int size = 0;
			synchronized (lock) {
				size = eventsRemaining.size();
			}
			while (size > 0) {
				Event e = null;
				synchronized (lock) {
					e = eventsRemaining.remove(0);
				}
				if (e == null) {
					continue;
				}
				sendEvent(e);
				synchronized (lock) {
					size = eventsRemaining.size();
				}
			}
		}
	}

	private void sendEvent(Event e) {
		// DebugBenchmark time = new DebugBenchmark("Event " +
		// e.getEventName());
		sendLuaEvent(e, "first");
		if (e instanceof ConsumableEvent && ((ConsumableEvent) e).isConsumed()) {
			// time.done();
			return;
		}
		sendJavaEvent(e, EventHandlerPriority.EARLY);
		if (e instanceof ConsumableEvent && ((ConsumableEvent) e).isConsumed()) {
			// time.done();
			return;
		}
		sendLuaEvent(e, "third");
		if (e instanceof ConsumableEvent && ((ConsumableEvent) e).isConsumed()) {
			// time.done();
			return;
		}
		sendJavaEvent(e, EventHandlerPriority.LATE);
		if (e instanceof ConsumableEvent && ((ConsumableEvent) e).isConsumed()) {
			// time.done();
			return;
		}
		sendLuaEvent(e, "fifth");
		// time.done();
	}

	private void sendLuaEvent(Event e, String priority) {
		if (e.getEventName() == null || e.getEventName().equals("")) {
			System.out.println(e.getClass() + " has no event name");
			return;
		}
		LUAEventPriority lep = new LUAEventPriority(e.getEventName(), priority);
		synchronized (e) {
			for (String key : luaListeners.keySet()) {
				HashMap<LUAEventPriority, ArrayList<LuaValue>> valueSets = luaListeners.get(key);
				Mod mod = Mod.getMod(key);
				if (!valueSets.containsKey(lep)) {
					continue;
				}
				ArrayList<LuaValue> list = valueSets.get(lep);
				for (LuaValue function : list) {
					try {
						EventDispatcher.loadingSession = mod.getSession();
						function.call(mod.getLibrary(), CoerceJavaToLua.coerce(e));

					} catch (LuaError err) {
						System.out.println(err.getMessage());
						err.printStackTrace();
					}
					EventDispatcher.loadingSession = null;
					if (e instanceof ConsumableEvent && ((ConsumableEvent) e).isConsumed()) {
						return;
					}
				}
			}
		}

	}

	private void sendJavaEvent(Event e, EventHandlerPriority priority) {
		synchronized (e) {
			@SuppressWarnings("unused")
			int dud = 0;
			synchronized (this.javaListeners) {
				for (int i = 0; i < javaListeners.size(); dud++) {
					WeakReference<EventListener> handlerRef = javaListeners.get(i);
					EventListener handler = handlerRef.get();
					if (handler != null) {
						dispatchEventTo(e, handler, priority);

					} else {
						javaListeners.remove(i);
						continue;
					}
					i++;
				}
				e.isDispatchCompleted = true;
			}
		}
	}

	public void beginEventThread() {
		t = new Thread(this, "Event Thread");
		t.start();
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onModLoad(ModLoadEvent event) {
		EventDispatcher.loadingSession = event.getModSession();
		event.getMod().setSession(event.getModSession());
		if (event.getMod() == null) {
			return;
		}
		System.out.println("Attempting to load mod " + event.getMod().getId());
		File main = event.getMod().getMainFile();
		if (main != null) {
			try (FileReader fr = new FileReader(main)) {
				CompiledScript script = ((Compilable) engine).compile(fr);
				script.eval(sb); // Put the Lua functions into the sb
									// environment

				LuaValue library = new LuaTable();
				event.getMod().setLibrary(library);

				LuaFunction mainFunc = (LuaFunction) sb.get("main");
				event.getMod().setLibrary(library);
				mainFunc.call(library);
				synchronized (modsLoaded) {
					modsLoaded.add(event.getMod());
				}
			} catch (FileNotFoundException e) {
				new DebugExceptionHandler(e, main.getAbsolutePath());
			} catch (ScriptException e) {
				new DebugExceptionHandler(e, main.getAbsolutePath());
			} catch (IOException e) {
				new DebugExceptionHandler(e, main.getAbsolutePath());
			}
		}
		EventDispatcher.loadingSession = null;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onModUnload(ModUnloadEvent event) {
		luaListeners.remove(event.getMod().getId());
		System.out.println("Unlinking mod " + event.getMod().getId());
	}

	@EventHandler(priority = EventHandlerPriority.EARLY)
	public void onModUnloadAll(ModUnloadAllEvent event) {
		synchronized (modsLoaded) {
			for (Mod mod : modsLoaded) {
				ModUnloadEvent event2 = new ModUnloadEvent(mod);
				dispatchEvent(event2);
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onLuaDebug(DebugWindowTextInputEvent event) {
		String command = event.getText();
		try {
			engine.eval(command, engine.getContext());
		} catch (LuaError e) {
			// System.err.println(e.getMessage());
			new DebugExceptionHandler(e);

		} catch (ScriptException e) {
			// System.err.println(e.getMessage());
			new DebugExceptionHandler(e);
		}
	}

	/**
	 * Return a copy of the list of loaded mods. Alterations to the list are
	 * ignored, but alterations to individual mods will persist
	 * 
	 * @return
	 */
	public static ArrayList<Mod> getModsLoaded() {
		ArrayList<Mod> modCopy = new ArrayList<Mod>();
		synchronized (events.modsLoaded) {
			for (Mod mod : events.modsLoaded) {
				modCopy.add(mod);
			}
		}
		return modCopy;
	}

}
