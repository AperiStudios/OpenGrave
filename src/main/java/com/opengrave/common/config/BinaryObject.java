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
import java.util.UUID;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.MenuInfo;
import com.opengrave.common.inventory.Item;
import com.opengrave.common.world.CommonLocation;
import com.opengrave.common.world.MaterialList;

/**
 * BinaryObjects are an end-point node. They have one value, and no children
 * 
 * @author triggerhapp
 * 
 */
public class BinaryObject implements BinaryNode {
	Object value = null;

	public BinaryObject(OGInputStream input) throws IOException, BinaryObjectWrongType {
		int typeOf = input.readInt();
		Class<?> klass = BinaryParent.types.get(typeOf);
		value = input.readObjectOfClass(klass);
	}

	@Override
	public void save(OGOutputStream out) throws IOException, BinaryNodeException {
		if (value == null) {
			throw new BinaryObjectWrongType("null");
		}
		int typeOf = BinaryParent.types.indexOf(value.getClass());
		if (typeOf != -1) {
			out.writeInt(typeOf);
			out.writeObject(value);
		} else {
			new DebugExceptionHandler(new BinaryObjectWrongType(value.getClass().getSimpleName()), value, value.getClass());
		}
	}

	public BinaryObject() {
	}

	@Override
	public BinaryNode getNode(String node) throws BinaryNodeException {
		if (node == null || node.equalsIgnoreCase("")) {
			return this;
		}
		throw (new BinaryNodeDoesNotExistException());
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public boolean isParentNode() {
		return false;
	}

	@Override
	public void setString(String node, String value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof String) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setInt(String node, Integer value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof Integer) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setLong(String node, Long value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof Long) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setFloat(String node, Float value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof Float) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setDouble(String node, Double value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof Double) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setUUID(String node, UUID value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof UUID) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setLocation(String node, CommonLocation value) throws BinaryNodeException {
		value = new CommonLocation(value); // Forcefully cast down to CommonLocation instead of Client-only Location
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof CommonLocation) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setMaterialList(String node, MaterialList value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof MaterialList) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setMenuInfo(String node, MenuInfo info) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof MenuInfo) {
				this.value = info;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public String getString(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof String) {
				return (String) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public Integer getInt(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof Integer) {
				return (Integer) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public Long getLong(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof Long) {
				return (Long) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public Float getFloat(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof Float) {
				return (Float) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public Double getDouble(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof Double) {
				return (Double) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public UUID getUUID(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof UUID) {
				return (UUID) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public CommonLocation getLocation(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof CommonLocation) {
				return (CommonLocation) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public MaterialList getMaterialList(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof MaterialList) {
				return (MaterialList) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	public Object getRaw() {
		return value;
	}

	@Override
	public MenuInfo getMenuInfo(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof MenuInfo) {
				return (MenuInfo) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setItem(String node, Item value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof Item) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public Item getItem(String node) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof Item) {
				return (Item) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@Override
	public void setList(String node, ArrayList<? extends Object> value) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null || this.value instanceof ArrayList) {
				this.value = value;
				return;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ArrayList<T> getList(String node, Class<? extends T> type) throws BinaryNodeException {
		BinaryNode checknode = getNode(node);
		if (checknode == this) {
			if (this.value == null) {
				return null;
			}
			if (this.value instanceof ArrayList) {
				return (ArrayList<T>) value;
			}
		}
		throw new BinaryObjectWrongType(value.getClass().getSimpleName());
	}
}
