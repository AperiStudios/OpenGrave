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
package com.opengrave.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.opengrave.common.inventory.ItemMaterial;
import com.opengrave.common.inventory.ItemType;
import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.CommonProcess;
import com.opengrave.common.world.CommonWorld;
import com.opengrave.common.world.ProcessProvision;

public class ModSession {

	boolean loadFinished = false;
	ArrayList<ItemMaterial> materialList = new ArrayList<ItemMaterial>();
	ArrayList<ItemType> itemTypes = new ArrayList<ItemType>();
	ArrayList<CommonWorld> worldList = new ArrayList<CommonWorld>();
	ArrayList<CommonProcess> processList = new ArrayList<CommonProcess>();
	HashMap<UUID, ArrayList<ProcessProvision>> processHash = new HashMap<UUID, ArrayList<ProcessProvision>>();
	ObjectStorage objectList = new ObjectStorage();

	public CommonProcess addProcess(String processName, ArrayList<String> vars) {
		CommonProcess process = new CommonProcess(processName, vars);
		synchronized (processList) {
			if (!processList.contains(process)) {
				processList.add(process);
			}
		}
		return process;
	}

	public CommonProcess getProcess(String processName) {
		synchronized (processList) {
			for (CommonProcess proc : processList) {
				if (proc.getProcessName().equalsIgnoreCase(processName)) {
					return proc;
				}
			}
		}
		return null;
	}

	public void addObjectProcess(UUID id, CommonProcess proc, ArrayList<Float> vars) {
		CommonObject obj = getObjectStorage().getObject(id);
		if (proc == null) {
			System.out.println("No process : '" + proc + "'");
			return;
		}
		if (obj == null) {
			System.out.println("No object with id " + id.toString());
			return;
		}
		if (vars.size() != proc.getVariables().size()) {
			System.out.println("Variables for process don't match.");
			return;
		}
		synchronized (processHash) {
			if (!processHash.containsKey(id)) {
				processHash.put(id, new ArrayList<ProcessProvision>());
			}
			ProcessProvision pp = new ProcessProvision(proc, vars);
			processHash.get(id).add(pp);
		}
	}

	public ArrayList<ProcessProvision> getProcessObject(UUID id) {
		ArrayList<ProcessProvision> copyList = new ArrayList<ProcessProvision>();
		synchronized (processHash) {
			if (processHash.containsKey(id)) {
				for (ProcessProvision prov : processHash.get(id)) {
					copyList.add(prov);
				}
			}
		}
		return copyList;
	}

	public void add(ItemMaterial im) {
		if (loadFinished) {
			throw new RuntimeException("Can not add new materials after mods finish loading");
		}
		synchronized (materialList) {
			materialList.add(im);
		}
	}

	public void add(ItemType itemType) {
		if (loadFinished) {
			throw new RuntimeException("Can not add new item types after mods finish loading");
		}
		synchronized (itemTypes) {
			itemTypes.add(itemType);
		}
	}

	public ItemType getItemType(String id) {
		synchronized (itemTypes) {
			for (ItemType it : itemTypes) {
				if (id.equalsIgnoreCase(it.getID())) {
					return it;
				}
			}
		}
		return null;
	}

	public ArrayList<ItemMaterial> getMaterials() {
		ArrayList<ItemMaterial> nM = new ArrayList<ItemMaterial>();
		synchronized (materialList) {
			for (ItemMaterial iM : materialList) {
				nM.add(iM);
			}
		}
		return nM;
	}

	public ArrayList<ItemType> getItemTypes() {
		ArrayList<ItemType> iT = new ArrayList<ItemType>();
		synchronized (itemTypes) {
			for (ItemType it : itemTypes) {
				iT.add(it);
			}
		}
		return iT;
	}

	public CommonWorld addWorld(String string) {
		if (loadFinished) {
			throw new RuntimeException("Can not add new worlds after mods finish loading");
		}
		CommonWorld world = new CommonWorld(string);
		synchronized (worldList) {
			worldList.add(world);
		}
		world.loadInThread(this);
		return world;
	}

	public ArrayList<CommonWorld> getWorlds() {
		ArrayList<CommonWorld> newList = new ArrayList<CommonWorld>();
		synchronized (worldList) {
			for (CommonWorld world : worldList) {
				newList.add(world);
			}
		}
		return newList;
	}

	public ObjectStorage getObjectStorage() {
		return objectList;
	}
}
