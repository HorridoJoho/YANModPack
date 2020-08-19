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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import YANModPack.YANTeleporter.src.model.TeleporterConfig;
import YANModPack.src.model.entity.YANModServer;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public abstract class AbstractTeleporter extends YANModServer
{
	private List<String> soloTeleports;
	private List<String> partyTeleports;
	private List<String> commandChannelTeleports;
	
	public transient Map<String, SoloTeleport> soloTeleportsMap;
	public transient Map<String, GroupTeleport> partyTeleportsMap;
	public transient Map<String, GroupTeleport> commandChannelTeleportsMap;
	
	public AbstractTeleporter(String bypassPrefix)
	{
		super(bypassPrefix, "teleporter");
		
		soloTeleportsMap = new LinkedHashMap<>();
		partyTeleportsMap = new LinkedHashMap<>();
		commandChannelTeleportsMap = new LinkedHashMap<>();
	}
	
	public void afterDeserialize(TeleporterConfig config)
	{
		super.afterDeserialize();
		
		for (String id : soloTeleports)
		{
			soloTeleportsMap.put(id, config.getGlobal().getSoloTeleports().get(id));
		}
		for (String id : partyTeleports)
		{
			partyTeleportsMap.put(id, config.getGlobal().getGroupTeleports().get(id));
		}
		for (String id : commandChannelTeleports)
		{
			commandChannelTeleportsMap.put(id, config.getGlobal().getGroupTeleports().get(id));
		}
		
		if (!soloTeleports.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("solo_teleports", null).getChild("solo_teleports");
			for (Entry<String, SoloTeleport> soloTeleport : soloTeleportsMap.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), soloTeleport.getValue().getPlaceholder());
			}
		}
		if (!partyTeleports.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("party_teleports", null).getChild("party_teleports");
			for (Entry<String, GroupTeleport> partyTeleport : partyTeleportsMap.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), partyTeleport.getValue().getPlaceholder());
			}
		}
		if (!commandChannelTeleports.isEmpty())
		{
			HTMLTemplatePlaceholder telePlaceholder = placeholder.addChild("command_channel_teleports", null).getChild("command_channel_teleports");
			for (Entry<String, GroupTeleport> commandChannelTeleport : commandChannelTeleportsMap.entrySet())
			{
				telePlaceholder.addAliasChild(String.valueOf(telePlaceholder.getChildsSize()), commandChannelTeleport.getValue().getPlaceholder());
			}
		}
	}
	
	public final Map<String, SoloTeleport> getSoloTeleports()
	{
		return soloTeleportsMap;
	}
	
	public final Map<String, GroupTeleport> getPartyTeleports()
	{
		return partyTeleportsMap;
	}
	
	public final Map<String, GroupTeleport> getCommandChannelTeleports()
	{
		return commandChannelTeleportsMap;
	}
}
