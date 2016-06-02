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
package com.opengrave.common.xml;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.ServerData;
import com.opengrave.common.event.*;
import com.opengrave.og.MainThread;
import com.opengrave.og.states.ErrorState;
import com.opengrave.og.states.ProfileState;

public class HGXMLThread implements Runnable, EventListener {

	private static ArrayList<HGXMLRequest> requestList = new ArrayList<HGXMLRequest>();
	public static String authURL;

	public static String sendXMLRequest(String urlString, Document XML) {
		try {

			URL url = new URL(urlString);

			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setSSLSocketFactory(sslSocketFactory);

			// specify that we will send output and accept input
			con.setDoInput(true);
			con.setDoOutput(true);

			con.setConnectTimeout(20000); // long timeout, but not infinite
			con.setReadTimeout(20000);

			con.setUseCaches(false);
			con.setDefaultUseCaches(false);

			// tell the web server what we are sending
			con.setRequestProperty("Content-Type", "text/xml");

			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
			writer.write(docToString(XML));
			writer.flush();
			writer.close();

			// reading the response
			InputStreamReader reader = new InputStreamReader(con.getInputStream());

			StringBuilder buf = new StringBuilder();
			char[] cbuf = new char[2048];
			int num;

			while (-1 != (num = reader.read(cbuf))) {
				buf.append(cbuf, 0, num);
			}

			String result = buf.toString();
			return result;
		} catch (NoRouteToHostException e) {
			new DebugExceptionHandler(e, urlString, docToString(XML));
		} catch (MalformedURLException e) {
			new DebugExceptionHandler(e, urlString, docToString(XML));
		} catch (IOException e) {
			new DebugExceptionHandler(e, urlString, docToString(XML));
		}
		return null;
	}

	@EventHandler(priority = EventHandlerPriority.LATE)
	public void onXMLReturned(XMLReturnedEvent event) {
		Document doc = event.getDocument();
		Element api = XML.getChild(doc, "api");
		if (api == null) {
			System.out.println("Error reading returned XML");
			System.out.println(event.getDocument().toString());
		} else {
			for (Element response : XML.getChildren(api, "response")) {
				String type = response.getAttribute("type");
				Element result = null;
				String mods = "";
				switch (type.toLowerCase()) {
				case "serverstart":
					result = XML.getChild(response, "result");
					boolean wanAccess = false;
					if (result == null || (!result.getTextContent().equalsIgnoreCase("OK"))) {
						// Server did not get outside WAN.
					} else {
						// Server connects on WAN with given IP and port
						wanAccess = true;
					}
					EventDispatcher.dispatchEvent(new ServerStartedEvent(wanAccess));
					break;
				case "clientstarted":
					Element token = XML.getChild(response, "token");
					if (token == null) {
						MainThread.changeState(new ErrorState(
								"Login details are incorrect. Please download and use a fresh copy of the Launcher from the website"));
						break;
					}
					Element modlist = XML.getChild(response, "mods");
					mods = "1";
					if (modlist != null) {
						mods = modlist.getTextContent();
					} else {
						mods = "1";
					}
					String accessToken = token.getTextContent();
					ProfileState.state.isOnline = true;
					ProfileState.state.standing = ProfileState.Standing.UNKNOWN;
					ProfileState.state.accessToken = accessToken;
					ProfileState.state.mods = mods;
					EventDispatcher.dispatchEvent(new ClientAuthStatusEvent(mods));
					break;
				case "clientbug":
					Element bugNumberEle = XML.getChild(response, "bugid");
					Element reportNumberEle = XML.getChild(response, "bugrep");
					if (bugNumberEle == null || reportNumberEle == null) {
						break;
					}
					try {
						int bugId = Integer.parseInt(bugNumberEle.getTextContent());
						int repId = Integer.parseInt(reportNumberEle.getTextContent());
						Desktop.getDesktop().browse(new URI("https://aperistudios.co.uk/bug.php?bug=" + bugId + "#report" + repId));
					} catch (NumberFormatException nfe) {
					} catch (IOException e) {
					} catch (URISyntaxException e) {
					}
					break;
				case "serverlist":
					ArrayList<ServerData> sd = new ArrayList<ServerData>();
					for (Element child : XML.getChildren(response, "server")) {
						String names = child.getAttribute("names");
						String ip = child.getAttribute("ip");
						String port = child.getAttribute("port");
						String id = child.getAttribute("id");
						mods = child.getAttribute("mods");
						ServerData s = new ServerData(names, ip, port, id, mods);
						sd.add(s);
					}
					EventDispatcher.dispatchEvent(new ServerListEvent(sd));
					break;
				case "clientjoin":
					result = XML.getChild(response, "result");
					Element name = XML.getChild(response, "name");
					token = XML.getChild(response, "token");
					Element id = XML.getChild(response, "id");
					if (id == null) {
						break;
					}
					if (token == null || result == null || name == null || result.getTextContent().equalsIgnoreCase("bad")) {
						EventDispatcher.dispatchEvent(new ServerAuthFailedEvent(id.getTextContent()));
						break;
					}
					EventDispatcher.dispatchEvent(new ServerAuthEvent(name.getTextContent(), id.getTextContent(), token.getTextContent()));
					break;
				}
			}
		}
	}

