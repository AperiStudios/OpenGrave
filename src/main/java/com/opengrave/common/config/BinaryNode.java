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

import com.opengrave.common.OGOutputStream;
import com.opengrave.common.MenuInfo;
import com.opengrave.common.inventory.Item;
import com.opengrave.common.world.CommonLocation;
import com.opengrave.common.world.MaterialList;

public interface BinaryNode {

	public BinaryNode getNode(String node) throws BinaryNodeException;

	public boolean isObject();

	public boolean isParentNode();

	public void save(OGOutputStream out) throws IOException, BinaryNodeException;

	public void setString(String node, String value) throws BinaryNodeException;

	public void setInt(String node, Integer value) throws BinaryNodeException;

	public void setLong(String node, Long value) throws BinaryNodeException;

	public void setFloat(String node, Float value) throws BinaryNodeException;

	public void setDouble(String node, Double value) throws BinaryNodeException;

	public void setUUID(String node, UUID value) throws BinaryNodeException;

	public void setLocation(String node, CommonLocation value) throws BinaryNodeException;

	public void setMaterialList(String node, MaterialList matList) throws BinaryNodeException;

	public void setItem(String node, Item item) throws BinaryNodeException;

	public void setList(String node, ArrayList<? extends Object> item) throws BinaryNodeException;

	public void setMenuInfo(String node, MenuInfo mi) throws BinaryNodeException;

	public String getString(String node) throws BinaryNodeException;

	public Integer getInt(String node) throws BinaryNodeException;

	public Long getLong(String node) throws BinaryNodeException;

	public Float getFloat(String node) throws BinaryNodeException;

	public Double getDouble(String node) throws BinaryNodeException;

	public UUID getUUID(String node) throws BinaryNodeException;

	public CommonLocation getLocation(String node) throws BinaryNodeException;

	public MaterialList getMaterialList(String node) throws BinaryNodeException;

	public Item getItem(String node) throws BinaryNodeException;

	public MenuInfo getMenuInfo(String node) throws BinaryNodeException;

	public <T extends Object> ArrayList<T> getList(String node, Class<? extends T> type) throws BinaryNodeException;
}
