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
package com.opengrave.common.config;

import java.io.*;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;

public class BinarySave {

	private BinaryParent root;
	private File file;

	public BinarySave(OGInputStream input) {
		try {
			root = new BinaryParent(input);
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		} catch (BinaryObjectWrongType e) {
			new DebugExceptionHandler(e);

		}
	}

	public BinarySave(File fileToOpen) {
		this.file = fileToOpen;
		if (fileToOpen != null && fileToOpen.exists() && fileToOpen.isFile()) {
			try (OGInputStream in = new OGInputStream(new FileInputStream(fileToOpen))) {
				root = new BinaryParent(in);
			} catch (FileNotFoundException e) {
				new DebugExceptionHandler(e);
			} catch (IOException e) {
				new DebugExceptionHandler(e);
			} catch (BinaryObjectWrongType e) {
				new DebugExceptionHandler(e);

			}

		} else {
			if (!fileToOpen.isFile()) {
				this.file = null;
			}
			root = new BinaryParent();
		}
	}

	public BinaryParent getRootNode() {
		return root;
	}

	public void save() {
		if (file != null) {
			if (!file.exists() || file.isFile()) {
				// Either a file or non-existant, we can write it
				try (OGOutputStream out = new OGOutputStream(new FileOutputStream(file))) {
					root.save(out);
				} catch (FileNotFoundException e) {
					new DebugExceptionHandler(e);
				} catch (IOException e) {
					new DebugExceptionHandler(e);
				} catch (BinaryNodeException e) {
					new DebugExceptionHandler(e);

				}
			}
		}
	}
}
