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

import java.util.Collections;
import java.util.LinkedHashMap;
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
	@XmlElement(name = "solo_teleports", required = true)
	@XmlJavaTypeAdapter(SoloTeleportRefListToMap.class)
	public Map<String, SoloTeleport> soloTeleports;
	@XmlElement(name = "party_teleports", required = true)
	@XmlJavaTypeAdapter(GroupTeleportRefListToMap.class)
	public Map<String, GroupTeleport> partyTeleports;
	@XmlElement(name = "command_channel_teleports", required = true)
	@XmlJavaTypeAdapter(GroupTeleportRefListToMap.class)
	public Map<String, GroupTeleport> commandChannelTeleports;
	
	public AbstractTeleporter(String bypassPrefix)
	{
		super(bypassPrefix, "teleporter");
		
		soloTeleports = Collections.unmodifiableMap(new LinkedHashMap<>());
		partyTeleports = Collections.unmodifiableMap(new LinkedHashMap<>());
		commandChannelTeleports = Collections.unmodifiableMap(new LinkedHashMap<>());
	}
	
	@Override
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		super.afterUnmarshal(unmarshaller, parent);
		
		if (!soloTeleports.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("solo_teleports", null).getChild("solo_teleports");
			for (Entry<String, SoloTeleport> soloTeleport : soloTeleports.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), soloTeleport.getValue().placeholder);
			}
		}
		if (!partyTeleports.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("party_teleports", null).getChild("party_teleports");
			for (Entry<String, GroupTeleport> partyTeleport : partyTeleports.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), partyTeleport.getValue().placeholder);
			}
		}
		if (!commandChannelTeleports.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("command_channel_teleports", null).getChild("command_channel_teleports");
			for (Entry<String, GroupTeleport> commandChannelTeleport : commandChannelTeleports.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), commandChannelTeleport.getValue().placeholder);
			}
		}
	}
}
