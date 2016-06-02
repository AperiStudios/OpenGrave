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

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.og.gui.*;
import com.opengrave.og.gui.ElementData.PositionTypeX;
import com.opengrave.og.gui.ElementData.PositionTypeY;
import com.opengrave.og.util.Vector4f;

public class GUIXML {

	public static HashMap<String, Class<? extends UIElement>> types = new HashMap<String, Class<? extends UIElement>>();
	private String fileName;
	private UnorderedContainer parent;
	private Vector4f defaultColour = new Vector4f(1f, 1f, 1f, 1f), activeColour = new Vector4f(1f, 1f, 1f, 1f), disabledColour = new Vector4f(1f, 1f, 1f, 1f),
			textColour = new Vector4f(0f, 0f, 0f, 1f);
	private boolean ignoreContainer = false;

	public UnorderedContainer getGUI() {
		return parent;
	}

	public GUIXML(String fileName) {
		this.fileName = fileName;
		this.parent = new UnorderedContainer(new ElementData());
		build();
	}

	public GUIXML(String fileName, ElementData ed) {
		ignoreContainer = true;
		this.fileName = fileName;
		this.parent = new UnorderedContainer(ed);
		build();
	}

	private void build() {
		String contents = Resources.loadTextFile(fileName);
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = dbf.newDocumentBuilder();
			doc = builder.parse(new InputSource(new StringReader(contents)));
		} catch (ParserConfigurationException e) {
			new DebugExceptionHandler(e, fileName);
		} catch (SAXException e) {
			new DebugExceptionHandler(e, fileName);
		} catch (IOException e) {
			new DebugExceptionHandler(e, fileName);
		}
		Node container = doc.getChildNodes().item(0);
		if (!ignoreContainer) {
			doGlobal(container);
		} else {
			defaultColour = parent.getElementData().activeColour;
			defaultColour = parent.getElementData().defaultColour;
			disabledColour = parent.getElementData().disabledColour;
			textColour = parent.getElementData().textColour;
		}
		doChildrenFor(parent, container);
		if (!ignoreContainer) {
			parent.getElementData().activeColour = activeColour;
			parent.getElementData().defaultColour = defaultColour;
			parent.getElementData().disabledColour = disabledColour;
			parent.getElementData().textColour = textColour;
		}
	}

	private void doGlobal(Node container) {
		NamedNodeMap attribs = container.getAttributes();
		if (attribs != null) {
			for (int i = 0; i < attribs.getLength(); i++) {
				Attr a = (Attr) attribs.item(i);
				String key = a.getNodeName();
				String val = a.getNodeValue();
				switch (key.toLowerCase()) {
				case "colour":
					defaultColour = getColour(val);
					break;
				case "activecolour":
					activeColour = getColour(val);
					break;
				case "disabledcolour":
					disabledColour = getColour(val);
					break;
				case "textcolour":
					textColour = getColour(val);
					break;
				}
			}
		}
	}

	private void doChildrenFor(UIParent parent, Node parentNode) {
		NodeList nodeList = parentNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String nodeType = node.getNodeName();
			if (nodeType.equalsIgnoreCase("include")) {
				NamedNodeMap attribs = node.getAttributes();
				String fileName = "";
				for (int c = 0; c < attribs.getLength(); c++) {
					Attr a = (Attr) attribs.item(c);
					String key = a.getNodeName();
					String val = a.getNodeValue();
					if (key.equalsIgnoreCase("file")) {
						fileName = val;
					}
				}
				if (fileName.length() > 1) {
					ElementData ed = new ElementData();
					ed.defaultColour = this.defaultColour;
					ed.activeColour = this.activeColour;
					ed.disabledColour = this.disabledColour;
					ed.textColour = this.textColour;
					GUIXML included = new GUIXML(fileName, ed);
					for (UIElement e : included.getGUI().getChildren()) {
						parent.addChildEnd(e);
					}
				}
			} else if (types.containsKey(nodeType)) {
				UIElement ele = null;
				ElementData ed = new ElementData();
				ed.defaultColour = this.defaultColour;
				ed.activeColour = this.activeColour;
				ed.disabledColour = this.disabledColour;
				ed.textColour = this.textColour;
				try {
					ele = types.get(nodeType).getConstructor(ElementData.class).newInstance(ed);
				} catch (InstantiationException e) {
					new DebugExceptionHandler(e, nodeType);
				} catch (IllegalAccessException e) {
					new DebugExceptionHandler(e, nodeType);
				} catch (IllegalArgumentException e) {
					new DebugExceptionHandler(e, nodeType);
				} catch (InvocationTargetException e) {
					new DebugExceptionHandler(e, nodeType);
				} catch (NoSuchMethodException e) {
					new DebugExceptionHandler(e, nodeType);
				} catch (SecurityException e) {
					new DebugExceptionHandler(e, nodeType);
				}
				if (ele != null) {
					setAttributes(ele, node);
					if (parent != null) {
						parent.addChildEnd(ele);
					}
					if (ele instanceof UIParent) {
						doChildrenFor((UIParent) ele, node);
					}
				}
			}
		}
	}

	private void setAttributes(UIElement ele, Node info) {
		NamedNodeMap attribs = info.getAttributes();
		ElementData ed = ele.getElementData();

		if (attribs != null) {
			for (int i = 0; i < attribs.getLength(); i++) {
				Attr a = (Attr) attribs.item(i);
				String key = a.getNodeName();
				String val = a.getNodeValue();
				ed.attributes.put(key.toLowerCase(), val);
				switch (key.toLowerCase()) {
				case "minimumheight":
					ed.minimum_height = Integer.parseInt(val);
					break;
				case "minimumwidth":
					ed.minimum_width = Integer.parseInt(val);
					break;
				case "maximumheight":
					ed.maximum_height = Integer.parseInt(val);
					break;
				case "maximumwidth":
					ed.maximum_width = Integer.parseInt(val);
					break;
				case "positionx":
					ed.x = Integer.parseInt(val);
					break;
				case "positiony":
					ed.y = Integer.parseInt(val);
					break;
				case "colour":
					ed.defaultColour = getColour(val);
					break;
				case "activecolour":
					ed.activeColour = getColour(val);
					break;
				case "disabledcolour":
					ed.disabledColour = getColour(val);
					break;
				case "textcolour":
					ed.textColour = getColour(val);
					break;
				case "id":
					ed.id = val;
					break;
				case "text":
					if (ele instanceof TextInterface) {
						TextInterface ti = (TextInterface) ele;
						ti.setString(val);
					}
					break;
				case "posxanchor":
					ed.positionTypeX = getPositioningTypeX(val);
					break;
				case "posyanchor":
					ed.positionTypeY = getPositioningTypeY(val);
					break;
				}
			}
			ele.attributesChanged();
		}
	}

	private PositionTypeX getPositioningTypeX(String s) {
		if (s.equalsIgnoreCase("fixed")) {
			return PositionTypeX.FIXED;
		} else if (s.equalsIgnoreCase("center")) {
			return PositionTypeX.CENTER;
		} else if (s.equalsIgnoreCase("left")) {
			return PositionTypeX.LEFT;
		} else if (s.equalsIgnoreCase("right")) {
			return PositionTypeX.RIGHT;
		}
		return PositionTypeX.FIXED;
	}

	private PositionTypeY getPositioningTypeY(String s) {
		if (s.equalsIgnoreCase("fixed")) {
			return PositionTypeY.FIXED;
		} else if (s.equalsIgnoreCase("center")) {
			return PositionTypeY.CENTER;
		} else if (s.equalsIgnoreCase("top")) {
			return PositionTypeY.TOP;
		} else if (s.equalsIgnoreCase("bottom")) {
			return PositionTypeY.BOTTOM;
		}
		return PositionTypeY.FIXED;
	}

	public static Vector4f getColour(String val) {
		if (!val.substring(0, 1).equals("#")) {
			return new Vector4f(1f, 1f, 1f, 1f);
		}
		float r = 1f, g = 1f, b = 1f, a = 1f;
		String rS = "f", gS = "f", bS = "f", aS = "f";
		float order = 1f;
		if (val.length() == 4) {
			// # and 1 byte each. Only RGB
			rS = val.substring(1, 2);
			gS = val.substring(2, 3);
			bS = val.substring(3, 4);
			aS = "f";
			order = 15f;
		} else if (val.length() == 5) {
			// # and 1 byte each. RGBA
			rS = val.substring(1, 2);
			gS = val.substring(2, 3);
			bS = val.substring(3, 4);
			aS = val.substring(4, 5);
			order = 15f;
		} else if (val.length() == 7) {
			// # and 2 bytes each, RRGGBB, html style
			rS = val.substring(1, 3);
			gS = val.substring(3, 5);
			bS = val.substring(5, 7);
			aS = "ff";
			order = 255f;
		} else if (val.length() == 9) {
			// # and 2 bytes each, RRGGBBAA
			rS = val.substring(1, 3);
			gS = val.substring(3, 5);
			bS = val.substring(5, 7);
			aS = val.substring(7, 9);
			order = 255f;
		} else {
			System.out.println("Unknown colour value : " + val);
			return new Vector4f(1f, 1f, 1f, 1f);
		}
		r = Integer.decode("0x" + rS) / order;
		g = Integer.decode("0x" + gS) / order;
		b = Integer.decode("0x" + bS) / order;
		a = Integer.decode("0x" + aS) / order;
		return new Vector4f(r, g, b, a);
	}

	public static void init() {
		types.put("main", UnorderedContainer.class);
		types.put("button", TextButton.class);
		types.put("scrollbox", ScrollBox.class);
		types.put("textinput", TextInput.class);
		types.put("numberinput", NumberRoller.class);
		types.put("vert", VerticalContainer.class);
		types.put("hori", HorizontalContainer.class);
		types.put("textarea", TextArea.class);
		types.put("image", Image.class);
		types.put("checkbox", CheckButton.class);
		types.put("floatinput", FloatNumberRoller.class);
		types.put("stringinput", StringRoller.class);
		types.put("checkgroup", CheckButtonGroupVertical.class);
		types.put("vector3", VectorInput3.class);
		types.put("sceneview", SceneView.class);
		types.put("scrollable", Scrollable.class);
		types.put("inputicon", ImageInput.class);
	}
}
