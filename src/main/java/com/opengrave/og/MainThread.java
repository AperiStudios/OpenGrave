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

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.text.*;

import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.opengrave.common.*;
import com.opengrave.common.DebugWindowThread.DebugType;
import com.opengrave.common.config.Config;
import com.opengrave.common.event.DebugWindowTextInputEvent;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.packet.Packet;
import com.opengrave.common.xml.HGXMLThread;
import com.opengrave.og.base.Renderable;
import com.opengrave.og.input.InputMain;
import com.opengrave.og.light.CubeData;
import com.opengrave.og.resources.GUIXML;
import com.opengrave.og.resources.Resources;
import com.opengrave.og.states.BaseState;
import com.opengrave.og.states.LoadingState;
import com.opengrave.og.states.ProfileState;
import com.opengrave.og.states.waitables.PreMenuLoader;
import com.opengrave.og.util.Vector3f;

public class MainThread extends Thread implements MainThreadInterface {

	public static String USERNAME, PASSWORD, TOKEN, SERVERLIST;
	public static int hgver = -1, assetver = -1;
	public static boolean running;
	public static MainThread main;
	public static ProfileState profileState;
	private static ServerConnection serverConnectionData = null;

	private static BaseState state = null, nextState = null;

	public InputMain input;
	public static Config config;
	public static File cache;

	private static ArrayList<Runnable> toDoList = new ArrayList<Runnable>();

	public int fpsCap;

	private long lastCheckIn = 0;
	private long checkInInter = 1000 * 60 * 10; // Check in every 10 minutes
	private boolean needsConfigUpdate = false;
	private ModSession session;
	private GLFWFramebufferSizeCallback framebufferSizeCallback;
	// private SoundSystem soundSystem;
	public static long window;
	// private GLFWFramebufferSizeCallback framebufferSizeCallback;

	public static JFrame debugWindow;
	static JTextPane debugText;
	static AttributeSet errStyle, outStyle;
	static JTabbedPane debugTabs;
	public static Object debugLock = new Object();

	public static int lastW = 0;
	public static int lastH = 0;
	public static int SHADOWSIZE = 1024;

	public static PrintStream oldErr;

	public void run() {
		Thread.currentThread().setName("OpenGL and game");
		running = true;
		main = this;
		config = new Config("hg.config");

		// Attempt to get HG and assets build version for reporting bugs
		try (Scanner s = new Scanner(new File(cache, "hgjava.ver"))) {
			hgver = s.nextInt();
		} catch (FileNotFoundException e) {
		}
		try (Scanner s = new Scanner(new File(cache, "hgasset.ver"));) {
			assetver = s.nextInt();
		} catch (FileNotFoundException e) {
		}
		// Set FPS Cap
		if (!config.getBoolean("capfps", true)) {
			fpsCap = -1;
		} else {
			fpsCap = config.getInteger("fpslimit", 60);
		}
		initDebugWindow();
		// Start Event thread
		new LoadingThread();
		EventDispatcher.events.beginEventThread();
		// Prepare known packets for Client->GameServer and opposite
		Packet.init();
		// Prepare OpenGL.
		initGL();
		// initSound();
		// Prepare input handling
		input = new InputMain();
		// Set a first state. Prepare mods and then load menu
		changeState(new LoadingState(new PreMenuLoader()));
		startAuthClient();
		// And we're away!
		gameLoop();
	}

