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
package com.opengrave.og;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLUtil;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.world.Material;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.engine.Camera;
import com.opengrave.og.engine.Location;
import com.opengrave.og.engine.RenderView;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix3f;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;
import com.opengrave.og.util.Vector4f;

public class Util {

	public static float nearZ = .1f, farZ = 50f; // , fov = 60f;
	public static Matrix4f shadowBiasMatrix;
	// public static Matrix4f projectionMatrix; // Window to 3D Perspective
	public static Matrix4f guiMatrix; // Window to 2D Gui
	// public static Matrix4f viewMatrix; // Perspective to World co-rds
	public static FloatBuffer matrix44Buffer;
	public static FloatBuffer matrix33Buffer;
	private final static double PI = 3.14159265358979323846;

	public static void initMatrices() {
		shadowBiasMatrix = new Matrix4f();
		Vector3f translate = new Vector3f(0.5f, 0.5f, 0.5f);
		shadowBiasMatrix.translate(translate, shadowBiasMatrix);
		shadowBiasMatrix.scale(translate, shadowBiasMatrix);
		System.out.println(shadowBiasMatrix);

		// update3DProjection();
		update2DProjection();
		// viewMatrix = new Matrix4f();
		matrix44Buffer = BufferUtils.createFloatBuffer(16);
		matrix33Buffer = BufferUtils.createFloatBuffer(9);
	}

	// public static void updateMatrices() {
	// update3DProjection();
	// update2DProjection();
	// }

	// private static void update3DProjection() {
	// float fov = 40f;
	// projectionMatrix = proj(fov, Display.getWidth(), Display.getHeight(),
	// nearZ, farZ);

	// }

	public static void update2DProjection() {
		guiMatrix = new Matrix4f();
		float x_max = MainThread.lastW - 1f;
		float y_max = MainThread.lastH - 1f;
		float x_min = 0f;
		float y_min = 0f;
		float left = x_min, right = x_max, top = y_min, bottom = y_max;
		float zFar = 200f, zNear = -0.1f;
		guiMatrix.set(0, 0, 2f / (right - left));
		guiMatrix.set(1, 1, 2f / (top - bottom));
		guiMatrix.set(2, 2, -2f / (zFar - zNear));
		guiMatrix.set(3, 3, 1f);
		guiMatrix.set(3, 2, -((zFar + zNear) / (zFar - zNear)));
		guiMatrix.set(3, 1, -((top + bottom) / (top - bottom)));
		guiMatrix.set(3, 0, -((right + left) / (right - left)));
		guiMatrix.translate(new Vector3f(0.35f, 0.35f, 0f), guiMatrix); // Deal with pixel
		// perfection on
		// location
	}

	public static float degreesToRadians(float f) {
		return f * (float) (PI / 180d);
	}

	public static Matrix4f lookDir(Vector3f eye, Vector3f direction, Vector3f up) {
		eye.add(direction, eye);
		Matrix4f mat1 = Matrix4f.lookAt(eye, eye, up);
		return mat1;
	}

	public static void setUniformMat44(int pID, String string, Matrix4f matrix) {
		matrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		setUniformMat44(pID, string, matrix44Buffer);
	}

	public static void setUniformMat33(int pID, String string, Matrix3f matrix) {
		matrix.store(matrix33Buffer);
		matrix33Buffer.flip();
		setUniformMat33(pID, string, matrix33Buffer);
	}

	public static void setUniformMat44(int pID, String string, FloatBuffer matrix44) {
		// System.out.println("Program "+pID);
		Util.checkErr();
		int i = GL20.glGetUniformLocation(pID, string);
		Util.checkErr();
		if (i == -1) {
			// System.out.println("Cannot find uniform '"+string+"'");
			// Thread.dumpStack();
		} else {
			GL20.glUniformMatrix4fv(i, false, matrix44);
			Util.checkErr();
		}
	}

	public static void setUniformMat33(int pID, String string, FloatBuffer matrix33) {
		// System.out.println("Program "+pID);
		Util.checkErr();
		int i = GL20.glGetUniformLocation(pID, string);
		Util.checkErr();
		if (i == -1) {
			// System.out.println("Cannot find uniform '"+string+"'");
			// Thread.dumpStack();
		} else {
			GL20.glUniformMatrix3fv(i, false, matrix33);
			Util.checkErr();
		}
	}

