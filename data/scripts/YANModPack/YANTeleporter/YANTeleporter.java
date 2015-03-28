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
package YANModPack.YANTeleporter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import YANModPack.YANTeleporter.YANTeleporterData.TeleportLocation;
import YANModPack.YANTeleporter.YANTeleporterData.TeleportNpc;
import YANModPack.src.model.entity.ItemReqDef;
import YANModPack.src.util.htmltmpls.HTMLTemplateParser;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.funcs.ExistsFunc;
import YANModPack.src.util.htmltmpls.funcs.ForeachFunc;
import YANModPack.src.util.htmltmpls.funcs.IfFunc;
import YANModPack.src.util.htmltmpls.funcs.IncludeFunc;
import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class YANTeleporter extends AbstractNpcAI
{
	private static final Logger _LOGGER = Logger.getLogger(YANTeleporter.class.getName());
	public static final Path SCRIPTS_SUBFOLDER = Paths.get("YANModPack");
	public static final Path SCRIPT_TOP_FOLDER = Paths.get("YANTeleporter");
	public static final Path SCRIPT_SUBFOLDER = Paths.get(SCRIPTS_SUBFOLDER.toString(), SCRIPT_TOP_FOLDER.toString());
	
	public static void main(String[] args)
	{
		try
		{
			YANTeleporterData.INIT_INSTANCE();
			new YANTeleporter();
		}
		catch (Exception ex)
		{
			_LOGGER.log(Level.WARNING, "YANTeleporter - Data: Exception while loading npc teleporter data, not registering mod!", ex);
		}
	}
	
	private static TeleportNpc _getTeleNpc(L2Npc npc)
	{
		return YANTeleporterData.GET_INSTANCE().getTeleportNpc(npc.getId());
	}
	
	private static TeleportLocation _getTeleLoc(TeleportNpc teleNpc, String locIdent)
	{
		return teleNpc.locations.get(locIdent);
	}
	
	private static String _generateHtml(String path, L2PcInstance player, HashMap<String, HTMLTemplatePlaceholder> placeholders)
	{
		return HTMLTemplateParser.fromCache(Paths.get("data", "scripts", SCRIPT_SUBFOLDER.toString(), "data", "html", path).toString(), player, placeholders, IncludeFunc.INSTANCE, IfFunc.INSTANCE, ForeachFunc.INSTANCE, ExistsFunc.INSTANCE);
	}
	
	private static String _getMainHtml(L2Npc npc, L2PcInstance player)
	{
		TeleportNpc teleNpc = _getTeleNpc(npc);
		if (teleNpc == null)
		{
			return null;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		placeholders.put(teleNpc.placeholder.getName(), teleNpc.placeholder);
		return _generateHtml("main.html", player, placeholders);
	}
	
	private static String _getLocationHtml(L2Npc npc, L2PcInstance player, String locIdent)
	{
		TeleportNpc teleNpc = _getTeleNpc(npc);
		if (teleNpc == null)
		{
			return null;
		}
		
		TeleportLocation teleLoc = _getTeleLoc(teleNpc, locIdent);
		if (teleLoc == null)
		{
			return null;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		placeholders.put(teleNpc.placeholder.getName(), teleNpc.placeholder);
		placeholders.put(teleLoc.placeholder.getName(), teleLoc.placeholder);
		return _generateHtml("location.html", player, placeholders);
	}
	
	private static String _makeTeleport(L2Npc npc, L2PcInstance player, String locIdent)
	{
		TeleportNpc teleNpc = _getTeleNpc(npc);
		if (teleNpc == null)
		{
			return null;
		}
		
		TeleportLocation teleLoc = _getTeleLoc(teleNpc, locIdent);
		if (teleLoc == null)
		{
			return null;
		}
		
		L2Party party = player.getParty();
		if ((party == null) || (party.getLeader() != player))
		{
			player.sendMessage("You have to be the leader of a party!");
			return null;
		}
		
		if (party.getMemberCount() < teleLoc.minMembers)
		{
			player.sendMessage("For this teleport you need at least " + teleLoc.minMembers + " players in your party!");
			return null;
		}
		
		List<L2PcInstance> members = party.getMembers();
		for (L2PcInstance member : members)
		{
			if (!member.isInsideRadius(npc, teleLoc.maxMemberDistance, true, true))
			{
				player.sendMessage("Not all party members in range!");
				return null;
			}
		}
		
		for (Entry<String, ItemReqDef> item : teleLoc.items.entrySet())
		{
			if (player.getInventory().getInventoryItemCount(item.getValue().getItem().getId(), 0) < item.getValue().itemAmount)
			{
				player.sendMessage("Not enough items!");
				return null;
			}
		}
		
		for (Entry<String, ItemReqDef> item : teleLoc.items.entrySet())
		{
			player.destroyItemByItemId("YANTeleporter-teleport", item.getValue().getItem().getId(), item.getValue().itemAmount, player, true);
		}
		
		for (L2PcInstance member : members)
		{
			member.teleToLocation(teleLoc.pos);
		}
		
		return "Your party has been teleported.";
	}
	
	private YANTeleporter()
	{
		super(SCRIPT_TOP_FOLDER.toString(), SCRIPTS_SUBFOLDER.toString());
		
		for (Entry<Integer, TeleportNpc> npc : YANTeleporterData.GET_INSTANCE().getTeleportNpcs().entrySet())
		{
			addFirstTalkId(npc.getKey());
			addStartNpc(npc.getKey());
			addTalkId(npc.getKey());
		}
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "first_talk.html";
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return _getMainHtml(npc, talker);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if ("list".equals(event))
		{
			return _getMainHtml(npc, player);
		}
		else if (event.startsWith("show_"))
		{
			return _getLocationHtml(npc, player, event.substring(5));
		}
		else if (event.startsWith("use_"))
		{
			return _makeTeleport(npc, player, event.substring(4));
		}
		
		return null;
	}
}