	/*
	 * private void initSound() {
	 * try {
	 * SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
	 * } catch (SoundSystemException e) {
	 * e.printStackTrace();
	 * }
	 * try {
	 * soundSystem = new SoundSystem(LibraryLWJGLOpenAL.class);
	 * soundSystem.switchLibrary(LibraryLWJGLOpenAL.class);
	 * } catch (SoundSystemException e) {
	 * e.printStackTrace();
	 * }
	 * 
	 * // loadSoundFile("midi/test.mid");
	 * // playSoundFile("midi/test.mid");
	 * }
	 * 
	 * public void playSoundFile(String string) {
	 * String label = string;
	 * label = label.replaceAll("/[^a-zA-Z0-9]/", "");
	 * soundSystem.play(label);
	 * soundSystem.setVolume(label, 1f);
	 * }
	 * 
	 * public void loadSoundFile(String file) {
	 * String label = file;
	 * label = label.replaceAll("/[^a-zA-Z0-9]/", "");
	 * File f = new File(cache, file);
	 * try {
	 * soundSystem.newStreamingSource(true, file, new URL("file://" + f.getAbsolutePath()), label, true, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
	 * } catch (MalformedURLException e) {
	 * e.printStackTrace();
	 * }
	 * }
	 */

	private void startAuthClient() {
		HGXMLThread.requestAuthClient();
		HGXMLThread.authURL = SERVERLIST;
		Thread t = new Thread(new HGXMLThread(), "XML Communication Thread");
		t.start();
	}

	public void startApp(File cache, String userName, String passWord, String serverList) {
		USERNAME = userName;
		PASSWORD = passWord;
		SERVERLIST = serverList;
		MainThread.cache = cache;
		Resources.cache = cache;
		start();
	}

	public void checkIn() {
		long timeNow = System.currentTimeMillis();
		if (lastCheckIn + checkInInter < timeNow) {
			checkInNow();
		}
	}

	public void checkInNow() {
		long timeNow = System.currentTimeMillis();
		lastCheckIn = timeNow;
		HGXMLThread.requestClientCheckIn();
	}

	public void gameLoop() {
		Util.checkErr();
		double lasttime = getTime(), lastFPS = getTime();
		int fps = 0;
		while (running) {
			if (needsConfigUpdate) {
				createConfig();
				needsConfigUpdate = false;
			}
			synchronized (toDoList) {
				while (toDoList.size() > 0) {
					Runnable runnable = toDoList.remove(0);
					runnable.run();
				}
			}

			if (nextState != null) {

				// Something has caused us to switch states, stop the last
				// cleanly and start the next
				if (state != null) {
					state.stop();
					state.finalise();
				}

				state = nextState;
				state.prestart();
				state.start();

				nextState = null;
			}
			// Update FPS counter in window title TODO: Remove from window title
			// and place into a debug context ingame
			if (getTime() - lastFPS > 1000) {
				// use fps value
				fps = 0;
				lastFPS += 1000;
			}

			fps++;
			// Prepare time delta for updating graphics
			double time = getTime();
			float delta = (float) (time - lasttime); // TODO Change delta to double as a rule?
			lasttime = time;
			input.doEet(state, delta);
			Util.checkErr();
			state.updateGUI(delta);
			Util.checkErr();
			state.update(delta); // Pre-render update
			Util.checkErr();

			// Clear frame ready for next frame
			GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
			GL11.glViewport(0, 0, lastW, lastH);

			set2D();
			state.renderGUI();
			if (glfwWindowShouldClose(window) == GL_TRUE) {
				running = false;
			}
			glfwPollEvents();
			// if (fpsCap > 10) {
			glfwSwapBuffers(window);
			// }
			Util.checkErr();

		}

		running = false;
		// soundSystem.cleanup();
		System.exit(1);
	}

	public static void set2D() {
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDepthFunc(GL11.GL_LEQUAL);

	}