	public static void setUniform(int pID, String string, Vector4f color) {
		// System.out.println("Program "+pID);
		Util.checkErr();
		int i = GL20.glGetUniformLocation(pID, string);
		Util.checkErr();
		if (i == -1) {
			// System.out.println("Cannot find uniform '"+string+"'");
			// Thread.dumpStack();
		} else {
			GL20.glUniform4f(i, color.x, color.y, color.z, color.w);
			Util.checkErr();
		}
	}

	/**
	 * Used for 2D "HUD" Renderables
	 * 
	 * @param pID
	 * @param location
	 * @param scale
	 * @param rotate
	 */
	public static void setMatrices(int pID, Vector3f location) {
		Util.checkErr();
		GL20.glUseProgram(pID);
		Matrix4f modelView = new Matrix4f();
		modelView.translate(location, modelView);
		// Matrix4f.scale(location.getScale(), modelView, modelView);
		// Matrix4f.rotate(degreesToRadians(rotate.z), new Vector3f(0, 0, 1),
		// modelView, modelView);
		// Matrix4f.rotate(degreesToRadians(rotate.y), new Vector3f(0, 1, 0),
		// modelView, modelView);
		// Matrix4f.rotate(degreesToRadians(rotate.x), new Vector3f(1, 0, 0),
		// modelView, modelView);
		Util.checkErr();

		guiMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		setUniformMat44(pID, "guiMatrix", matrix44Buffer);
		// projectionMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		// setUniform(pID, "projectionMatrix", matrix44Buffer);
		// viewMatrix.store(matrix44Buffer); matrix44Buffer.flip();
		// setUniform(pID, "viewMatrix", matrix44Buffer);
		modelView.store(matrix44Buffer);
		matrix44Buffer.flip();
		setUniformMat44(pID, "modelMatrix", matrix44Buffer);
		Util.checkErr();

	}

	public static Matrix4f createMatrixFor(Location location, Matrix4f parent, Matrix4f inside, RenderView context) {
		Camera cam = null;
		if (context != null) {
			cam = context.getCam();
		}
		Vector3f loc = location.toVector3();
		if (cam != null) {
			loc = location.minus(cam.getLocation());
		}
		Matrix4f modelView = new Matrix4f();
		// Translate to world co-ords first
		modelView.translate(loc, modelView);

		// Then scale/rotate
		modelView.scale(location.getScale(), modelView);
		modelView.rotate(degreesToRadians(location.getRotate().z), new Vector3f(0, 0, 1), modelView);
		modelView.rotate(degreesToRadians(location.getRotate().y), new Vector3f(0, 1, 0), modelView);
		modelView.rotate(degreesToRadians(location.getRotate().x), new Vector3f(1, 0, 0), modelView);
		if (parent != null) {
			// Matrix4f.mul(modelView, parent, modelView);
			parent.mult(modelView, modelView); // TODO Check Mult ordering.
		}
		if (inside != null) {
			inside.mult(modelView, modelView);
		}
		return modelView;
	}

	// public static void setMatrices(int pID, Matrix4f parent, Location
	// location) {
	// GL20.glUseProgram(pID);
	// Matrix4f modelView = createMatrixFor(location, parent, null);

	// modelView.store(matrix44Buffer);
	// matrix44Buffer.flip();
	// setUniformMat44(pID, "M", matrix44Buffer);
	// Matrix4f MV = Matrix4f.mul(viewMatrix, modelView, null);
	// MV.store(matrix44Buffer);
	// matrix44Buffer.flip();
	// setUniformMat44(pID, "MV", matrix44Buffer);

	// Matrix4f MVP = Matrix4f.mul(projectionMatrix, MV, null);
	// MVP.store(matrix44Buffer);
	// matrix44Buffer.flip();
	// setUniformMat44(pID, "MVP", matrix44Buffer);

	// if (HGMainThread.config.getBoolean("shadows", true)) {
	// Matrix4f shadowBMVP = Matrix4f.mul(shadowBiasMatrix,
	// HGMainThread.main.skyLight.getMVP(modelView), null);
	// shadowBMVP.store(matrix44Buffer);
	// matrix44Buffer.flip();
	// setUniformMat44(pID, "shadowBMVP", matrix44Buffer);
	// }
	// }

	public static void checkErr(boolean b) {
		if (!b) {
			// Throw it away!
			GL11.glGetError();
		} else {
			checkErr();
		}

	}

	public static void checkErr() {
		int errorValue = GL11.glGetError();
		if (errorValue != GL11.GL_NO_ERROR) {
			String err = GLUtil.getErrorString(errorValue);
			Thread.dumpStack();
			System.out.println(err);
		}
	}

