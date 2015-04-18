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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author HorridoJoho
 */
public class GroupTeleport extends SoloTeleport
{
	@XmlAttribute(name = "min_members", required = true)
	public final int minMembers;
	@XmlAttribute(name = "max_members", required = true)
	public final int maxMembers;
	@XmlAttribute(name = "max_distance")
	public final int maxDistance;
	@XmlAttribute(name = "allow_incomplete")
	public final boolean allowIncomplete;
	
	public GroupTeleport()
	{
		minMembers = 0;
		maxMembers = 0;
		maxDistance = 50;
		allowIncomplete = false;
	}
	
	@Override
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		super.afterUnmarshal(unmarshaller, parent);
		
		placeholder.addChild("min_members", String.valueOf(minMembers)).addChild("max_members", String.valueOf(maxMembers)).addChild("max_distance", String.valueOf(maxDistance));
	}
}
