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

import com.l2jserver.datapack.ai.npc.AbstractNpcAI;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jserver.gameserver.util.Util;

import YANModPack.src.model.entity.ItemRequirement;
import YANModPack.src.model.entity.YANModProduct;
import YANModPack.src.model.entity.YANModServer;
import YANModPack.src.util.CommandProcessor;
import YANModPack.src.util.htmltmpls.HTMLTemplateParser;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.funcs.ChildsCountFunc;
import YANModPack.src.util.htmltmpls.funcs.ExistsFunc;
import YANModPack.src.util.htmltmpls.funcs.ForeachFunc;
import YANModPack.src.util.htmltmpls.funcs.IfChildsFunc;
import YANModPack.src.util.htmltmpls.funcs.IfFunc;
import YANModPack.src.util.htmltmpls.funcs.IncludeFunc;

/**
 * @author HorridoJoho
 */
public abstract class YANModScript extends AbstractNpcAI
{
	public static final String SCRIPT_COLLECTION = "YANModPack";
	public final String scriptName;
	public final Path scriptPath;
	private final ConcurrentHashMap<Integer, String> _lastPlayerHtmls = new ConcurrentHashMap<>();
	
	public YANModScript(String name)
	{
		super(name, SCRIPT_COLLECTION);
		
		Objects.requireNonNull(name);
		scriptName = name;
		scriptPath = Paths.get(SCRIPT_COLLECTION, scriptName);
	}
	
	private void _setLastPlayerHtml(L2PcInstance player, String command)
	{
		_lastPlayerHtmls.put(player.getObjectId(), command);
	}
	
	private void _showLastPlayerHtml(L2PcInstance player, L2Npc npc)
	{
		String lastHtmlCommand = _lastPlayerHtmls.get(player.getObjectId());
		if (lastHtmlCommand != null)
		{
			executeHtmlCommand(player, npc, new CommandProcessor(lastHtmlCommand));
		}
	}
	
	private String _generateAdvancedHtml(L2PcInstance player, YANModServer service, String path, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
		final String htmlPath = "/data/scripts/" + scriptPath + "/data/html/" + service.getHtmlFolder() + "/" + path;
		debug(player, htmlPath);
		return HTMLTemplateParser.fromCache(htmlPath, player, placeholders, IncludeFunc.INSTANCE, IfFunc.INSTANCE, ForeachFunc.INSTANCE, ExistsFunc.INSTANCE, IfChildsFunc.INSTANCE, ChildsCountFunc.INSTANCE);
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
			items.put(item.getValue().getItem().getId(), amount + item.getValue().getItemAmount());
		}
	}
	
	protected final void showAdvancedHtml(L2PcInstance player, YANModServer service, L2Npc npc, String path, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
		placeholders.put(service.htmlAccessorName, service.placeholder);
		String html = _generateAdvancedHtml(player, service, path, placeholders);
		
		debug(html);
		
		switch (service.getDialogType())
		{
			case NPC:
				player.sendPacket(new NpcHtmlMessage(npc == null ? 0 : npc.getObjectId(), html));
				break;
			case COMMUNITY:
				Util.sendCBHtml(player, html, npc == null ? 0 : npc.getObjectId());
				break;
		}
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		executeCommand(player, npc, null);
		return null;
	}
	
	public final void debug(L2PcInstance player, String message)
	{
		if (player.isGM() && isDebugEnabled())
		{
			player.sendMessage(scriptName + ": " + message);
		}
	}
	
	public final void debug(String message)
	{
		if (isDebugEnabled())
		{
			System.out.println(message);
		}
	}
	
	public final void executeCommand(L2PcInstance player, L2Npc npc, String commandString)
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
		
		if ((commandString == null) || commandString.isEmpty())
		{
			commandString = "html main";
		}
		
		debug(player, "--------------------");
		debug(player, commandString);
		
		CommandProcessor command = new CommandProcessor(commandString);
		
		if (command.matchAndRemove("html ", "h "))
		{
			String playerCommand = command.getRemaining();
			if (!executeHtmlCommand(player, npc, command))
			{
				_setLastPlayerHtml(player, "main");
			}
			else
			{
				_setLastPlayerHtml(player, playerCommand);
			}
		}
		else
		{
			if (executeActionCommand(player, npc, command))
			{
				_showLastPlayerHtml(player, npc);
			}
		}
	}
	
	/**
	 * Method for Html command processing. The default html command is "main". This also<br>
	 * means, "main" must be implemented. The return value indicates if the user<br>
	 * supplied html command should be saved as last html command.
	 * @param player
	 * @param npc
	 * @param command
	 * @return true: save the html command as last html command<br>
	 *         false: don't save the html command as last html command
	 */
	protected abstract boolean executeHtmlCommand(L2PcInstance player, L2Npc npc, CommandProcessor command);
	
	/**
	 * Method for action command processing. The return value indicates if the<br>
	 * last saved player html command should be executed after this method.
	 * @param player
	 * @param npc
	 * @param command
	 * @return true: execute last saved html command of the player<br>
	 *         false: don't execute last saved html command of the player
	 */
	protected abstract boolean executeActionCommand(L2PcInstance player, L2Npc npc, CommandProcessor command);
	
	/**
	 * Method to determine if debugging is enabled.
	 * @return true: debugging is enabled<br>
	 *         false: debugging is disabled
	 */
	protected abstract boolean isDebugEnabled();
}