	public static void requestClientCheckIn() {
		String host = "";
		int port = -1;
		String idHost = "-1";
		if (MainThread.getConnection() != null) {
			host = MainThread.getConnection().getHost();
			port = MainThread.getConnection().getPort();
			idHost = MainThread.getConnection().getId();
		}
		System.out.println("Reporting connection as " + host + ":" + port + ":" + idHost);
		Document doc = createDoc();
		Element root = doc.createElement("api");
		doc.appendChild(root);
		Element request = doc.createElement("request");
		root.appendChild(request);
		request.setAttribute("type", "clientping");
		Element login = doc.createElement("userlogin");
		login.setTextContent(MainThread.USERNAME);
		Element password = doc.createElement("userpassword");
		password.setTextContent(MainThread.PASSWORD);
		Element ip = doc.createElement("ip");
		ip.setTextContent(host);
		Element portE = doc.createElement("port");
		portE.setTextContent(port + "");
		Element id = doc.createElement("id");
		id.setTextContent(idHost);
		request.appendChild(login);
		request.appendChild(password);
		request.appendChild(ip);
		request.appendChild(portE);
		String url = "https://api.aperistudios.co.uk/auth/";
		HGXMLThread.addRequest(new HGXMLRequest(url, doc));
	}

	public static void requestServerlist() {
		Document doc = createDoc();
		Element root = doc.createElement("api");
		doc.appendChild(root);
		Element request = doc.createElement("request");
		root.appendChild(request);
		request.setAttribute("type", "serverlist");
		root.appendChild(request);
		HGXMLThread.addRequest(new HGXMLRequest(authURL, doc));
	}

	public static void requestServerRegister(int port) {
		Document doc = createDoc();
		Element root = doc.createElement("api");
		doc.appendChild(root);
		Element request = doc.createElement("request");
		root.appendChild(request);
		request.setAttribute("type", "serverstart");
		Element login = doc.createElement("userlogin");
		login.setTextContent(MainThread.USERNAME);
		Element password = doc.createElement("userpassword");
		password.setTextContent(MainThread.PASSWORD);
		Element ip = doc.createElement("ip");
		ip.setTextContent("None");
		Element portE = doc.createElement("port");
		portE.setTextContent(port + "");
		request.appendChild(login);
		request.appendChild(password);
		request.appendChild(ip);
		request.appendChild(portE);
		HGXMLThread.addRequest(new HGXMLRequest(authURL, doc));
	}

