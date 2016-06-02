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

public abstract class Readyable {
	public boolean ready = false;
	public Object readyLock = new Object();

	/**
	 * Dangerous! Every intervalSize milliseconds it checks if this class is
	 * ready now, returning only when ready. waits intervalSize * max
	 * milliseconds at most and returns unready anyway otherwise
	 */
	public void waitUntilReady(int intervalSize, int max) {
		synchronized (readyLock) {
			if (ready) {
				return;
			}
			try {
				readyLock.wait();
			} catch (InterruptedException e) {
			}
		}
	}
}
