package YANModPack.YANTeleporter.src.model;

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

import YANModPack.YANTeleporter.src.YANTeleporter;
import YANModPack.YANTeleporter.src.model.entity.AbstractTeleporter;
import YANModPack.YANTeleporter.src.model.entity.NpcTeleporter;
import YANModPack.YANTeleporter.src.model.entity.VoicedTeleporter;

public class TeleporterConfig
{
	private final GlobalConfig global;
	private final VoicedTeleporter voiced;
	private final Map<Integer, NpcTeleporter> npcs;
	
	public TeleporterConfig() throws JsonSyntaxException, JsonIOException, IOException
	{
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		
		Path jsonPath = Paths.get(Configuration.server().getDatapackRoot().getAbsolutePath(), "data", "scripts", YANTeleporter.SCRIPT_PATH.toString(), "data", "json");

		global = gson.fromJson(Files.newBufferedReader(jsonPath.resolve("global.json")), GlobalConfig.class);
		voiced = gson.fromJson(Files.newBufferedReader(jsonPath.resolve("voiced.json")), VoicedTeleporter.class);
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
				
				NpcTeleporter npc = gson.fromJson(Files.newBufferedReader(entry), NpcTeleporter.class);
				npcs.put(npc.getId(), npc);
			}
		}
		
		global.afterDeserialize(this);
		voiced.afterDeserialize(this);
		for (NpcTeleporter npc : npcs.values())
		{
			npc.afterDeserialize(this);
		}
	}
	
	public AbstractTeleporter determineTeleporter(L2Npc npc, L2PcInstance player)
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
	
	public void registerNpcs(YANTeleporter scriptInstance)
	{
		for (NpcTeleporter npc : npcs.values())
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
	
	public VoicedTeleporter getVoiced()
	{
		return voiced;
	}
	
	public Map<Integer, NpcTeleporter> getNpcs()
	{
		return npcs;
	}
}