	public static void requestAuthClientFromServer(String userId, String userToken) {
		Document doc = createDoc();
		Element root = doc.createElement("api");
		doc.appendChild(root);
		Element request = doc.createElement("request");
		root.appendChild(request);
		request.setAttribute("type", "clientjoin");
		Element login = doc.createElement("userlogin");
		login.setTextContent(userId);
		Element password = doc.createElement("usertoken");
		password.setTextContent(userToken);
		request.appendChild(login);
		request.appendChild(password);
		HGXMLThread.addRequest(new HGXMLRequest(authURL, doc));
	}

	public static void requestAuthClient() {
		Document doc = createDoc();
		Element root = doc.createElement("api");
		doc.appendChild(root);
		Element request = doc.createElement("request");
		root.appendChild(request);
		request.setAttribute("type", "clientstarted");
		Element login = doc.createElement("userlogin");
		login.setTextContent(MainThread.USERNAME);
		Element password = doc.createElement("userpassword");
		password.setTextContent(MainThread.PASSWORD);
		request.appendChild(login);
		request.appendChild(password);
		HGXMLThread.addRequest(new HGXMLRequest(authURL, doc));
	}

	private static void addRequest(HGXMLRequest hgxmlCallback) {
		synchronized (requestList) {
			requestList.add(hgxmlCallback);
			requestList.notifyAll();
		}
	}

	static String docToString(Document doc) {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans;
		StringWriter sw = null;
		try {
			trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// create string from xml tree
			sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);

		} catch (TransformerConfigurationException e) {
			new DebugExceptionHandler(e);
		} catch (TransformerException e) {
			new DebugExceptionHandler(e);
		}
		return sw != null ? sw.toString() : null;
	}

	private static Document createDoc() {
		try {
			DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
			DocumentBuilder build = dFact.newDocumentBuilder();
			Document doc = build.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
			new DebugExceptionHandler(e);
		}
		return null;
	}

	public HGXMLThread() {
		EventDispatcher.addHandler(this);
	}

	private SSLContext sslContext;
	private static SSLSocketFactory sslSocketFactory;

	@Override
	public void run() {
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

		while (MainThread.running) {
			synchronized (requestList) {
				while (requestList.size() > 0) {

					HGXMLRequest cb = requestList.remove(0); // Earlier first
					String ret = sendXMLRequest(cb.url, cb.originalXml);

					cb.returnXML(ret);
				}
			}
			synchronized (requestList) {
				try {
					requestList.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void requestBugReport(String sTrace, String title) {
		System.out.println("Filing bug report");
		Document doc = createDoc();
		Element root = doc.createElement("api");
		doc.appendChild(root);
		Element request = doc.createElement("request");
		root.appendChild(request);
		request.setAttribute("type", "clientbug");
		Element login = doc.createElement("userlogin");
		login.setTextContent(MainThread.USERNAME);
		Element password = doc.createElement("userpassword");
		password.setTextContent(MainThread.PASSWORD);
		Element strace = doc.createElement("strace");
		strace.setTextContent(sTrace);
		Element prod1 = doc.createElement("prod1");
		prod1.setTextContent("hgjava");
		Element rev1 = doc.createElement("rev1");
		rev1.setTextContent("" + MainThread.hgver);
		Element prod2 = doc.createElement("prod2");
		prod2.setTextContent("hgasset");
		Element rev2 = doc.createElement("rev2");
		rev2.setTextContent("" + MainThread.assetver);
		Element titleE = doc.createElement("title");
		titleE.setTextContent(title);
		request.appendChild(login);
		request.appendChild(password);
		request.appendChild(strace);
		request.appendChild(prod1);
		request.appendChild(prod2);
		request.appendChild(rev1);
		request.appendChild(rev2);
		request.appendChild(titleE);
		String url = "https://api.aperistudios.co.uk/auth/";
		HGXMLThread.addRequest(new HGXMLRequest(url, doc));
	}

}
