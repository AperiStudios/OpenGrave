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

import java.util.ArrayList;

import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opengrave.common.DebugExceptionHandler;

public class XML {

	public static Element getElementById(Document document, String id) {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//*[@id = '" + id + "']");
			Element result = (Element) expr.evaluate(document, XPathConstants.NODE);
			return result;
		} catch (XPathExpressionException e) {
			new DebugExceptionHandler(e);
			return null;
		}
	}

	public static ArrayList<Element> getChildren(Node parent, String tag) {
		ArrayList<Element> children = new ArrayList<Element>();
		NodeList nodes = parent.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equalsIgnoreCase(tag)) {
				children.add((Element) node);
			}
		}
		return children;
	}

	public static Element getChild(Node parent, String tag) {
		ArrayList<Element> children = getChildren(parent, tag);
		if (children.size() == 0) {
			return null;
		}
		return children.get(0);
	}

	public static int getChildCount(Element parent, String string) {
		int i = 0;
		NodeList nodes = parent.getChildNodes();
		for (int j = 0; j < nodes.getLength(); j++) {
			Node node = nodes.item(j);
			if (node.getNodeName().equalsIgnoreCase(string)) {
				i++;
			}
		}
		return i;
	}
}
