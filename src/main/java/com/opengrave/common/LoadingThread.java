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

import java.util.ArrayList;

import com.opengrave.og.MainThread;

public class LoadingThread implements Runnable {
	Thread t;
	Object lock = new Object();
	ArrayList<ThreadedLoading> loadable = new ArrayList<ThreadedLoading>();
	static LoadingThread inst;

	public LoadingThread() {
		t = new Thread(this, "Loading Thread");
		t.start();
		inst = this;
	}

	public static void addLoadable(ThreadedLoading t) {
		synchronized (inst.loadable) {
			inst.loadable.add(t);
		}
		synchronized (inst.lock) {
			inst.lock.notifyAll();
		}
	}

	@Override
	public void run() {
		while (MainThread.running) {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					new DebugExceptionHandler(e);
				}
			}
			int size = 0;
			ThreadedLoading l = null; // I like being thread safe but all this could be 2/3 lines if I didn't have to care :P
			synchronized (loadable) {
				size = loadable.size();
			}
			while (size > 0) {
				synchronized (loadable) {
					if (loadable.size() > 0) {
						l = loadable.remove(0);
					}
				}
				if (l != null && !l.isLoaded()) {
					l.loadInThisThread();
				}
				synchronized (loadable) {
					size = loadable.size();
				}
			}
		}
	}

}