	protected void initGL() {
		if (glfwInit() != GLFW_TRUE) {
			System.exit(1);
		}
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		window = glfwCreateWindow(800, 600, "HiddenGrave", NULL, NULL);
		if (window == NULL) {
			System.exit(1);
		}
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1); // TODO condig of vsync. Enable vsync
		GL.createCapabilities();
		glfwSetFramebufferSizeCallback(window, (framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				onResize(width, height);
			}
		}));
		onResize(800, 600);
		// TODO Check all extensions. TEX 2D ARRAY, GLSL 130
		createConfig();

		Util.initMatrices();
		Renderable.init();
		GUIXML.init();

		// Prepare Lighting
		initLighting();
		// Default Values
		GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f); // sets background to grey
		Util.checkErr();
		GL11.glClearDepth(1.0f); // clear depth buffer
		Util.checkErr();
		GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables depth testing
		Util.checkErr();
		GL11.glDepthFunc(GL11.GL_LEQUAL); // sets the type of test to use for
											// depth testing
		GL11.glEnable(GL11.GL_BLEND);
		Resources.loadTextures(); // Reconsider positioning. Other than GUI
									// texture we could offset these in another
									// thread... Possibly?
		Resources.loadModels();
		Resources.loadFonts();

	}

	protected void onResize(int width, int height) {
		DisplayRes dr = DisplayRes.get();
		if (width != lastW || height != lastH) {
			lastH = height;
			lastW = width;
			Util.update2DProjection();
		}
	}

	public void createConfig() {
		boolean isSet = config.getBoolean("set", false);
		System.out.println("Running OpenGL : " + GL11.glGetString(GL11.GL_VERSION));

		String glsl = GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION).split(" ")[0];
		System.out.println(glsl + " " + Float.parseFloat(glsl));
		if (Float.parseFloat(glsl) < 1.299f) { // Stupid float accuracy. < 1.30
			System.out.println("This program requires GLSL 1.30 as an absolute minimum");
			System.exit(130);
		}
		if (!isSet) {
			int textures = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS) - 3;
			System.out.println("Maximum spare textures (minus reserved) : " + textures);
			config.setBoolean("shadows", true);
			if (textures < 4) {
				System.out.println("Setting no point-light shadows");
				config.setInteger("lightCount", 0);
			} else if (textures < 8) {
				System.out.println("Setting 4 point-lights with shadows");
				config.setInteger("lightCount", 4);
			} else if (textures < 16) {
				System.out.println("Setting 8 point-lights with shadows");
				config.setInteger("lightCount", 8);
			} else {
				System.out.println("Setting 16 point-lights with shadows");
				// config.setInteger("lightCount", 16);
				// TODO Create lighting-more
				config.setInteger("lightCount", 8);
			}

			// TODO Check for GL 3.0 and turn off shadows/lightCount if below
			// if (!GLContext.getCapabilities().OpenGL30) {
			// System.out.println("This program does not support OpenGL versions before 3.0. Setting absolute minimum detail");
			// if (!isSet) {
			// // We'll assume the worst.
			// config.setInteger("lightCount", 0);
			// config.setBoolean("shadows", false);
			// }
			// }
			Resources.removeShadersWithLighting();
			config.setBoolean("set", true);
		}
	}

	private void initLighting() {
		CubeData.data.add(new CubeData(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, new Vector3f(1f, 0f, 0f), new Vector3f(0f, -1f, 0f)));
		CubeData.data.add(new CubeData(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, new Vector3f(-1f, 0f, 0f), new Vector3f(0f, -1f, 0f)));
		CubeData.data.add(new CubeData(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, new Vector3f(0f, 1f, 0f), new Vector3f(0f, 0f, 1f)));
		CubeData.data.add(new CubeData(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, new Vector3f(0f, -1f, 0f), new Vector3f(0f, 0f, -1f)));
		CubeData.data.add(new CubeData(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, new Vector3f(0f, 0f, 1f), new Vector3f(0f, -1f, 0f)));
		CubeData.data.add(new CubeData(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, new Vector3f(0f, 0f, -1f), new Vector3f(0f, -1f, 0f)));

	}

	public static void changeState(BaseState state) {
		nextState = state;
	}

	public double getTime() {
		double f = (glfwGetTime() * 1000);

		return f;
	}

	public static BaseState getGameState() {
		return state;
	}

	public static void sendPacket(Packet packet) {
		getConnection().sendPacket(packet);
	}

	public static void changeServerConnection(ServerData sd) {
		// TODO Check assumption that
		// "We only run this code when the last connection is completely dead/closed"
		if (serverConnectionData != null) {
			serverConnectionData.killConnection();
		}
		serverConnectionData = new ServerConnection(sd.getIP(), sd.getPort(), sd.getId());
		// Don't connect yet. So far this is only information about how and
		// where to connect
		// We need to make sure we double check asset loading and mod loading
		// before we begin an attempted connect.
	}

	public static void startConnectionThread() {
		serverConnectionData.connect();
	}

	public static ServerConnection getConnection() {
		return serverConnectionData;
	}

	public void newConfig() {
		needsConfigUpdate = true;
	}

	public static void showDebugWindow(boolean b) {
		debugWindow.setVisible(b);
	}

	private void initDebugWindow() {
		debugWindow = new JFrame("HG Debug");

		debugText = new JTextPane();
		debugText.setEditable(false);
		StyleContext sc = StyleContext.getDefaultStyleContext();
		errStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);
		outStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLUE);

		final JTextField input = new JTextField();
		input.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String text = input.getText();
				EventDispatcher.dispatchEvent(new DebugWindowTextInputEvent(text));
				input.setText("");
			}

		});
		JScrollPane scroll = new JScrollPane(debugText);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scroll, BorderLayout.CENTER);
		debugTabs = new JTabbedPane();
		debugTabs.addTab("Debug", panel);
		debugWindow.add(debugTabs, BorderLayout.CENTER);
		debugWindow.add(input, BorderLayout.SOUTH);
		debugWindow.pack();
		debugWindow.setSize(400, 400);
		debugWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// TODO Also place debug into text file.
		try {
			PipedOutputStream pOut = new PipedOutputStream();
			PipedInputStream pIn = new PipedInputStream(pOut);

			PrintStream oldOut = System.out;
			System.setOut(new PrintStream(pOut, true));

			BufferedReader reader = new BufferedReader(new InputStreamReader(pIn));
			new DebugWindowThread(reader, oldOut, DebugType.OUT);
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}

		try {
			PipedOutputStream pErr = new PipedOutputStream();
			PipedInputStream pInErr = new PipedInputStream(pErr);

			oldErr = System.err;
			System.setErr(new PrintStream(pErr, true));

			BufferedReader readerErr = new BufferedReader(new InputStreamReader(pInErr));
			new DebugWindowThread(readerErr, oldErr, DebugType.ERROR);
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		}
		Thread.setDefaultUncaughtExceptionHandler(new DebugUncaughtExceptionHandler());
		showDebugWindow(true);
	}

	public static void addDebugLine(String line, DebugType type) {
		AttributeSet x = outStyle;
		if (type == DebugType.ERROR) {
			x = errStyle;
		}
		synchronized (debugText) {
			StyledDocument doc = debugText.getStyledDocument();
			try {
				doc.insertString(doc.getLength(), line + "\n", x);
				debugText.setCaretPosition(doc.getLength());
			} catch (BadLocationException e) {
				new DebugExceptionHandler(e);

			}
		}

	}

	public static void addDebugException(DebugExceptionHandler handler) {
		synchronized (debugTabs) {
			debugTabs.addTab(handler.getName(), handler.makeGUI());
		}
	}

	public static void removeDebugException(DebugExceptionHandler handler) {
		synchronized (debugTabs) {
			debugTabs.remove(handler.makeGUI());
		}
	}

	/**
	 * Used to push GL-context-only commands into the GL thread.
	 */
	public static void addToGLCommands(Runnable run) {
		synchronized (toDoList) {
			toDoList.add(run);
		}
	}

	public static void newSession() {
		main.session = new ModSession();
	}

	public static ModSession getSession() {
		return main.session;
	}

}
