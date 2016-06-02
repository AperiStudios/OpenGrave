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

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.opengrave.common.DebugExceptionHandler;

public class JavaHandler implements Handler {

	private Class<? extends Event> type;
	private WeakReference<EventListener> instanceRef;
	private Method method;

	public JavaHandler(Class<? extends Event> type, EventListener instance, Method method) {
		this.type = type;
		this.method = method;
		this.instanceRef = new WeakReference<EventListener>(instance);
	}

	@Override
	public boolean call(Event event) {
		EventListener instance = instanceRef.get();
		if (instance == null) {
			return false;
		}
		try {
			method.invoke(instanceRef, event);
		} catch (IllegalAccessException e) {
			new DebugExceptionHandler(e, method.getName(), method.getClass());
		} catch (IllegalArgumentException e) {
			new DebugExceptionHandler(e, method.getName(), method.getClass());
		} catch (InvocationTargetException e) {
			new DebugExceptionHandler(e, method.getName(), method.getClass());
		}
		return true;
	}

	@Override
	public boolean handler(Class<? extends Event> type) {
		return this.type.equals(type);
	}

}