	public static String getDigest(String dataFile) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			new DebugExceptionHandler(e);
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(dataFile);
		} catch (FileNotFoundException e) {
			return "0";
		}
		byte[] dataBytes = new byte[1024];

		int nread = 0;

		try {
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
		} catch (IOException e) {
			new DebugExceptionHandler(e, dataFile);
		}

		byte[] mdbytes = md.digest();

		// convert the byte to hex format
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		try {
			fis.close();
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}
		return sb.toString();
	}

	public static void downloadAndSave(File cache, String url, String local) {
		File f = new File(cache, local);
		File parent = f.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}

		System.out.println(cache + " " + url);
		byte[] buffer = new byte[8 * 1024];
		InputStream input = null;
		OutputStream output = null;
		try {
			HttpsURLConnection conn = openConnection(url);
			input = conn.getInputStream();
			output = new FileOutputStream(new File(cache, local).getAbsolutePath());
			int bytesRead;
			while ((bytesRead = input.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
		} catch (FileNotFoundException e) {
			new DebugExceptionHandler(e, url, local);
		} catch (MalformedURLException e) {
			new DebugExceptionHandler(e, url, local);
		} catch (IOException e) {
			new DebugExceptionHandler(e, url, local);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				new DebugExceptionHandler(e, url, local);
			}
		}
	}

	public static HttpsURLConnection openConnection(String urlS) {
		if (sslSocketFactory == null) {
			prepSSL();
		}
		HttpsURLConnection conn = null;
		try {
			URL url = new URL(urlS);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setSSLSocketFactory(sslSocketFactory);
		} catch (IOException e) {
			new DebugExceptionHandler(e, urlS);
		}
		return conn;
	}

	public static void prepSSL() {
		try {
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

				@Override
				public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
				}

				@Override
				public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} };
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			sslSocketFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			new DebugExceptionHandler(e);
		} catch (KeyManagementException e) {
			new DebugExceptionHandler(e);

		}
	}

	private static SSLContext sslContext;
	private static SSLSocketFactory sslSocketFactory;
	private static boolean debug;

	public static void loadMaterials(MaterialList matList, int pID) {
		int i = 0;
		for (Material m : matList.all()) {
			int locCol1 = GL20.glGetUniformLocation(pID, "material[" + i + "].colour");
			int locTex = GL20.glGetUniformLocation(pID, "material[" + i + "].textureindex");
			int locDTex = GL20.glGetUniformLocation(pID, "material[" + i + "].texturedataindex");

			/*
			 * if (locCol == -1) { throw new
			 * RuntimeException("No Material Colour location"); } if (locTex ==
			 * -1) { throw new RuntimeException("No Material Texture location");
			 * }
			 */
			GL20.glUniform4f(locCol1, m.getColour().x, m.getColour().y, m.getColour().z, m.getColour().w);
			GL20.glUniform1f(locTex, (float) m.getTextureIndex());
			GL20.glUniform1f(locDTex, (float) m.getTextureDataIndex());

			i++;
		}
	}

	public static void setRenderStyle(int pID, RenderStyle style) {
		int i = 0;
		if (style == RenderStyle.GRAYSCALE) {
			i = 1;
		} else if (style == RenderStyle.SEPIA) {
			i = 2;
		} else if (style == RenderStyle.HALO) {
			i = 3;
		}
		GL20.glUniform1i(GL20.glGetUniformLocation(pID, "renderStyle"), i);

	}

	public static void toggleDebug() {
		debug = !debug;
		if (debug) {
			MainThread.showDebugWindow(true);
		} else {
			MainThread.showDebugWindow(false);
		}
	}

	public static Vector4f unproject(int x, int y, float z, Matrix4f viewM, Matrix4f projM, int totalx, int totaly, int width, int height) {
		Matrix4f finalM = viewM.mult(projM, null);
		Vector4f in = new Vector4f(x, y, z, 1f);
		finalM.inverse(finalM);
		in.x = (in.x - totalx) / width;
		in.y = (in.y - totaly) / height;

		in.x = in.x * 2 - 1;
		in.y = in.y * 2 - 1;
		in.z = in.z * 2 - 1;

		Vector4f out = finalM.mult4(in, null);
		if (out.w == 0) {
			return null;
		}
		out.x /= out.w;
		out.y /= out.w;
		out.z /= out.w;
		out.w = 1f;

		return out;
	}

}
