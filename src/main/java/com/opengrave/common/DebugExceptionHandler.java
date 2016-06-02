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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.swing.*;

import com.opengrave.common.xml.HGXMLThread;
import com.opengrave.og.MainThread;

/**
 * Each time a potentially fatal exception is thrown, a new instance of this
 * class should be created. As much information as possible should be crammed
 * into bug reports.
 * 
 * @author triggerhapp
 * 
 */
public class DebugExceptionHandler {

	// Static
	protected static ArrayList<DebugExceptionHandler> exceptionList = new ArrayList<DebugExceptionHandler>();

	// Object
	private String sTrace;
	private Object[] listOfDebuggableObjects;
	private Throwable exception;
	private JTextArea report;
	private JPanel panel;

	public DebugExceptionHandler(Throwable exception, Object... listOfDebuggableObjects) {
		this.listOfDebuggableObjects = listOfDebuggableObjects;
		this.exception = exception;
		synchronized (exceptionList) {
			exceptionList.add(this);
		}
		MainThread.addDebugException(this);
	}

	public Object[] getListOfDebuggableObjects() {
		return listOfDebuggableObjects;
	}

	public Throwable getException() {
		return exception;
	}

	public void sendReport() {
		String s = (String) JOptionPane.showInputDialog(MainThread.debugWindow, "Give a basic title for the report", "Automated bug report",
				JOptionPane.PLAIN_MESSAGE);
		if (s == null || s.length() < 10) {
			s = "Automated bug report";
		}
		HGXMLThread.requestBugReport(sTrace, s);
		remove();
	}

	public String getName() {
		return exception.toString();
	}

	public Component makeGUI() {
		if (panel != null) {
			return panel;
		}
		panel = new JPanel(new BorderLayout());
		JPanel lowerPanel = new JPanel();
		this.report = new JTextArea(40, 80);
		this.report.setEditable(false);
		this.report.setLineWrap(true);
		this.report.setWrapStyleWord(true);
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		sTrace = sw.toString();
		MainThread.oldErr.println(sTrace);
		this.report.append(sTrace + "\n");
		for (Object o : listOfDebuggableObjects) {
			sTrace += "\n" + o.toString() + "\n";
			this.report.append("\n" + o.toString() + "\n");
		}
		JButton ignore = new JButton("Ignore"), report = new JButton("Report");
		JScrollPane scroll = new JScrollPane(this.report);
		lowerPanel.add(ignore, BorderLayout.WEST);
		lowerPanel.add(report, BorderLayout.EAST);
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(lowerPanel, BorderLayout.SOUTH);

		ignore.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				remove();
			}
		});
		report.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendReport();
			}
		});
		return panel;
	}

	public void remove() {
		MainThread.removeDebugException(this);
		synchronized (exceptionList) {
			exceptionList.remove(this);
		}
	}

}
