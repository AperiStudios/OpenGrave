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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class SocketSender implements Runnable {
	private Object lock = new Object();
	Thread thread;
	private List<ByteArrayOutputStream> list;
	private boolean running = true;
	OutputStream output;
	Connector conn;

	public void stop() {
		running = false;
		thread.interrupt();
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SocketSender(Connector conn, OutputStream output) {
		this.conn = conn;
		list = new ArrayList<ByteArrayOutputStream>();
		// list = Collections.synchronizedList(new
		// Vector<ByteArrayOutputStream>());
		this.output = output;
		thread = new Thread(this, "Socket Sender Thread");
		thread.start();
	}

	@Override
	public void run() {
		try {
			while (running) {
				// Thread.sleep(100);
				synchronized (lock) {
					lock.wait();
				}
				synchronized (list) {
					for (ByteArrayOutputStream baos : list) {
						try {
							output.write(baos.toByteArray());
							output.flush();
							baos.close();
						} catch (SocketException e) {
							new DebugExceptionHandler(e);
							conn.setDestroy();
						} catch (IOException e) {
							new DebugExceptionHandler(e);
							conn.setDestroy();
						}
					}
					list.clear();
				}
			}
			output.close();
		} catch (InterruptedException e) {
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}

	}

	public void addSend(ByteArrayOutputStream out) {
		synchronized (list) {
			list.add(out);
		}
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void setName(String string) {
		thread.setName(string + " : Writer");
	}

}
