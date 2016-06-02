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
package com.opengrave.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.UUID;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.opengrave.common.Connector;
import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.ModSession;
import com.opengrave.common.Readyable;
import com.opengrave.common.config.BinaryNodeException;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.event.*;
import com.opengrave.common.packet.Packet;
import com.opengrave.common.packet.fromclient.ClientAuthPacket;
import com.opengrave.common.packet.fromclient.ClientSaveFilePacket;
import com.opengrave.common.packet.fromclient.NewOrderPacket;
import com.opengrave.common.packet.fromclient.PlayerCharacterChosen;
import com.opengrave.common.packet.fromserver.*;
import com.opengrave.common.world.*;
import com.opengrave.common.xml.HGXMLThread;
import com.opengrave.common.xml.XML;
import com.opengrave.server.events.*;
import com.opengrave.server.exptoken.Token;
import com.opengrave.server.runnables.RunnableThread;

public class Server extends Readyable implements Runnable, EventListener {

	private int port = 4242;
	public boolean running = false;
	private Vector<DataConnector> clients = new Vector<DataConnector>();
	private String uPnPHost;
	private int uPnPPort = -1;
	private static Server server = null;
	private ArrayList<Token> tokenTypes = new ArrayList<Token>();
	private GatewayDevice d;
	private Thread shutdownHook;
	PortMappingEntry portMapping;
	int externalport;
	RunnableThread rt;
	Thread connectionThread;
	Timer timer;
	ModSession session;

	public static ModSession getSession() {
		if (server.session == null) {
			server.session = new ModSession();
		}
		return server.session;
	}

	public static void setModSession(ModSession session) {
		server.session = session;
	}

	public static Server getServer() {
		return server;
	}

	public void dumpConnections() {
		System.out.println("------START-----");
		synchronized (clients) {
			for (DataConnector dc : clients) {
				System.out.println(dc.getIP() + " : " + dc.name + " " + dc.identified);
			}
		}
		System.out.println("------END-----");
	}

	/**
	 * Init with a port.
	 * 
	 * @param port
	 */
	public Server(int port) {
		server = this;
		EventDispatcher.addHandler(this);
		this.port = port;
		running = true;
		connectionThread = new Thread(this, "Server Connections thread");
		connectionThread.start();
		initGateway();
		ServerHeartbeat shb = new ServerHeartbeat(this);
		timer = new Timer();
		timer.schedule(shb, 0, 250);
		rt = new RunnableThread(this);
	}

	public void addRunnable(Runnable r) {
		rt.addNewRunnable(r);
	}

	//
	public void run() {
		EventDispatcher.dispatchEvent(new ServerStartingEvent());
		EventDispatcher.dispatchEvent(new PrepareSessionEvent());
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			new DebugExceptionHandler(e);

		}
		HGXMLThread.requestServerRegister(port);
		synchronized (readyLock) {
			ready = true;
			readyLock.notifyAll();
		}
		while (running) {
			Socket clientSocket = null;
			// System.out.println("SERVER : awaiting connection");
			try {
				clientSocket = serverSocket.accept();
				dumpConnections();
			} catch (IOException e) {
				if (!running) {
					System.out.println("Server Stopped.");
					return;
				}
				new DebugExceptionHandler(e, "Error accepting client connection");
			}
			EventDispatcher.dispatchEvent(new ServerConnectionAttempt(clientSocket));
			DataConnector conn = new DataConnector(clientSocket, "Multithreaded Server");
			synchronized (clients) {
				clients.add(conn);
			}

		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		timer.cancel();
		rt.interupt();
		synchronized (clients) {
			for (DataConnector dc : clients) {
				dc.finalDestroy();
			}
		}
		closeGateway();
	}

