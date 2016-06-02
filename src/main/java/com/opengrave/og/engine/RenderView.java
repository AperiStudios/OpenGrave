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

import java.util.ArrayList;
import java.util.Collections;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.opengrave.og.MainThread;
import com.opengrave.og.Util;
import com.opengrave.og.input.MouseRenderableHoverEvent;
import com.opengrave.og.light.Shadow;
import com.opengrave.og.light.Shadow2D;
import com.opengrave.og.light.ShadowCube;
import com.opengrave.og.util.Matrix3f;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class RenderView {

	private RootNode sceneNode;
	private Camera cam;

	private Matrix4f projectionMatrix, viewMatrix, shadowMatrix;
	public int totalx;
	public int totaly;
	public int width;
	public int height;
	private boolean shadows = false, hasshadows = false;
	Matrix4f ident = new Matrix4f();
	private Vector4f clearCol = new Vector4f(0f, 0f, 0f, 1f);
	private boolean clear = false;
	private ArrayList<ShadowCube> lightShadows = new ArrayList<ShadowCube>();

	public RenderView(RootNode node, Camera camera) {
		this.sceneNode = node;
		this.cam = camera;
	}

	public Node getSceneNode() {
		return sceneNode;
	}

	public void setSceneNode(RootNode sceneNode) {
		this.sceneNode = sceneNode;
	}

	public Camera getCam() {
		return cam;
	}

	public void setCam(FlyByCamera cam) {
		this.cam = cam;
	}

	public void clearAreaBeforeDraw(boolean b) {
		clear = b;
	}

	public void renderForPicking(int totalx, int totaly, int width, int height) {
		if (totalx < 0 || totaly < 0 || width < 1 || height < 1) {
			return;
		}
		this.totalx = totalx;
		this.totaly = totaly;
		this.width = width;
		this.height = height;
		if (sceneNode == null) {
			System.out.println("No scene node to render");
			return;
		}
		if (cam == null) {
			System.out.println("No camera to render from");
			return;
		}
		Util.checkErr();
		GL11.glViewport(totalx, totaly, width, height);
		Util.checkErr();
		prepare3DOpaque();
		Util.checkErr();
		sceneNode.renderForPicking(ident);
		Util.checkErr();
		revertSettings();
	}

	public void render(int totalx, int totaly, int width, int height) {
		this.totalx = totalx;
		this.totaly = totaly;
		this.width = width;
		this.height = height;
		if (sceneNode == null) {
			System.out.println("No scene node to render");
			return;
		}
		if (cam == null) {
			System.out.println("No camera to render from");
			return;
		}
		Shadow2D skyLight = sceneNode.getSkyLight();
		if (shadows && hasshadows) {
			// For each light source we map out a texture using a quick
			// render.
			prepare3DShadow();

			skyLight.getFramebuffer().bindDraw();
			// GL11.glEnable(GL32.GL_DEPTH_CLAMP); // TODO Simu;ate depth clamp in shadow shader when z < 0. This way we force distanced objects to still cast a shadow.
			sceneNode.renderShadows(ident, skyLight);
			skyLight.getFramebuffer().unbindDraw();
			int count = 0;
			ArrayList<PointLightNode> lightList = new ArrayList<PointLightNode>();
			sceneNode.getAllLights(lightList, ident, cam.getLocation().toVector4());
			Collections.sort(lightList, new PointLightSorter());
			for (PointLightNode light : lightList) {
				if (light.getColour().x > 0 || light.getColour().y > 0 || light.getColour().z > 0) {
					// Render each face
					if (lightShadows.size() == count) {
						ShadowCube shadow = new ShadowCube(MainThread.config.getInteger("shadowSize", 1024));
						lightShadows.add(shadow);
					}
					ShadowCube shadow = lightShadows.get(count);
					shadow.setLight(light);
					for (int i = 0; i < 6; i++) {
						shadow.getFramebuffer().bindDraw(i);
						Util.checkErr();
						shadow.setFace(i);
						Util.checkErr();
						sceneNode.renderShadows(ident, shadow);
						Util.checkErr();
						shadow.getFramebuffer().unbindDraw();
						Util.checkErr();
					}

					count++;
				}
				if (count == 16) {
					break;
				}
			}
			GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

		}
		GL11.glViewport(totalx, totaly, width, height);
		GL11.glScissor(totalx, totaly, width, height);
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glClearColor(clearCol.x, clearCol.y, clearCol.z, clearCol.w);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | (clear ? GL11.GL_COLOR_BUFFER_BIT : 0));
		Util.checkErr();

		prepare3DOpaque();
		sceneNode.render(ident);
		Util.checkErr();
		prepare3DTransparent();
		sceneNode.renderSemiTransparent(ident);
		MouseRenderableHoverEvent lF = MainThread.main.input.getLastHovered();
		if (lF != null) {
			if (lF.getRenderable() instanceof BaseObject) {
				/*
				 * GL11.glDepthFunc(GL11.GL_LESS);
				 * GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				 * GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_LINE);
				 * 
				 * GL11.glLineWidth(10f);
				 * GL11.glCullFace(GL11.GL_FRONT);
				 * GL11.glEnable(GL11.GL_CULL_FACE);
				 * GL11.glDisable(GL11.GL_BLEND);
				 * BaseObject bo = (BaseObject) lF.getRenderable();
				 * RenderStyle rs = bo.getRenderStyle();
				 * bo.setRenderStyle(RenderStyle.HALO);
				 * sceneNode.renderOne(ident, bo, ident);
				 * 
				 * GL11.glDisable(GL11.GL_BLEND);
				 * 
				 * bo.setRenderStyle(rs);
				 * GL11.glCullFace(GL11.GL_BACK);
				 * GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				 * GL11.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);
				 * 
				 * GL11.glLineWidth(1f);
				 * sceneNode.renderOne(ident, bo, ident);
				 */

			}
		}

		revertSettings();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);

	}

	private void prepare3DShadow() {
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthFunc(GL11.GL_LESS);
		// GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		// GL11.glPolygonOffset(1.1f, 10f);
		GL11.glCullFace(GL11.GL_FRONT);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	private void revertSettings() {
		GL11.glViewport(0, 0, MainThread.lastW, MainThread.lastH);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
	}

	public void prepare3DTransparent() {
		Util.checkErr();
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		Util.checkErr();
		GL11.glEnable(GL11.GL_BLEND);
		Util.checkErr();
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		Util.checkErr();
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	private void prepare3DOpaque() {
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthFunc(GL11.GL_LESS);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	public void update(float delta) {
		cam.update(delta);

		projectionMatrix = cam.getProjectionMatrix(width, height);
		viewMatrix = cam.getViewMatrix();

		Shadow2D skyLight = sceneNode.getSkyLight();
		hasshadows = skyLight != null;
		shadows = MainThread.config.getBoolean("shadows", true);
		if (hasshadows) {
			skyLight.update(delta);
		}
		sceneNode.update(this, delta);

	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public void setProjectionMatrix(Matrix4f projectionMatrix) {
		this.projectionMatrix = projectionMatrix;
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public void setViewMatrix(Matrix4f viewMatrix) {
		this.viewMatrix = viewMatrix;
	}

	public Matrix4f getShadowMatrix() {
		return shadowMatrix;
	}

	public void setShadowMatrix(Matrix4f shadowMatrix) {
		this.shadowMatrix = shadowMatrix;
	}

	public void setMatrices(int pID, Matrix4f modelView) {
		GL20.glUseProgram(pID);
		Util.setUniformMat44(pID, "M", modelView);
		Matrix4f MV = viewMatrix.mult(modelView, null);
		Util.setUniformMat44(pID, "V", viewMatrix);
		Util.setUniformMat44(pID, "MV", MV);
		Matrix4f MVP = projectionMatrix.mult(MV, null);
		Util.setUniformMat44(pID, "MVP", MVP);
		Matrix3f N = makeNormalMatrix(modelView);
		Util.setUniformMat33(pID, "N", N); // FIXME Was M 3x3, watch out for the bugs this'll cause

		if (shadows && hasshadows) {
			Matrix4f shadowBMVP = Util.shadowBiasMatrix.mult(sceneNode.getSkyLight().getMVP(modelView, this), null);
			Util.setUniformMat44(pID, "shadowBMVP", shadowBMVP);
		}
	}

	public Matrix3f makeNormalMatrix(Matrix4f in) {
		Matrix4f out = new Matrix4f(in);

		out.inverse(out);
		out.transpose(out);
		// out.m00 = in.m00;
		// out.m01 = in.m01;
		// out.m02 = in.m02;
		// out.m10 = in.m10;
		// out.m11 = in.m11;
		// out.m12 = in.m12;
		// out.m20 = in.m20;
		// out.m21 = in.m21;
		// out.m22 = in.m22;
		// out = (Matrix3f) out.transpose();
		// out = (Matrix3f) out.invert();

		return new Matrix3f(out);
	}

	public void setShadowMatrices(int pID, Matrix4f modelView, Shadow shadow) {
		GL20.glUseProgram(pID);

		Util.setUniformMat44(pID, "M", modelView);
		Matrix4f MVP = shadow.getMVP(modelView, this);
		Util.setUniformMat44(pID, "MVP", MVP);
	}

	public Matrix4f createMatrixFor(Location location, Matrix4f parent, Matrix4f inside) {
		Vector3f loc = location.toVector3();
		if (cam != null) {
			loc = location.minus(cam.getLocation());
		}
		Matrix4f modelView = new Matrix4f();
		// Translate to world co-ords first
		modelView.translate(loc, modelView);

		// Then scale/rotate
		modelView.scale(location.getScale(), modelView);
		modelView.rotate(Util.degreesToRadians(location.getRotate().z), new Vector3f(0, 0, 1), modelView);
		modelView.rotate(Util.degreesToRadians(location.getRotate().y), new Vector3f(0, 1, 0), modelView);
		modelView.rotate(Util.degreesToRadians(location.getRotate().x), new Vector3f(1, 0, 0), modelView);
		if (parent != null) {
			// Matrix4f.mul(modelView, parent, modelView);
			parent.mult(modelView, modelView);
		}
		if (inside != null) {
			inside.mult(inside, modelView);
		}
		return modelView;
	}

	public void bindLights(int pID, int texture) {
		GL20.glUseProgram(pID);
		int lights = MainThread.config.getInteger("lightCount", 16);
		int showShadows = GL20.glGetUniformLocation(pID, "showShadows");
		GL20.glUniform1i(showShadows, hasshadows ? 1 : 0);
		Util.checkErr();
		if (cam instanceof FlyByCamera) {
			int eyepos = GL20.glGetUniformLocation(pID, "eyepos");
			Util.checkErr();
			Location camL = ((FlyByCamera) cam).getCameraLocation();
			Util.checkErr();
			GL20.glUniform3f(eyepos, camL.getFullXAsFloat(), camL.getFullYAsFloat(), camL.getZ());
			Util.checkErr();
		}
		if (hasshadows) {
			int lightDir = GL20.glGetUniformLocation(pID, "sunDir");
			Vector3f dir = sceneNode.getSkyLight().getDirection().normalise(null);
			GL20.glUniform3f(lightDir, dir.x, dir.y, dir.z);
			int sunStr = GL20.glGetUniformLocation(pID, "sunStr");
			GL20.glUniform1f(sunStr, sceneNode.getSkyLight().getIntensity());
		}
		if (!shadows) {
			return;
		}

		int lightCount = GL20.glGetUniformLocation(pID, "lightCount");
		GL20.glUniform1i(lightCount, lights);

		if (lights < 4) {
			lights = 4;
		}
		for (int i = 0; i < lights; i++) {
			ShadowCube ls;
			if (i >= lightShadows.size()) {
				ls = new ShadowCube(MainThread.config.getInteger("shadowSize", 1024));
				lightShadows.add(ls);
			}
			ls = lightShadows.get(i);
			if (ls == null) {
				continue;
			}
			PointLightNode light = ls.getLight();
			if (light == null) {
				continue;
			}
			int cube = GL20.glGetUniformLocation(pID, "cube" + i);
			int lightcolour = GL20.glGetUniformLocation(pID, "lights[" + i + "].colour");
			int lightposition = GL20.glGetUniformLocation(pID, "lights[" + i + "].position");
			int lightpower = GL20.glGetUniformLocation(pID, "lights[" + i + "].power");
			Vector3f loc = light.getPosition().toVector3();
			if (cam != null) {
				// loc = light.getPosition().minus(cam.getLocation());
				// loc = cam.minus(light.getPosition());
			}
			Util.checkErr();
			// Only upload a shadow texture location if we're having shadows for
			// the light. Basic has lights but no shadows
			if (i < showShadows) {
				GL20.glUniform1i(cube, texture + i - GL13.GL_TEXTURE0);
				ls.getFramebuffer().bindDepthTexture(texture + i);
			}
			Util.checkErr();
			GL20.glUniform4f(lightcolour, light.getColour().x, light.getColour().y, light.getColour().z, light.getColour().w);
			Util.checkErr();
			GL20.glUniform4f(lightposition, loc.x, loc.y, loc.z, 1f);
			Util.checkErr();
			GL20.glUniform1f(lightpower, light.getPower());
			Util.checkErr();
			Util.checkErr();
		}
	}

	public void unbindLights() {
		int lights = MainThread.config.getInteger("lightCount", 16);
		if (!shadows) {
			return;
		}
		int lightShadowCount = lights;
		if (lights < 4) {
			lights = 4;
		}
		for (int i = 0; i < lightShadowCount; i++) {
			ShadowCube ls = lightShadows.get(i);
			if (ls == null) {
				continue;
			}
			Util.checkErr();
			ls.getFramebuffer().unbindDepthTexture();
			Util.checkErr();
		}
	}

	public void bindShadowData(int glTexture) {
		if (!hasshadows || !shadows) {
			return;
		}
		Util.checkErr();
		sceneNode.getSkyLight().getFramebuffer().bindDepthTexture(glTexture);
		Util.checkErr();
	}

	public void unbindShadowData() {
		if (!shadows || !hasshadows) {
			return;
		}
		Util.checkErr();
		sceneNode.getSkyLight().getFramebuffer().unbindDepthTexture();
		Util.checkErr();
	}

}
