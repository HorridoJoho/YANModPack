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
package YANModPack.YANTeleporter.src.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import YANModPack.YANTeleporter.src.YANTeleporter;
import YANModPack.YANTeleporter.src.model.adapter.NpcTeleporterListToMap;
import YANModPack.YANTeleporter.src.model.entity.AbstractTeleporter;
import YANModPack.YANTeleporter.src.model.entity.NpcTeleporter;
import YANModPack.YANTeleporter.src.model.entity.VoicedTeleporter;

/**
 * @author HorridoJoho
 */
@XmlRootElement(name = "teleporters")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Teleporters
{
	@XmlElement(name = "voiced_teleporter", required = true)
	public final VoicedTeleporter voicedTeleporter;
	@XmlElement(name = "npc_teleporters", required = true)
	@XmlJavaTypeAdapter(NpcTeleporterListToMap.class)
	public final Map<Integer, NpcTeleporter> npcTeleporters;
	
	public Teleporters()
	{
		voicedTeleporter = null;
		npcTeleporters = Collections.unmodifiableMap(new LinkedHashMap<>());
	}
	
	public AbstractTeleporter determineTeleporter(L2Npc npc, L2PcInstance player)
	{
		if (npc == null)
		{
			if (!voicedTeleporter.enabled || ((voicedTeleporter.requiredItemId > 0) && (player.getInventory().getAllItemsByItemId(voicedTeleporter.requiredItemId) == null)))
			{
				return null;
			}
			return voicedTeleporter;
		}
		return npcTeleporters.get(npc.getId());
	}
	
	public void registerNpcs(YANTeleporter scriptInstance)
	{
		for (Entry<Integer, NpcTeleporter> npc : npcTeleporters.entrySet())
		{
			if (npc.getValue().directFirstTalk)
			{
				scriptInstance.addFirstTalkId(npc.getKey());
			}
			scriptInstance.addStartNpc(npc.getKey());
			scriptInstance.addTalkId(npc.getKey());
		}
	}
}
