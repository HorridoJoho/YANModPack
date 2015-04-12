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
package YANModPack.YANTeleporter.src;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import YANModPack.YANBuffer.src.YANBufferData;
import YANModPack.YANTeleporter.src.model.entity.AbstractTeleporter;
import YANModPack.YANTeleporter.src.model.entity.GroupTeleportLocation;
import YANModPack.YANTeleporter.src.model.entity.SoloTeleportLocation;
import YANModPack.src.YANModScript;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

import com.l2jserver.gameserver.handler.BypassHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.handler.VoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class YANTeleporter extends YANModScript
{
	private static final class SingletonHolder
	{
		protected static final YANTeleporter INSTANCE = new YANTeleporter();
	}
	
	private static final Logger _LOGGER = Logger.getLogger(YANTeleporter.class.getName());
	public static final String SCRIPT_NAME = "YANTeleporter";
	public static final Path SCRIPT_PATH = Paths.get(SCRIPT_COLLECTION, SCRIPT_NAME);
	
	static YANTeleporter getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public static void main(String[] args)
	{
		try
		{
			YANTeleporterData.initInstance();
		}
		catch (Exception ex)
		{
			_LOGGER.log(Level.WARNING, "YanBuffer - Data: Exception while loading npc buffer data, not registering mod!", ex);
			return;
		}
		
		YANTeleporter scriptInstance = getInstance();
		YANTeleporterData.getInstance().getTeleporters().registerNpcs(scriptInstance);
	}
	
	public YANTeleporter()
	{
		super(SCRIPT_NAME);
		
		BypassHandler.getInstance().registerHandler(YANTeleporterBypassHandler.getInstance());
		
		if (YANTeleporterData.getInstance().getTeleporters().voicedTeleporter.enabled)
		{
			VoicedCommandHandler.getInstance().registerHandler(YANTeleporterVoicedCommandHandler.getInstance());
			ItemHandler.getInstance().registerHandler(YANTeleporterItemHandler.getInstance());
		}
	}
	
	// ////////////////////////////////////
	// UTILITY METHODS
	// ////////////////////////////////////
	private void _debug(L2PcInstance player, String msg)
	{
		if (player.isGM() && YANBufferData.getInstance().getConfig().debug)
		{
			player.sendMessage("YANT DEBUG: " + msg);
		}
	}
	
	// ////////////////////////////////////
	// HTML COMMANDS
	// ////////////////////////////////////
	private void _showAdvancedHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String htmlPath, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
		showAdvancedHtml(player, teleporter, npc, htmlPath, placeholders);
	}
	
	private void _showMainHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "main.html", new HashMap<String, HTMLTemplatePlaceholder>());
	}
	
	private void _showSoloListHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "solo_list.html", new HashMap<String, HTMLTemplatePlaceholder>());
	}
	
	private void _showPartyListHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "party_list.html", new HashMap<String, HTMLTemplatePlaceholder>());
	}
	
	private void _showCommandChannelListHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "command_channel_list.html", new HashMap<String, HTMLTemplatePlaceholder>());
	}
	
	private void _showTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, SoloTeleportLocation teleport, L2Npc npc, String htmlPath)
	{
		Map<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		placeholders.put("teleport", teleport.placeholder);
		
		_showAdvancedHtml(player, teleporter, npc, htmlPath, placeholders);
	}
	
	private void _showSoloTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		SoloTeleportLocation teleport = teleporter.soloLocs.get(teleIdent);
		if (teleport == null)
		{
			return;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "solo_teleport.html");
	}
	
	private void _showPartyTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		GroupTeleportLocation teleport = teleporter.partyLocs.get(teleIdent);
		if (teleport == null)
		{
			return;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "party_teleport.html");
	}
	
	private void _showCommandChannelTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		GroupTeleportLocation teleport = teleporter.commandChannelLocs.get(teleIdent);
		if (teleport == null)
		{
			return;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "command_channel_teleport.html");
	}
	
	private void _executeHtmlCommand(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String command)
	{
		setLastPlayerHtml(player, command);
		
		if ((command = matchAndRemove("main", "m")) != null)
		{
			if (!teleporter.soloLocs.isEmpty() && teleporter.partyLocs.isEmpty() && teleporter.commandChannelLocs.isEmpty())
			{
				_showSoloListHtml(player, teleporter, npc);
			}
			else if (teleporter.soloLocs.isEmpty() && !teleporter.partyLocs.isEmpty() && teleporter.commandChannelLocs.isEmpty())
			{
				_showPartyListHtml(player, teleporter, npc);
			}
			else if (teleporter.soloLocs.isEmpty() && teleporter.partyLocs.isEmpty() && !teleporter.commandChannelLocs.isEmpty())
			{
				_showCommandChannelListHtml(player, teleporter, npc);
			}
			else
			{
				_showMainHtml(player, teleporter, npc);
			}
		}
		else if ((command = matchAndRemove("solo_list", "sl")) != null)
		{
			_showSoloListHtml(player, teleporter, npc);
		}
		else if ((command = matchAndRemove("party_list", "pl")) != null)
		{
			_showPartyListHtml(player, teleporter, npc);
		}
		else if ((command = matchAndRemove("command_channel_list", "ccl")) != null)
		{
			_showCommandChannelListHtml(player, teleporter, npc);
		}
		else if ((command = matchAndRemove("solo_teleport ", "st ")) != null)
		{
			_showSoloTeleportHtml(player, teleporter, npc, command);
		}
		else if ((command = matchAndRemove("party_teleport ", "pt ")) != null)
		{
			_showPartyTeleportHtml(player, teleporter, npc, command);
		}
		else if ((command = matchAndRemove("command_channel_teleport ", "cct ")) != null)
		{
			_showCommandChannelTeleportHtml(player, teleporter, npc, command);
		}
		else
		{
			_showMainHtml(player, teleporter, npc);
		}
	}
	
	// ////////////////////////////////////
	// TELEPORT COMMANDS
	// ////////////////////////////////////
	private void _executeTeleportCommand(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String command)
	{
		
	}
	
	@Override
	protected void executeCommandImpl(L2PcInstance player, L2Npc npc, String command)
	{
		AbstractTeleporter teleporter = YANTeleporterData.getInstance().getTeleporters().determineTeleporter(npc, player);
		if (teleporter == null)
		{
			// not an authorized npc or npc is null and voiced buffer is disabled
			player.sendMessage("No authorization!");
			return;
		}
		
		if ((command == null) || command.isEmpty())
		{
			command = "html main";
		}
		
		_debug(player, "--------------------");
		_debug(player, command);
		
		if ((command = matchAndRemove("html ", "h ")) != null)
		{
			_executeHtmlCommand(player, teleporter, npc, command);
		}
		else
		{
			if ((command = matchAndRemove("teleport ", "t ")) != null)
			{
				_executeTeleportCommand(player, teleporter, npc, command);
			}
			
			showLastPlayerHtml(player, npc);
		}
	}
}