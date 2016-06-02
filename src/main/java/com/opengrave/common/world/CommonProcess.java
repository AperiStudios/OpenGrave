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
package com.opengrave.common.world;

import java.util.ArrayList;

public class CommonProcess {
	String processName;
	ArrayList<String> vars;

	public CommonProcess(String processName, ArrayList<String> vars) {
		this.processName = processName;
		this.vars = vars;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CommonProcess) {
			CommonProcess p = (CommonProcess) o;
			if (p.processName.equals(processName)) {
				return true;
			}
		}
		return false;
	}

	public String getProcessName() {
		return processName;
	}

	public ArrayList<String> getVariables() {
		return vars;
	}

}
