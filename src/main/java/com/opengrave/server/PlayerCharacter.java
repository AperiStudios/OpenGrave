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

import java.util.ArrayList;

import com.opengrave.common.config.BinaryNodeException;
import com.opengrave.common.config.BinaryParent;
import com.opengrave.common.inventory.Item;
import com.opengrave.server.combat.CombatObject;
import com.opengrave.server.combat.DamageType;
import com.opengrave.server.exptoken.Token;

public class PlayerCharacter extends CombatObject {
	public PlayerCharacter(String id, BinaryParent binaryParent) {
		super();
		data = binaryParent;
		setIdentifier(id);
	}

	public Item getHelmet() {
		try {
			return data.getItem("helm");
		} catch (BinaryNodeException e) {
			return null;
		}
	}

	public Item getTorso() {
		try {
			return data.getItem("torso");
		} catch (BinaryNodeException e) {
			return null;
		}
	}

	public Item getLegs() {
		try {
			return data.getItem("legs");
		} catch (BinaryNodeException e) {
			return null;
		}
	}

	public Item getRightHand() {
		try {
			return data.getItem("right");
		} catch (BinaryNodeException e) {
			return null;
		}
	}

	public Item getLeftHand() {
		try {
			return data.getItem("left");
		} catch (BinaryNodeException e) {
			return null;
		}
	}

	public ArrayList<Item> getInventory() {
		try {
			return data.getList("inventory", Item.class);
		} catch (BinaryNodeException e) {
			return null;
		}
	}

	BinaryParent data;

	// Only Transient non-saved data should be kept here, All save-data should be kept in data block
	ArrayList<Token> tokensCarrying = new ArrayList<Token>();

	@Override
	public float getSoak(DamageType type) {
		// TODO Implement reading equipment stats
		return 0;
	}

	@Override
	public float getReduction(DamageType type) {
		// TODO Implement reading equipment stats
		return 0;
	}
}
