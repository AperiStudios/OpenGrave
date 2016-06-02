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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import com.opengrave.og.MainThread;

public class DebugWindowThread implements Runnable {

	public enum DebugType {
		OUT, ERROR
	}

	private BufferedReader reader;
	private DebugType type;
	private Thread t;
	private PrintStream oldOut;

	public DebugWindowThread(BufferedReader reader, PrintStream oldOut, DebugType type) {
		this.reader = reader;
		this.type = type;
		this.oldOut = oldOut;
		t = new Thread(this, "Debug Window Thread - " + type.toString());
		t.start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				String line = reader.readLine();
				oldOut.println(line);
				synchronized (MainThread.debugLock) {
					MainThread.addDebugLine(line, type);
				}
			} catch (IOException e) {
				// The last thread to write to system.out or system.err has ended, and the pipe has noticed, and believes that means no more to read. Ignore it and await more from another thread
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

}
