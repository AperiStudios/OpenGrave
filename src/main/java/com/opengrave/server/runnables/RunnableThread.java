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
package com.opengrave.server.runnables;

import java.util.ArrayList;

import com.opengrave.server.Server;

public class RunnableThread implements Runnable {
	ArrayList<Runnable> runList = new ArrayList<Runnable>();
	private Server s;
	private Thread t;

	public void addNewRunnable(Runnable r) {
		synchronized (runList) {
			runList.add(r);
			runList.notifyAll();
		}
	}

	public RunnableThread(Server s) {
		this.s = s;
		t = new Thread(this, "Server Runnables");
		t.start();
	}

	@Override
	public void run() {
		while (s.running) {
			synchronized (runList) {
				try {
					runList.wait();
				} catch (InterruptedException e) {
					if (!s.running) {
						return;
					}
				}
			}
			int len = -1;
			synchronized (runList) {
				len = runList.size();
			}
			while (len > 0) {
				Runnable r = null;
				synchronized (runList) {
					r = runList.remove(0);
				}
				if (r != null) {
					r.run();
				}
				synchronized (runList) {
					len = runList.size();
				}
			}
		}
	}

	public void interupt() {
		t.interrupt();
	}
}
