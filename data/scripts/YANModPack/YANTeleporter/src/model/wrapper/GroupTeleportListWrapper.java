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
package YANModPack.YANTeleporter.src.model.wrapper;

import javax.xml.bind.annotation.XmlElement;

import YANModPack.YANTeleporter.src.model.entity.GroupTeleport;
import YANModPack.src.model.wrapper.IListWrapper;

/**
 * @author HorridoJoho
 */
public class GroupTeleportListWrapper implements IListWrapper<GroupTeleport>
{
	@XmlElement(name = "group_teleport")
	private final GroupTeleport[] defs;
	
	public GroupTeleportListWrapper()
	{
		defs = new GroupTeleport[0];
	}
	
	public GroupTeleportListWrapper(GroupTeleport[] defs)
	{
		this.defs = defs;
	}
	
	@Override
	public GroupTeleport[] getList()
	{
		return defs;
	}
}
