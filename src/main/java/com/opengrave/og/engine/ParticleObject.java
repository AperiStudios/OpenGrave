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
package com.opengrave.og.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.base.RenderableParticles;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class ParticleObject extends BaseObject implements Pickable {

	ArrayList<ParticlePart> particles, sortedParticles;
	private float timeSinceLastSpawn = 0f;
	private int lastSpawned = 0;

	private float spawnRate = 200f;
	private boolean respawn = true;
	private Vector3f gravity = new Vector3f(0f, 0f, -.1f);
	BoundingBox box = new BoundingBox();

	Random rand = new Random();
	private Vector3f minimum = new Vector3f(0f, 0f, 0f), minimumDir = new Vector3f(0f, 0f, 1f), minimumCol = new Vector3f(0f, 0f, 0f);
	private Vector3f maximum = new Vector3f(0f, 0f, 0f), maximumDir = new Vector3f(0f, 0f, 1f), maximumCol = new Vector3f(1f, 1f, 1f);

	public ParticleObject(CommonObject cobj) {
		super(cobj);
		renderable = new RenderableParticles();
		particles = new ArrayList<ParticlePart>();
		setParticleCount(100);
	}

	public void setParticleCount(int particleCount) {
		synchronized (particles) {
			while (particles.size() < particleCount) {
				particles.add(new ParticlePart());
			}
			while (particles.size() > particleCount) {
				particles.remove(0);
			}
			System.out.println(particles.size());
		}
	}

	public int getParticleCount() {
		synchronized (particles) {
			return particles.size();
		}
	}

	public void setGravity(Vector3f gravity) {
		this.gravity = gravity;
	}

	public Vector3f getGravity() {
		return gravity;
	}

	public void setMaterialList(MaterialList matList) {
		this.matList = matList;
	}

	public MaterialList getMaterialList() {
		return matList;
	}

	public void setSpawnRate(float spawnRate) {
		this.spawnRate = spawnRate;
	}

	public float getSpawnRate() {
		return spawnRate;
	}

	public void setRespawn(boolean respawn) {
		this.respawn = respawn;
	}

	public boolean getRespawn() {
		return respawn;
	}

	@Override
	public void doRender(Matrix4f matrix) {
	}

	@Override
	public void doRenderForPicking(Matrix4f matrix) {
		synchronized (particles) {
			((RenderableParticles) renderable).setParticleData(sortedParticles);
			renderable.setContext(context);
			renderable.renderForPicking(matrix, this);
		}
	}

	@Override
	public void doRenderShadows(Matrix4f matrix, Shadow shadow) {
	}

	@Override
	public void renderableLabelChanged(String s) {
	}

	@Override
	public void doUpdate(float delta) {
		timeSinceLastSpawn += delta;
		synchronized (particles) {
			for (ParticlePart particle : particles) {
				particle.setNextDelta(delta);
			}
			if (spawnRate > 0f && particles.size() > 0) {
				while (timeSinceLastSpawn >= spawnRate) {
					lastSpawned++;
					if (lastSpawned >= particles.size()) {
						lastSpawned = 0;
					}
					ParticlePart particle = particles.get(lastSpawned);
					particle.spawn(getRandomLocation(), chooseRandomDirection(), gravity, chooseRandomColour());
					particle.setNextDelta(delta);
					timeSinceLastSpawn -= spawnRate;
					delta -= spawnRate;

				}
			}
			box.clear();
			Vector3f minusOne = new Vector3f(-1f, -1f, -1f), plusOne = new Vector3f(1f, 1f, 1f);
			for (ParticlePart particle : particles) {
				particle.update(0f);
				Vector3f pos = particle.getPosition().toVector3();
				box.addVector3f(pos.add(minusOne, null));
				box.addVector3f(pos.add(plusOne, null));
			}
		}
	}

	private Location getRandomLocation() {
		float xrange = maximum.x - minimum.x, yrange = maximum.y - minimum.y, zrange = maximum.z - minimum.z;
		Vector3f ran = new Vector3f(rand.nextFloat() * xrange + minimum.x, rand.nextFloat() * yrange + minimum.y, rand.nextFloat() * zrange + minimum.z);
		return getLocation().addTogether(ran);
	}

	private Vector4f chooseRandomColour() {
		float xrange = maximumCol.x - minimumCol.x, yrange = maximumCol.y - minimumCol.y, zrange = maximumCol.z - minimumCol.z;
		return new Vector4f(rand.nextFloat() * xrange + minimumCol.x, rand.nextFloat() * yrange + minimumCol.y, rand.nextFloat() * zrange + minimumCol.z, 0.5f);
		// TODO Alpha
	}

	private Vector3f chooseRandomDirection() {
		float xrange = maximumDir.x - minimumDir.x, yrange = maximumDir.y - minimumDir.y, zrange = maximumDir.z - minimumDir.z;
		return new Vector3f(rand.nextFloat() * xrange + minimumDir.x, rand.nextFloat() * yrange + minimumDir.y, rand.nextFloat() * zrange + minimumDir.z);
	}

	@Override
	public String getType() {
		return "particle";
	}

	@Override
	public void startAnimation(String name, float speed, boolean once) {
	}

	@Override
	public void stopAnimation(String name) {
	}

	@Override
	public void doRenderSemiTransparent(Matrix4f matrix) {
		synchronized (particles) {
			for (ParticlePart particle : particles) {
				particle.setDistanceFrom(context);
			}
			sort();
			renderable.setMaterialList(matList);
			((RenderableParticles) renderable).setParticleData(sortedParticles);
			renderable.setContext(context);
			renderable.render(matrix, style);
		}
	}

	private void sort() {
		if (sortedParticles == null || sortedParticles.size() != particles.size()) {
			sortedParticles = new ArrayList<ParticlePart>();
			sortedParticles.addAll(particles);
		}
		Collections.sort(sortedParticles, new ParticlesDepthSorter());
		((RenderableParticles) renderable).setParticleData(sortedParticles);

	}

	public Vector3f getMinimum() {
		return minimum;
	}

	public void setMinimum(Vector3f minimum) {
		this.minimum = minimum;
	}

	public Vector3f getMaximum() {
		return maximum;
	}

	public Vector3f getMinimumDir() {
		return minimumDir;
	}

	public Vector3f getMaximumDir() {
		return maximumDir;
	}

	public void setMaximum(Vector3f maximum) {
		this.maximum = maximum;
	}

	public void setMinimumDir(Vector3f minDir) {
		this.minimumDir = minDir;
	}

	public void setMaximumDir(Vector3f maxDir) {
		this.maximumDir = maxDir;
	}

	public void setMinimumCol(Vector3f minCol) {
		this.minimumCol = minCol;
	}

	public void setMaximumCol(Vector3f maxCol) {
		this.maximumCol = maxCol;
	}

	public Vector3f getMinimumCol() {
		return minimumCol;
	}

	public Vector3f getMaximumCol() {
		return maximumCol;
	}

	@Override
	public void save(OGOutputStream stream) {
		try {
			stream.writeString(getType());
			stream.writeString(this.getModelLabel());
			stream.writeVector3f(minimum);
			stream.writeVector3f(maximum);
			stream.writeVector3f(minimumDir);
			stream.writeVector3f(maximumDir);
			stream.writeVector3f(minimumCol);
			stream.writeVector3f(maximumCol);
			stream.writeInt(getParticleCount());
			stream.writeLocation(location);
			stream.writeMaterialList(matList);

		} catch (IOException e) {
			new DebugExceptionHandler(e);

		}

	}

	@Override
	public RenderView getContext() {
		return context;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return box;
	}
}
