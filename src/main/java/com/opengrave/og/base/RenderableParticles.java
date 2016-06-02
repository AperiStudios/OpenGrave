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
package com.opengrave.og.base;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.engine.ParticlePart;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class RenderableParticles extends Renderable3D {

	private ArrayList<ParticlePart> particleList;
	int idVao, idVboPositions, idVboColours, idVboScale, size;

	FloatBuffer positions;
	FloatBuffer colours;
	FloatBuffer scales;

	@Override
	public void render(Matrix4f matrix, RenderStyle style) {
		dealWithChange();
		if (matList == null || !matList.valid()) {
			return;
		}
		GL30.glBindVertexArray(idVao);
		int pID = Resources.loadShader("particle.vs", "particle.fs").getProgram();
		GL20.glUseProgram(pID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glBindAttribLocation(pID, 0, "in_Position");
		GL20.glBindAttribLocation(pID, 1, "in_Colour");
		GL20.glBindAttribLocation(pID, 2, "in_Scale");
		Util.checkErr();
		int texture = GL20.glGetUniformLocation(pID, "tex");
		GL20.glUniform1i(texture, 0);
		int wSS = GL20.glGetUniformLocation(pID, "windowSizeScale");

		GL20.glUniform1f(wSS, getContext().width / 1024f);
		getContext().setMatrices(pID, matrix);
		if (matList != null && matList.valid()) {
			matList.bind(pID, GL13.GL_TEXTURE0);
		}
		GL11.glEnable(GL20.GL_POINT_SPRITE);
		GL11.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
		GL11.glDepthMask(false);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, size);
		GL11.glDepthMask(true);
		GL11.glDisable(GL20.GL_POINT_SPRITE);
		GL11.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);

		if (matList != null && matList.valid()) {
			matList.unbind();
		}
		Util.checkErr();

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);

		Util.checkErr();

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		Util.checkErr();
	}

	@Override
	public void renderForPicking(Matrix4f matrix, Pickable object) {
		dealWithChange();
		GL30.glBindVertexArray(idVao);
		int pID = Resources.loadShader("particle.vs", "pickingmodel.fs").getProgram();
		GL20.glUseProgram(pID);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(2);
		GL20.glBindAttribLocation(pID, 0, "in_Position");
		GL20.glBindAttribLocation(pID, 2, "in_Scale");
		Util.checkErr();

		int wSS = GL20.glGetUniformLocation(pID, "windowSizeScale");

		GL20.glUniform1f(wSS, MainThread.lastW / 1024f);
		getContext().setMatrices(pID, matrix);
		GL11.glEnable(GL20.GL_POINT_SPRITE);
		GL11.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
		Picking.registerObject(pID, getContext(), object);
		GL11.glDrawArrays(GL11.GL_POINTS, 0, size);
		GL11.glDisable(GL20.GL_POINT_SPRITE);
		GL11.glDisable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);

		Util.checkErr();

		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(2);

		Util.checkErr();

		GL20.glUseProgram(0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		Util.checkErr();
	}

	@Override
	public void renderShadows(Matrix4f matrix, Shadow shadow) {
	}

	@Override
	public void dealWithChange() {
		// Safely assume every frame is a new frame in terms of particle
		// positions.
		if (idVao == 0) {
			idVao = GL30.glGenVertexArrays();
		}
		GL30.glBindVertexArray(idVao);
		if (idVboColours == 0) {
			idVboColours = GL15.glGenBuffers();
		}
		if (idVboPositions == 0) {
			idVboPositions = GL15.glGenBuffers();
		}
		if (idVboScale == 0) {
			idVboScale = GL15.glGenBuffers();
		}

		size = particleList.size() * 4;
		if (positions == null || positions.capacity() != size) {
			positions = BufferUtils.createFloatBuffer(size);
		}
		if (colours == null || colours.capacity() != size) {
			colours = BufferUtils.createFloatBuffer(size);
		}
		if (scales == null || scales.capacity() != size) {
			scales = BufferUtils.createFloatBuffer(size);
		}
		positions.rewind();
		colours.rewind();
		scales.rewind();
		for (ParticlePart particle : particleList) {
			Vector3f pos = particle.getPosition().toVector3(), sca = particle.getScaleData();
			Vector4f col = particle.getColour();

			positions.put(pos.x).put(pos.y).put(pos.z).put(1f);
			colours.put(col.x).put(col.y).put(col.z).put(col.w);
			scales.put(sca.x).put(sca.y).put(sca.z).put(1f);

		}
		positions.flip();
		colours.flip();
		scales.flip();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, idVboPositions);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positions, GL15.GL_STREAM_DRAW);
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, idVboColours);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colours, GL15.GL_STREAM_DRAW);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, idVboScale);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, scales, GL15.GL_STREAM_DRAW);
		GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, 0, 0);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);

	}

	@Override
	public void recreate() {
	}

	@Override
	public void addVertex(VertexData vd) {
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void delete() {
	}

	public void setParticleData(ArrayList<ParticlePart> parts) {
		this.particleList = parts;
	}

}
