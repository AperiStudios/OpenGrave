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
package com.opengrave.common.event;

import java.util.UUID;

import com.opengrave.common.world.CommonProcess;
import com.opengrave.common.world.ProcessProvision;

public class ProcessEvent extends Event {

	private CommonProcess proc;
	private UUID id;
	private ProcessProvision prov;
	private UUID player;

	public ProcessEvent(UUID player, CommonProcess proc, UUID id, ProcessProvision prov) {
		this.player = player;
		this.proc = proc;
		this.id = id;
		this.prov = prov;
	}

	public CommonProcess getProcessDescription() {
		return proc;
	}

	public UUID getObjectID() {
		return id;
	}

	public ProcessProvision prov() {
		return prov;
	}

	public UUID getPlayerID() {
		return player;
	}

	@Override
	public String getEventName() {
		return "process";
	}

}