	public void sendToAllObject(UUID uuid, Packet orop) {
		ArrayList<Connector> copyList;
		synchronized (clients) {
			copyList = new ArrayList<Connector>(clients);
		}
		CommonObject co = getSession().getObjectStorage().getObject(uuid);
		CommonAreaLoc cloc = CommonWorld.getAreaLocFor(co.getLocation());
		for (Connector c : copyList) {
			DataConnector dc = (DataConnector) c;
			ArrayList<CommonAreaLoc> list;
			if (dc.lastSeenAreas == null || dc.lastSeenAreas.size() == 0) {
				// New users won't have a last tick yet. Make one up for now, but don't store it so they still get the first tick full of objects
				list = new ArrayList<CommonAreaLoc>();
				for (PlayerCharacter pc : dc.pcList) {
					CommonLocation l = pc.getLocation();
					if (l == null) {
						continue;
					}
					CommonAreaLoc center = CommonWorld.getAreaLocFor(l);
					for (int x = -1; x < 2; x++) {
						for (int y = -1; y < 2; y++) {
							CommonAreaLoc loc = center.getNeighbour(x, y);
							if (!list.contains(loc)) {
								list.add(loc);
							}
						}
					}
				}
			} else {
				list = dc.lastSeenAreas;
			}
			if (list.contains(cloc)) {
				c.send(orop);
			}
		}
	}

	public void sendAll(DataConnector dConn, Packet p) {
		sendAll(dConn, p, false);
	}

	public void sendAll(DataConnector dConn, Packet p, boolean allowLoopback) {
		ArrayList<Connector> copyList;
		synchronized (clients) {
			copyList = new ArrayList<Connector>(clients);
		}
		// All connected devices
		for (Connector c : copyList) {
			if (allowLoopback == false && c.loopback == false && c == dConn) {
				continue;
			}
			c.send(p);
		}
	}

	public String getExternalIP() {
		if (uPnPHost != null) {
			return uPnPHost;
		}
		return "unknown";
	}

	public int getExternalPort() {
		return uPnPPort;
	}

	public void setExternalIp(String externalAddress, int externalport) {
		uPnPHost = externalAddress;
		uPnPPort = externalport;
	}

	public ArrayList<DataConnector> getConnectionsWithID(String id) {
		ArrayList<DataConnector> newList = new ArrayList<DataConnector>();
		synchronized (clients) {
			for (DataConnector client : clients) {
				if (client.name.equals(id)) {
					newList.add(client);
				}
			}
		}
		return newList;

	}

	public boolean hasConnection(Connector connector) {
		synchronized (clients) {
			for (DataConnector client : clients) {
				if (client.equals(connector)) {
					return true;
				}
			}
		}
		return false;

	}

	public ArrayList<DataConnector> getConnectionsCopy() {
		synchronized (clients) {
			ArrayList<DataConnector> dCList = new ArrayList<DataConnector>(clients);
			return dCList;
		}
	}

	public void dropConnection(DataConnector c) {
		synchronized (clients) {
			clients.remove(c);
			c.finalDestroy();
		}
	}

