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
package com.opengrave.og.models;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.opengrave.common.xml.XML;

public class DAEAnimation {

	String id;
	// ArrayList<DAEAnimChannel> channels = new ArrayList<DAEAnimChannel>();
	public HashMap<String, DAEAnimChannelCollection> channels = new HashMap<String, DAEAnimChannelCollection>();
	String target;

	public DAEAnimation(Node node) {
		id = ((Element) node).getAttribute("id");
		ArrayList<DAEAnimSample> samples = new ArrayList<DAEAnimSample>();
		ArrayList<DAEAnimSource> sources = new ArrayList<DAEAnimSource>();
		for (Element source : XML.getChildren(node, "source")) {
			DAEAnimSource animsource = new DAEAnimSource(source);
			sources.add(animsource);
		}
		for (Element sampler : XML.getChildren(node, "sampler")) {
			DAEAnimSample animsampler = new DAEAnimSample(sampler, sources);
			samples.add(animsampler);
		}
		for (Element channel : XML.getChildren(node, "channel")) {
			DAEAnimChannel animchannel = new DAEAnimChannel(channel, samples);
			String target = channel.getAttribute("target");
			String[] parts = target.split("/");
			if (parts.length == 2) {
				target = parts[0];
				animchannel.target = parts[1];
			}
			if (!channels.containsKey(target)) {
				DAEAnimChannelCollection collection = new DAEAnimChannelCollection();
				collection.target = target;
				this.target = target;
				channels.put(target, collection);
			}
			channels.get(target).add(animchannel);
		}
		// All Channels collected now need to be bunched together into
		// time->matrix info
		for (DAEAnimChannelCollection channel : channels.values()) {
			channel.prepare();
		}
	}
}
