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
package com.opengrave.common.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.MenuInfo;
import com.opengrave.common.inventory.Item;
import com.opengrave.common.world.CommonLocation;
import com.opengrave.common.world.MaterialList;

public class BinaryParent implements BinaryNode {
	public static ArrayList<Class<?>> types = null;

	HashMap<String, BinaryNode> children = new HashMap<String, BinaryNode>();

	public BinaryParent(OGInputStream input) throws IOException, BinaryObjectWrongType {
		prepTypes();
		int count = input.readInt();
		for (int i = 0; i < count; i++) {
			String key = input.readString();
			int valueType = input.readInt();
			if (valueType == 0) {
				// Another Parent
				BinaryParent child = new BinaryParent(input);
				children.put(key, child);
			} else if (valueType == 1) {
				BinaryObject child = new BinaryObject(input);
				children.put(key, child);
			}
		}
	}

	@Override
	public void save(OGOutputStream out) throws IOException, BinaryNodeException {
		// TODO Prune out dead objects (null) and any parents without objects
		ArrayList<String> deadKeys = new ArrayList<String>();
		for (String key : children.keySet()) {
			BinaryNode child = children.get(key);
			if (child instanceof BinaryObject) {
				BinaryObject childObj = (BinaryObject) child;
				if (childObj.getRaw() == null) {
					deadKeys.add(key);
				}
			}
		}
		for (String key : deadKeys) {
			children.remove(key);
		}
		out.writeInt(children.size());
		for (String key : children.keySet()) {
			out.writeString(key);
			BinaryNode child = children.get(key);
			if (child instanceof BinaryParent) {
				out.writeInt(0);
			} else {
				out.writeInt(1);
			}
			child.save(out);
		}
	}

	public BinaryParent() {
		prepTypes();
	}

	public BinaryParent(LuaTable lua) {
		for (LuaValue key : lua.keys()) {
			LuaValue val = lua.get(key);
			if (val.istable()) {
				BinaryParent p = new BinaryParent((LuaTable) val);
				this.setParent(key.toString(), p);
			} else {

			}
		}
	}

	private void setParent(String node, BinaryParent p) {
		if (node.indexOf(".") == -1) {
			children.put(node, p);
		}
	}

	@Override
	public BinaryNode getNode(String node) throws BinaryNodeException {
		if (node == null || node.equals("")) {
			return this;
		}
		String[] split = node.split("\\.");
		if (children.containsKey(split[0])) {
			BinaryNode child = children.get(split[0]);
			int firstDot = node.indexOf(".");
			String remainder = node.substring(firstDot + 1);
			if (remainder.indexOf(".") == -1) {
				return child;
			}
			return child.getNode(remainder);
		} else {
			// This node doesn't exist... yet

			int firstDot = node.indexOf(".");
			String remainder = node.substring(firstDot + 1);
			if (remainder.indexOf(".") == -1) {
				// No more dots - this should be a value of some sort.
				BinaryNode child = new BinaryObject();
				children.put(split[0], child);
				return child;
			} else {
				// Still another node, let's assume this should be a parent
				BinaryNode child = new BinaryParent();
				children.put(split[0], child);
				return child.getNode(remainder);
			}
		}
		// throw new BinaryNodeDoesNotExistException();
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public boolean isParentNode() {
		return true;
	}

	@Override
	public void setString(String node, String value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setString("", value);
	}

	@Override
	public void setInt(String node, Integer value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setInt("", value);
	}

	@Override
	public void setLong(String node, Long value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setLong("", value);
	}

	@Override
	public void setFloat(String node, Float value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setFloat("", value);
	}

	@Override
	public void setDouble(String node, Double value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setDouble("", value);
	}

	@Override
	public void setUUID(String node, UUID value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setUUID("", value);
	}

	@Override
	public void setLocation(String node, CommonLocation value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setLocation("", value);
	}

	@Override
	public void setMaterialList(String node, MaterialList value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setMaterialList("", value);
	}

	@Override
	public void setMenuInfo(String node, MenuInfo info) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setMenuInfo("", info);
	}

	@Override
	public String getString(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getString("");
	}

	@Override
	public Integer getInt(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getInt("");
	}

	@Override
	public Long getLong(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getLong("");
	}

	@Override
	public Float getFloat(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getFloat("");
	}

	@Override
	public Double getDouble(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getDouble("");
	}

	@Override
	public UUID getUUID(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getUUID("");
	}

	@Override
	public CommonLocation getLocation(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getLocation("");
	}

	@Override
	public MaterialList getMaterialList(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getMaterialList("");
	}

	@Override
	public MenuInfo getMenuInfo(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getMenuInfo("");
	}

	private void prepTypes() {
		if (types == null) {
			types = new ArrayList<Class<?>>();
			types.add(Integer.class);
			types.add(Long.class);
			types.add(Float.class);
			types.add(Double.class);
			types.add(String.class);
			types.add(UUID.class);
			types.add(CommonLocation.class);
			types.add(MaterialList.class);
			types.add(ArrayList.class);
			types.add(Item.class);
			types.add(MenuInfo.class);
		}
	}

	@Override
	public void setItem(String node, Item item) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setItem("", item);
	}

	@Override
	public Item getItem(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getItem("");
	}

	@Override
	public void setList(String node, ArrayList<? extends Object> item) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		checknode.setList("", item);
	}

	@Override
	public <T extends Object> ArrayList<T> getList(String node, Class<? extends T> type) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		return checknode.getList("", type);
	}

}
