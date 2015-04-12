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
package YANModPack.src;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import YANModPack.src.model.entity.ItemRequirement;
import YANModPack.src.model.entity.YANModProduct;
import YANModPack.src.model.entity.YANModServer;
import YANModPack.src.util.htmltmpls.HTMLTemplateParser;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.funcs.ChildsCountFunc;
import YANModPack.src.util.htmltmpls.funcs.ExistsFunc;
import YANModPack.src.util.htmltmpls.funcs.ForeachFunc;
import YANModPack.src.util.htmltmpls.funcs.IfChildsFunc;
import YANModPack.src.util.htmltmpls.funcs.IfFunc;
import YANModPack.src.util.htmltmpls.funcs.IncludeFunc;
import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jserver.gameserver.util.Util;

/**
 * @author HorridoJoho
 */
public abstract class YANModScript extends AbstractNpcAI
{
	public static final String SCRIPT_COLLECTION = "YANModPack";
	public final String scriptName;
	public final Path scriptPath;
	private final ConcurrentHashMap<Integer, String> _lastPlayerHtmls = new ConcurrentHashMap<>();
	
	public YANModScript(String descr)
	{
		super(SCRIPT_COLLECTION, descr);
		
		Objects.requireNonNull(descr);
		scriptName = descr;
		scriptPath = Paths.get(SCRIPT_COLLECTION, scriptName);
	}
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		executeCommand(player, npc, null);
		return null;
	}
	
	protected final String matchAndRemove(String command, String... expectations)
	{
		if (command == null)
		{
			return null;
		}
		Objects.requireNonNull(expectations);
		for (String expectation : expectations)
		{
			Objects.requireNonNull(expectation);
			if (!expectation.isEmpty() && command.startsWith(expectation))
			{
				return command.substring(expectation.length());
			}
		}
		return null;
	}
	
	protected final boolean isInsideAnyZoneOf(L2Character character, ZoneId first, ZoneId... more)
	{
		if (character.isInsideZone(first))
		{
			return true;
		}
		
		if (more != null)
		{
			for (ZoneId zone : more)
			{
				if (character.isInsideZone(zone))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	protected final void fillItemAmountMap(Map<Integer, Long> items, YANModProduct product)
	{
		for (Entry<String, ItemRequirement> item : product.items.entrySet())
		{
			Long amount = items.get(item.getValue().getItem().getId());
			if (amount == null)
			{
				amount = 0L;
			}
			items.put(item.getValue().getItem().getId(), amount + item.getValue().itemAmount);
		}
	}
	
	private final String _generateAdvancedHtml(L2PcInstance player, YANModServer service, String path, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
		return HTMLTemplateParser.fromCache("/data/scripts/" + scriptPath + "/data/html/" + service.htmlFolder + "/" + path, player, placeholders, IncludeFunc.INSTANCE, IfFunc.INSTANCE, ForeachFunc.INSTANCE, ExistsFunc.INSTANCE, IfChildsFunc.INSTANCE, ChildsCountFunc.INSTANCE);
	}
	
	protected final void showAdvancedHtml(L2PcInstance player, YANModServer service, L2Npc npc, String path, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
		placeholders.put(service.placeholder.getName(), service.placeholder);
		String html = _generateAdvancedHtml(player, service, path, placeholders);
		switch (service.dialogType)
		{
			case NPC:
				player.sendPacket(new NpcHtmlMessage(npc == null ? 0 : npc.getObjectId(), html));
				break;
			case COMMUNITY:
				Util.sendCBHtml(player, html, npc == null ? 0 : npc.getObjectId());
				break;
		}
	}
	
	public final void executeCommand(L2PcInstance player, L2Npc npc, String command)
	{
		if (isInsideAnyZoneOf(player, ZoneId.PVP, ZoneId.SIEGE, ZoneId.WATER, ZoneId.JAIL, ZoneId.DANGER_AREA))
		{
			player.sendMessage("The service cannot be used here.");
			return;
		}
		else if ((player.getEventStatus() != null) || (player.getBlockCheckerArena() != -1) || player.isOnEvent() || player.isInOlympiadMode())
		{
			player.sendMessage("The service cannot be used in events.");
			return;
		}
		
		else if (player.isInDuel() || (player.getPvpFlag() == 1))
		{
			player.sendMessage("The service cannot be used in duells or pvp.");
			return;
		}
		
		else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			player.sendMessage("The service cannot be used while in combat.");
			return;
		}
		
		executeCommandImpl(player, npc, command);
	}
	
	protected void setLastPlayerHtml(L2PcInstance player, String html)
	{
		_lastPlayerHtmls.put(player.getObjectId(), html);
	}
	
	protected void showLastPlayerHtml(L2PcInstance player, L2Npc npc)
	{
		String lastHtmlCommand = _lastPlayerHtmls.get(player.getObjectId());
		if (lastHtmlCommand != null)
		{
			executeCommandImpl(player, npc, "html" + lastHtmlCommand);
		}
	}
	
	protected abstract void executeCommandImpl(L2PcInstance player, L2Npc npc, String command);
}
