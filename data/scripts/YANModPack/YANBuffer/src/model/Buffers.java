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
package YANModPack.YANBuffer.src.model;

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

import YANModPack.YANBuffer.src.YANBuffer;
import YANModPack.YANBuffer.src.model.adapter.NpcBufferListToMap;
import YANModPack.YANBuffer.src.model.entity.AbstractBuffer;
import YANModPack.YANBuffer.src.model.entity.NpcBuffer;
import YANModPack.YANBuffer.src.model.entity.VoicedBuffer;

/**
 * @author HorridoJoho
 */
@XmlRootElement(name = "buffers")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Buffers
{
	@XmlElement(name = "voiced_buffer", required = true)
	public final VoicedBuffer voicedBuffer;
	@XmlElement(name = "npc_buffers", required = true)
	@XmlJavaTypeAdapter(NpcBufferListToMap.class)
	public final Map<Integer, NpcBuffer> npcBuffers;
	
	public Buffers()
	{
		voicedBuffer = null;
		npcBuffers = Collections.unmodifiableMap(new LinkedHashMap<>());
	}
	
	public AbstractBuffer determineBuffer(L2Npc npc, L2PcInstance player)
	{
		if (npc == null)
		{
			if (!voicedBuffer.enabled || ((voicedBuffer.requiredItemId > 0) && (player.getInventory().getAllItemsByItemId(voicedBuffer.requiredItemId) == null)))
			{
				return null;
			}
			return voicedBuffer;
		}
		return npcBuffers.get(npc.getId());
	}
	
	public void registerNpcs(YANBuffer scriptInstance)
	{
		for (Entry<Integer, NpcBuffer> npc : npcBuffers.entrySet())
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
