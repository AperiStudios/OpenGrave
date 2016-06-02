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

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.event.EventDispatcher;
import com.opengrave.common.event.XMLReturnedEvent;

public class HGXMLRequest {

	String url;
	Document originalXml;

	public HGXMLRequest(String url, Document xml) {
		this.url = url;
		this.originalXml = xml;
	}

	public void returnXML(String xml) {
		if (xml == null) {
			return;
		}
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = dbf.newDocumentBuilder();
			Document d = builder.parse(new InputSource(new StringReader(xml)));
			EventDispatcher.dispatchEvent(new XMLReturnedEvent(d, HGXMLThread.docToString(this.originalXml)));
		} catch (ParserConfigurationException e) {
			new DebugExceptionHandler(e, xml);
		} catch (SAXException e) {
			new DebugExceptionHandler(e, xml);
		} catch (IOException e) {
			new DebugExceptionHandler(e, xml);
		}
	}

}