	public boolean addTokens(File f) {
		if (f.isFile()) {
			try (FileInputStream fis = new FileInputStream(f)) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder;
				builder = dbf.newDocumentBuilder();
				Document d = builder.parse(fis);
				Element list = XML.getChild(d, "tokenlist");
				for (Element e : XML.getChildren(list, "token")) {
					String id = e.getAttribute("id");
					String desc = e.getAttribute("desc");
					String klass = e.getAttribute("class");
					String valueS = e.getAttribute("value");
					int value = 0;
					try {
						value = Integer.parseInt(valueS);
					} catch (NumberFormatException nfe) {
					}
					Token t = new Token(id, desc, value, klass);
					synchronized (tokenTypes) {
						tokenTypes.add(t);
					}
				}
			} catch (FileNotFoundException e) {
				new DebugExceptionHandler(e, f.getAbsolutePath());
			} catch (IOException e) {
				new DebugExceptionHandler(e, f.getAbsolutePath());
			} catch (ParserConfigurationException e) {
				new DebugExceptionHandler(e);
			} catch (SAXException e) {
				new DebugExceptionHandler(e);
			}
			return true;
		}
		return false;
	}

	public void initGateway() {
		d = null;
		portMapping = null;
		externalport = 4242;
		// First we mess about with uPnP
		System.out.println("Discovering uPnP Gateway for internet access to local server");
		try {
			GatewayDiscover discover = new GatewayDiscover();
			discover.discover();
			d = discover.getValidGateway();

			if (d != null) {
				InetAddress localAddress = d.getLocalAddress();
				String externalAddress = d.getExternalIPAddress();
				portMapping = new PortMappingEntry();
				while (d.getSpecificPortMappingEntry(externalport, "TCP", portMapping)) {
					externalport++;
				}

				// External port, Internal port, Internal Host
				if (d.addPortMapping(externalport, 4242, localAddress.getHostAddress(), "TCP", "HiddenGrave")) {
					System.out.println("Bound Local " + localAddress.getHostAddress() + ":" + 4242 + " to External " + externalAddress + ":" + externalport);
					setExternalIp(externalAddress, externalport);
					EventDispatcher.dispatchEvent(new ServerGotExternalHostDetails(externalAddress, externalport));
					HGXMLThread.requestClientCheckIn();
					shutdownHook = new Thread(new ServerShutdownThread(d, externalport));
					Runtime.getRuntime().addShutdownHook(shutdownHook);
					// Force check in with new server data
				} else {
					System.out.println("uPnP Gateway port mapping failed. Cannot host an internet game");
				}
			} else {
				System.out.println("We have no valid uPnP Gateway. Cannot host an internet game");
			}
		} catch (SocketException e) {
			new DebugExceptionHandler(e);
		} catch (UnknownHostException e) {
			new DebugExceptionHandler(e);
		} catch (IOException e) {
			new DebugExceptionHandler(e);
		} catch (SAXException e) {
			new DebugExceptionHandler(e);
		} catch (ParserConfigurationException e) {
			new DebugExceptionHandler(e);
		}
	}

	public void closeGateway() {
		if (d != null && portMapping != null) {
			// Remove port mapping for future use.
			try {
				d.deletePortMapping(externalport, "TCP");
				if (shutdownHook != null) {
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onClientAuthFailed(ServerAuthFailedEvent event) {
		for (DataConnector c : Server.getServer().getConnectionsWithID(event.getID())) {
			if (c.identified == false) {
				c.setDestroy();
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onClientAuth(ServerAuthEvent event) {
		for (DataConnector c : Server.getServer().getConnectionsWithID(event.getId())) {
			if (c.identified == false && c.token.equals(event.getToken())) {
				c.name = event.getUserName();
				// Throw event to ensure newly identified user is not banned by Mods
				EventDispatcher.dispatchEvent(new PlayerJoinedEvent(c.name, event.getId(), c));
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onPlayerJoined(PlayerJoinedEvent event) {
		if (event.getBanReason() == null) {
			// This is it, they're joining the server officially now. Better
			// throw some data at them first and then tell them they logged in
			// fine
			event.getConnection().identified = true;
			event.getConnection().setName("Server to " + event.getConnection().name);

			// Send all loaded worlds
			for (CommonWorld world : getSession().getWorlds()) {
				Packet p = new LoadWorldPacket(world.getName());
				event.getConnection().send(p);
			}
			// Send all ItemTypes

			// TODO Possibly just serialise the whole copy of Server Session?
			// Would make it seriously easy to just dump the whole modded session on the user and
			// not have to worry about anything but updating whatever changes

			Packet p = new PlayerJoinedPacket(event.getPlayerName(), event.getPlayerId());
			Server.getServer().sendAll(event.getConnection(), p);
		} else {
			// TODO Send ban reason
			event.getConnection().setDestroy();
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onClientSaveFilePacket(ClientSaveFilePacket packet) {
		DataConnector dc = (DataConnector) packet.getFrom();
		if (dc.identified) {
			// TODO Read through save-file data, list back players characters that are able to be played with the mod-set of this server
			dc.pcList.add(new PlayerCharacter(dc.name + ":1", new BinaryParent()));
			PlayerAddCharacterOption paco = new PlayerAddCharacterOption();
			paco.characterData = new BinaryParent();
			try {
				paco.characterData.setString("name", "Mr ploppy pants the first");
				paco.characterData.setString("class", "poshtwat");
				paco.characterData.setLocation("location", new CommonLocation());
				paco.characterData.setMaterialList("mat", new MaterialList());
				paco.characterData.setString("model", "mod/craig.dae:Cylinder001");
			} catch (BinaryNodeException e) {
				new DebugExceptionHandler(e);
			}
			dc.send(paco);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onClientChosenCharacter(PlayerCharacterChosen packet) {
		DataConnector dc = (DataConnector) packet.getFrom();
		if (dc.identified) {
			if (packet.choice < dc.pcList.size()) {
				PlayerCharacterSpawn pcs = new PlayerCharacterSpawn();
				PlayerCharacter obj = dc.pcList.get(packet.choice);

				// Since the obj currently isn't from a save file, let's drop sensible defaults in.
				obj.setType(CommonObject.Type.Anim);
				obj.getUUID(); // Pre-fill UUID
				CommonLocation loc = new CommonLocation();
				loc.setScale(0.15f, 0.15f, 0.15f);
				obj.setLocation(loc);
				obj.setModelLabel("mod/craig.dae:Cylinder001");
				getSession().getObjectStorage().addObject(obj);
				pcs.objectLinked = obj.getUUID();
				pcs.playerSpot = 0;
				pcs.data = obj.getData();
				obj.replaceOptions();
				dc.send(pcs);
			}
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onClientServerConnect(ClientAuthPacket packet) {
		System.out.println("Connection packet : " + packet.userID + " " + packet.passKey);
		ErrorPacket ep = new ErrorPacket();
		DataConnector conn = (DataConnector) packet.getFrom();
		ep.error = "Identity data is wrong. Please download a new copy of your launcher";
		if (packet.userID == null || packet.passKey == null) {
			conn.send(ep);
			return;
		}
		conn.identified = false;
		conn.name = packet.userID;
		conn.token = packet.passKey;
		// Send XML request to confirm user identity
		HGXMLThread.requestAuthClientFromServer(packet.userID, packet.passKey);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onConnectionLost(ConnectionLostEvent event) {
		if (Server.getServer().hasConnection(event.getConnector())) {
			DataConnector dc = (DataConnector) event.getConnector();
			dc.identified = false;
			dc.setDestroy();
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onNewOrderGiven(NewOrderPacket packet) {
		if (packet.getFrom() instanceof DataConnector) {
			DataConnector dc = (DataConnector) packet.getFrom();
			dc.addOrder(packet.order);
		}
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onPathSet(ServerObjectSetPathEvent event) {
		if (event.isConsumed()) {
			return;
		}
		if (event.getPathFinder() == null) {
			return;
		}
		event.setConsumed();
		event.getObject().setPath(event.getPathFinder().getPath());
		ObjectPathSetPacket packet = new ObjectPathSetPacket(event.getPathFinder().getPath(), event.getObject().getUUID());
		getServer().sendAll(null, packet);
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onOptionsReplace(OptionReplaceEvent event) {
		ObjectReplaceOptionsPacket orop = new ObjectReplaceOptionsPacket();
		orop.uuid = event.getObjectId();
		orop.mi = event.getMenu();
		getServer().sendToAllObject(orop.uuid, orop);
	}

	public void stop() {
		running = false;
		connectionThread.interrupt();
	}

	public void replaceOptionsAll(UUID id) {
		synchronized (clients) {
			for (DataConnector dC : clients) {
				dC.replaceOptions(id);
			}
		}
	}
}
