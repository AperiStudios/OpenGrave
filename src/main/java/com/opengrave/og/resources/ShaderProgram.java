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
package com.opengrave.og.resources;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.og.Util;

public class ShaderProgram {

	SourceFile sfv, sff;
	String label = "";
	int PID = -1, vert, frag;

	public static ShaderProgram makeShaderProgram(String vert, String frag) {
		ShaderProgram prog = new ShaderProgram();
		prog.sfv = SourceFile.loadFile("sdr/" + vert);
		prog.sff = SourceFile.loadFile("sdr/" + frag);
		prog.label = vert + ":" + frag;
		return prog;
	}

	public void compile() {
		Util.checkErr();

		PID = GL20.glCreateProgram();
		Util.checkErr();

		vert = loadShader(sfv.getSource(), GL20.GL_VERTEX_SHADER);
		Util.checkErr();

		frag = loadShader(sff.getSource(), GL20.GL_FRAGMENT_SHADER);
		Util.checkErr();

		GL20.glAttachShader(PID, vert);
		Util.checkErr();

		GL20.glAttachShader(PID, frag);
		Util.checkErr();

		GL20.glLinkProgram(PID);
		Util.checkErr();

		System.out.println(GL20.glGetProgramInfoLog(PID, 2000));
		Util.checkErr();

		GL20.glValidateProgram(PID);
		Util.checkErr();

		// System.out.println("Compiled " + label + " as number " + PID);
		if (GL20.glGetProgrami(PID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			new DebugExceptionHandler(new Exception(), label, GL20.glGetShaderInfoLog(PID, 2000), GL20.glGetProgramInfoLog(PID, 2000));
			// System.out.println("Failed to link " + label);
			// System.out.println(sfv.getSource());
			// System.out.println(sff.getSource());

			// Util.checkErr();

			// printShaderLogs();
		}
		Util.checkErr();

		GL20.glDetachShader(PID, vert);
		Util.checkErr();

		GL20.glDetachShader(PID, frag);
		GL20.glDeleteShader(vert);
		Util.checkErr();

		GL20.glDeleteShader(frag);
		Util.checkErr();
	}

	public static int loadShader(String source, int type) {
		Util.checkErr();
		int i = GL20.glCreateShader(type);
		Util.checkErr();
		GL20.glShaderSource(i, source);
		Util.checkErr();
		GL20.glCompileShader(i);
		Util.checkErr();
		if (GL20.glGetShaderi(i, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			new DebugExceptionHandler(new Exception(), annotate(source), GL20.glGetShaderInfoLog(i, 2000));
		}
		Util.checkErr();
		return i;
	}

	public int getProgram() {
		if (PID == -1) {
			compile();
		}
		if (PID == 0) {
			System.out.println("Program 0 returned - error");
		}
		return PID;
	}

	public boolean usesLighting() {
		if (sfv.usesLighting || sff.usesLighting) {
			return true;
		}
		return false;
	}

	public static String annotate(String code) {
		StringBuilder sb = new StringBuilder();
		int count = 1;
		for (String s : code.split("\n")) {
			sb.append(count).append(" : ").append(s).append("\n");
			count++;
		}
		return sb.toString();
	}
}
