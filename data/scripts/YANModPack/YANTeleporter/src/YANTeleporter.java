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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import YANModPack.YANBuffer.src.YANBufferData;
import YANModPack.YANTeleporter.src.model.entity.AbstractTeleporter;
import YANModPack.YANTeleporter.src.model.entity.GroupTeleport;
import YANModPack.YANTeleporter.src.model.entity.SoloTeleport;
import YANModPack.src.YANModScript;
import YANModPack.src.util.CommandProcessor;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

import com.l2jserver.gameserver.handler.BypassHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.handler.VoicedCommandHandler;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.AbstractPlayerGroup;
import com.l2jserver.gameserver.model.L2CommandChannel;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;

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
	
	private boolean _takeTeleportItems(SoloTeleport teleport, L2PcInstance initiator)
	{
		if (!teleport.items.isEmpty())
		{
			HashMap<Integer, Long> items = new HashMap<>();
			fillItemAmountMap(items, teleport);
			
			for (Entry<Integer, Long> item : items.entrySet())
			{
				if (initiator.getInventory().getInventoryItemCount(item.getKey(), 0, true) < item.getValue())
				{
					initiator.sendMessage("Not enough items!");
					return false;
				}
			}
			
			for (Entry<Integer, Long> item : items.entrySet())
			{
				initiator.destroyItemByItemId("YANTeleporter", item.getKey(), item.getValue(), initiator, true);
			}
		}
		
		return true;
	}
	
	private InstanceWorld _createInstance(String instanceDefinition)
	{
		int instanceId = InstanceManager.getInstance().createDynamicInstance(instanceDefinition);
		InstanceWorld world = new InstanceWorld();
		world.setInstanceId(instanceId);
		// TODO: world.setTemplateId(???);
		InstanceManager.getInstance().addWorld(world);
		return world;
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
	
	private void _showTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, SoloTeleport teleport, L2Npc npc, String htmlPath)
	{
		Map<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		placeholders.put("teleport", teleport.placeholder);
		
		_showAdvancedHtml(player, teleporter, npc, htmlPath, placeholders);
	}
	
	private void _showSoloTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		SoloTeleport teleport = teleporter.soloLocs.get(teleIdent);
		if (teleport == null)
		{
			return;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "solo_teleport.html");
	}
	
	private void _showPartyTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		GroupTeleport teleport = teleporter.partyLocs.get(teleIdent);
		if (teleport == null)
		{
			return;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "party_teleport.html");
	}
	
	private void _showCommandChannelTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		GroupTeleport teleport = teleporter.commandChannelLocs.get(teleIdent);
		if (teleport == null)
		{
			return;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "command_channel_teleport.html");
	}
	
	private void _executeHtmlCommand(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, CommandProcessor command)
	{
		setLastPlayerHtml(player, command);
		
		if (command.matchAndRemove("main", "m"))
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
		else if (command.matchAndRemove("solo_list", "sl"))
		{
			_showSoloListHtml(player, teleporter, npc);
		}
		else if (command.matchAndRemove("party_list", "pl"))
		{
			_showPartyListHtml(player, teleporter, npc);
		}
		else if (command.matchAndRemove("command_channel_list", "ccl"))
		{
			_showCommandChannelListHtml(player, teleporter, npc);
		}
		else if (command.matchAndRemove("solo_teleport ", "st "))
		{
			_showSoloTeleportHtml(player, teleporter, npc, command.getRemaining());
		}
		else if (command.matchAndRemove("party_teleport ", "pt "))
		{
			_showPartyTeleportHtml(player, teleporter, npc, command.getRemaining());
		}
		else if (command.matchAndRemove("command_channel_teleport ", "cct "))
		{
			_showCommandChannelTeleportHtml(player, teleporter, npc, command.getRemaining());
		}
		else
		{
			_showMainHtml(player, teleporter, npc);
		}
	}
	
	// ////////////////////////////////////
	// TELEPORT COMMANDS
	// ////////////////////////////////////
	private void _makeTeleport(SoloTeleport teleport, L2PcInstance initiator)
	{
		if (!_takeTeleportItems(teleport, initiator))
		{
			return;
		}
		
		InstanceWorld world = null;
		if (!teleport.instance.isEmpty())
		{
			world = _createInstance(teleport.instance);
		}
		
		initiator.teleToLocation(teleport.x, teleport.y, teleport.z, teleport.heading, world != null ? world.getInstanceId() : initiator.getInstanceId(), teleport.randomOffset);
	}
	
	private void _makeGroupTeleport(GroupTeleport teleport, L2PcInstance initiator, AbstractPlayerGroup group)
	{
		final L2PcInstance leader = group.getLeader();
		if (leader != initiator)
		{
			initiator.sendMessage("You are not the leader!");
			return;
		}
		
		final int memberCount = group.getMemberCount();
		if (group.getMemberCount() < teleport.minMembers)
		{
			group.broadcastString("Not enough members!");
			return;
		}
		
		final int leaderInstanceId = leader.getInstanceId();
		final ArrayList<L2PcInstance> membersInRange = new ArrayList<>(memberCount);
		
		for (L2PcInstance member : group.getMembers())
		{
			if ((member != leader) && ((member.getInstanceId() != leaderInstanceId) || (member.calculateDistance(leader, false, false) > teleport.maxDistance)))
			{
				continue;
			}
			
			membersInRange.add(member);
		}
		
		if (membersInRange.size() < memberCount)
		{
			if (!teleport.allowIncomplete)
			{
				group.broadcastString("Your group is not together!");
				return;
			}
			else if (membersInRange.size() < teleport.minMembers)
			{
				group.broadcastString("Not enough members around!");
				return;
			}
		}
		
		if (membersInRange.size() > teleport.maxMembers)
		{
			group.broadcastString("Too many members!");
			return;
		}
		
		if (!_takeTeleportItems(teleport, initiator))
		{
			return;
		}
		
		InstanceWorld world = null;
		if (!teleport.instance.isEmpty())
		{
			world = _createInstance(teleport.instance);
		}
		
		initiator.teleToLocation(teleport.x, teleport.y, teleport.z, teleport.heading, world != null ? world.getInstanceId() : initiator.getInstanceId());
		for (L2PcInstance member : membersInRange)
		{
			member.teleToLocation(initiator, teleport.randomOffset);
		}
	}
	
	private void _teleportSolo(L2PcInstance player, AbstractTeleporter teleporter, String teleId)
	{
		SoloTeleport teleport = teleporter.soloLocs.get(teleId);
		if (teleport == null)
		{
			_debug(player, "Invalid solo teleport id: " + teleId);
			return;
		}
		
		_makeTeleport(teleport, player);
	}
	
	private void _teleportParty(L2PcInstance player, AbstractTeleporter teleporter, String teleId)
	{
		GroupTeleport teleport = teleporter.partyLocs.get(teleId);
		if (teleport == null)
		{
			_debug(player, "Invalid party teleport id: " + teleId);
			return;
		}
		
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendMessage("You are not in a party!");
			return;
		}
		
		_makeGroupTeleport(teleport, player, party);
	}
	
	private void _teleportCommandChannel(L2PcInstance player, AbstractTeleporter teleporter, String teleId)
	{
		GroupTeleport teleport = teleporter.commandChannelLocs.get(teleId);
		if (teleport == null)
		{
			_debug(player, "Invalid command channel teleport id: " + teleId);
			return;
		}
		
		final L2Party party = player.getParty();
		if (party == null)
		{
			player.sendMessage("You are not in a party!");
			return;
		}
		
		final L2CommandChannel commandChannel = party.getCommandChannel();
		if (commandChannel == null)
		{
			player.sendMessage("Your party is not in a command channel!");
			return;
		}
		
		_makeGroupTeleport(teleport, player, commandChannel);
	}
	
	private void _executeTeleportCommand(L2PcInstance player, AbstractTeleporter teleporter, CommandProcessor command)
	{
		if (command.matchAndRemove("solo ", "s "))
		{
			_teleportSolo(player, teleporter, command.getRemaining());
		}
		else if (command.matchAndRemove("party ", "p "))
		{
			_teleportParty(player, teleporter, command.getRemaining());
		}
		else if (command.matchAndRemove("command_channel ", "cc "))
		{
			_teleportCommandChannel(player, teleporter, command.getRemaining());
		}
	}
	
	@Override
	protected void executeCommandImpl(L2PcInstance player, L2Npc npc, String commandString)
	{
		AbstractTeleporter teleporter = YANTeleporterData.getInstance().getTeleporters().determineTeleporter(npc, player);
		if (teleporter == null)
		{
			// not an authorized npc or npc is null and voiced buffer is disabled
			player.sendMessage("No authorization!");
			return;
		}
		
		if ((commandString == null) || commandString.isEmpty())
		{
			commandString = "html main";
		}
		
		_debug(player, "--------------------");
		_debug(player, commandString);
		
		CommandProcessor command = new CommandProcessor(commandString);
		
		if (command.matchAndRemove("html ", "h "))
		{
			_executeHtmlCommand(player, teleporter, npc, command);
		}
		else
		{
			if (command.matchAndRemove("teleport ", "t "))
			{
				_executeTeleportCommand(player, teleporter, command);
			}
			
			showLastPlayerHtml(player, npc);
		}
	}
}