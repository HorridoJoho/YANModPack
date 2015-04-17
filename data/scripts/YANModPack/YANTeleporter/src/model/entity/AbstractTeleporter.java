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

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import YANModPack.YANTeleporter.src.model.adapter.GroupTeleportRefListToMap;
import YANModPack.YANTeleporter.src.model.adapter.SoloTeleportRefListToMap;
import YANModPack.src.model.entity.YANModServer;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public abstract class AbstractTeleporter extends YANModServer
{
	@XmlElement(name = "solo_teleport_locations", required = true)
	@XmlJavaTypeAdapter(SoloTeleportRefListToMap.class)
	public Map<String, SoloTeleport> soloLocs;
	@XmlElement(name = "party_teleport_locations", required = true)
	@XmlJavaTypeAdapter(GroupTeleportRefListToMap.class)
	public Map<String, GroupTeleport> partyLocs;
	@XmlElement(name = "command_channel_teleport_locations", required = true)
	@XmlJavaTypeAdapter(GroupTeleportRefListToMap.class)
	public Map<String, GroupTeleport> commandChannelLocs;
	
	public AbstractTeleporter(String bypassPrefix)
	{
		super(bypassPrefix, "teleporter");
		
		soloLocs = null;
		partyLocs = null;
		commandChannelLocs = null;
	}
	
	@Override
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		super.afterUnmarshal(unmarshaller, parent);
		
		if (!soloLocs.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("solo_locs", null).getChild("solo_locs");
			for (Entry<String, SoloTeleport> soloLoc : soloLocs.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), soloLoc.getValue().placeholder);
			}
		}
		if (!partyLocs.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("party_locs", null).getChild("party_locs");
			for (Entry<String, GroupTeleport> partyLoc : partyLocs.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), partyLoc.getValue().placeholder);
			}
		}
		if (!commandChannelLocs.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("command_channel_locs", null).getChild("command_channel_locs");
			for (Entry<String, GroupTeleport> commandChannelLoc : commandChannelLocs.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), commandChannelLoc.getValue().placeholder);
			}
		}
	}
}
