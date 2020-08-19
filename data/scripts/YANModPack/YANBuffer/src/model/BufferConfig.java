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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.l2jserver.gameserver.config.Configuration;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import YANModPack.YANBuffer.src.YANBuffer;
import YANModPack.YANBuffer.src.model.entity.AbstractBuffer;
import YANModPack.YANBuffer.src.model.entity.NpcBuffer;
import YANModPack.YANBuffer.src.model.entity.VoicedBuffer;

/**
 * @author HorridoJoho
 */
public final class BufferConfig
{
	private GlobalConfig global;
	private VoicedBuffer voiced;
	private Map<Integer, NpcBuffer> npcs;
	
	public BufferConfig() throws JsonSyntaxException, JsonIOException, IOException
	{
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		
		Path jsonPath = Paths.get(Configuration.server().getDatapackRoot().getAbsolutePath(), "data", "scripts", YANBuffer.SCRIPT_PATH.toString(), "data", "json");

		global = gson.fromJson(Files.newBufferedReader(jsonPath.resolve("global.json")), GlobalConfig.class);
		voiced = gson.fromJson(Files.newBufferedReader(jsonPath.resolve("voiced.json")), VoicedBuffer.class);
		npcs = new HashMap<>();

		Path npcsDir = Paths.get(jsonPath.toString(), "npcs");
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(npcsDir))
		{
			for (Path entry : dirStream)
			{
				if (!Files.isRegularFile(entry) || !entry.endsWith(".json"))
				{
					continue;
				}
				
				NpcBuffer npc = gson.fromJson(Files.newBufferedReader(entry), NpcBuffer.class);
				npcs.put(npc.getId(), npc);
			}
		}
		
		global.afterDeserilize(this);
		voiced.afterDeserialize(this);
		for (NpcBuffer npc : npcs.values())
		{
			npc.afterDeserialize(this);
		}
	}
	
	public AbstractBuffer determineBuffer(L2Npc npc, L2PcInstance player)
	{
		if (npc == null)
		{
			if (!voiced.getEnabled() || ((voiced.getRequiredItemId() > 0) && (player.getInventory().getAllItemsByItemId(voiced.getRequiredItemId()) == null)))
			{
				return null;
			}
			return voiced;
		}
		return npcs.get(npc.getId());
	}
	
	public void registerNpcs(YANBuffer scriptInstance)
	{
		for (NpcBuffer npc : npcs.values())
		{
			if (npc.getDirectFirstTalk())
			{
				scriptInstance.addFirstTalkId(npc.getId());
			}
			scriptInstance.addStartNpc(npc.getId());
			scriptInstance.addTalkId(npc.getId());
		}
	}
	
	public GlobalConfig getGlobal()
	{
		return global;
	}
	
	public final VoicedBuffer getVoiced()
	{
		return voiced;
	}
	
	public final Map<Integer, NpcBuffer> getNpcs()
	{
		return npcs;
	}
}