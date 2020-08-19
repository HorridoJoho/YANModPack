/*
 * This file is part of YANModPack: https://github.com/HorridoJoho/YANModPack
 * Copyright (C) 2015  Christian Buck
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package YANModPack.YANTeleporter.src.model.entity;

import YANModPack.YANTeleporter.src.model.TeleporterConfig;

/**
 * @author HorridoJoho
 */
public class GroupTeleport extends SoloTeleport
{
	private int minMembers;
	private int maxMembers;
	private int maxDistance;
	private boolean allowIncomplete;
	
	public GroupTeleport()
	{
		minMembers = 0;
		maxMembers = 0;
		maxDistance = 50;
		allowIncomplete = false;
	}
	
	public void afterDeserialize(TeleporterConfig config)
	{
		super.afterDeserialize();
		
		placeholder.addChild("min_members", String.valueOf(minMembers)).addChild("max_members", String.valueOf(maxMembers)).addChild("max_distance", String.valueOf(maxDistance));
	}
	
	public int getMinMembers() {return minMembers;}
	public int getMaxMembers() {return maxMembers;}
	public int getMaxDistance() {return maxDistance;}
	public boolean getAllowIncomplete() {return allowIncomplete;}
}
