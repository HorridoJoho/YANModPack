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
			_LOGGER.log(Level.WARNING, "YanTeleporter - Data: Exception while loading teleporter data, not registering mod!", ex);
			return;
		}
		
		YANTeleporter scriptInstance = getInstance();
		YANTeleporterData.getInstance().getConfig().registerNpcs(scriptInstance);
	}
	
	public YANTeleporter()
	{
		super(SCRIPT_NAME);
		
		BypassHandler.getInstance().registerHandler(YANTeleporterBypassHandler.getInstance());
		
		if (YANTeleporterData.getInstance().getConfig().getVoiced().getEnabled())
		{
			VoicedCommandHandler.getInstance().registerHandler(YANTeleporterVoicedCommandHandler.getInstance());
			ItemHandler.getInstance().registerHandler(YANTeleporterItemHandler.getInstance());
		}
	}
	
	// ////////////////////////////////////
	// UTILITY METHODS
	// ////////////////////////////////////
	private void _showAdvancedHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String htmlPath, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
		showAdvancedHtml(player, teleporter, npc, htmlPath, placeholders);
	}
	
	private void _showTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, SoloTeleport teleport, L2Npc npc, String htmlPath)
	{
		Map<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		placeholders.put("teleport", teleport.getPlaceholder());
		
		_showAdvancedHtml(player, teleporter, npc, htmlPath, placeholders);
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
	private boolean _showMainHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "main.html", new HashMap<String, HTMLTemplatePlaceholder>());
		return true;
	}
	
	private boolean _showSoloListHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "solo_list.html", new HashMap<String, HTMLTemplatePlaceholder>());
		return true;
	}
	
	private boolean _showPartyListHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "party_list.html", new HashMap<String, HTMLTemplatePlaceholder>());
		return true;
	}
	
	private boolean _showCommandChannelListHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc)
	{
		_showAdvancedHtml(player, teleporter, npc, "command_channel_list.html", new HashMap<String, HTMLTemplatePlaceholder>());
		return true;
	}
	
	private boolean _showSoloTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		SoloTeleport teleport = teleporter.getSoloTeleports().get(teleIdent);
		if (teleport == null)
		{
			debug(player, "Invalid teleport ident: " + teleIdent);
			return false;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "solo_teleport.html");
		return true;
	}
	
	private boolean _showPartyTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		GroupTeleport teleport = teleporter.getPartyTeleports().get(teleIdent);
		if (teleport == null)
		{
			debug(player, "Invalid teleport ident: " + teleIdent);
			return false;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "party_teleport.html");
		return true;
	}
	
	private boolean _showCommandChannelTeleportHtml(L2PcInstance player, AbstractTeleporter teleporter, L2Npc npc, String teleIdent)
	{
		GroupTeleport teleport = teleporter.getCommandChannelTeleports().get(teleIdent);
		if (teleport == null)
		{
			debug(player, "Invalid teleport ident: " + teleIdent);
			return false;
		}
		
		_showTeleportHtml(player, teleporter, teleport, npc, "command_channel_teleport.html");
		return true;
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
		if (!teleport.getInstance().isEmpty())
		{
			world = _createInstance(teleport.getInstance());
			world.addAllowed(initiator.getObjectId());
		}
		
		initiator.teleToLocation(teleport.getX(), teleport.getY(), teleport.getZ(), teleport.getHeading(), world != null ? world.getInstanceId() : initiator.getInstanceId(), teleport.getRandomOffset());
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
		if (group.getMemberCount() < teleport.getMinMembers())
		{
			group.broadcastString("Not enough members!");
			return;
		}
		
		final int leaderInstanceId = leader.getInstanceId();
		final ArrayList<L2PcInstance> membersInRange = new ArrayList<>(memberCount);
		membersInRange.add(leader);
		
		for (L2PcInstance member : group.getMembers())
		{
			if ((member != leader) && ((member.getInstanceId() != leaderInstanceId) || (member.calculateDistance(leader, false, false) > teleport.getMaxDistance())))
			{
				continue;
			}
			
			membersInRange.add(member);
		}
		
		if (membersInRange.size() < memberCount)
		{
			if (!teleport.getAllowIncomplete())
			{
				group.broadcastString("Your group is not together!");
				return;
			}
			else if (membersInRange.size() < teleport.getMinMembers())
			{
				group.broadcastString("Not enough members around!");
				return;
			}
		}
		
		if (membersInRange.size() > teleport.getMaxMembers())
		{
			group.broadcastString("Too many members!");
			return;
		}
		
		if (!_takeTeleportItems(teleport, initiator))
		{
			return;
		}
		
		InstanceWorld world = null;
		int instanceId = initiator.getInstanceId();
		if (!teleport.getInstance().isEmpty())
		{
			world = _createInstance(teleport.getInstance());
			instanceId = world.getInstanceId();
		}
		
		for (L2PcInstance member : membersInRange)
		{
			if (world != null)
			{
				world.addAllowed(member.getObjectId());
			}
			member.teleToLocation(teleport.getX(), teleport.getY(), teleport.getZ(), teleport.getHeading(), instanceId, teleport.getRandomOffset());
		}
	}
	
	private void _teleportSolo(L2PcInstance player, AbstractTeleporter teleporter, String teleId)
	{
		SoloTeleport teleport = teleporter.getSoloTeleports().get(teleId);
		if (teleport == null)
		{
			debug(player, "Invalid solo teleport id: " + teleId);
			return;
		}
		
		_makeTeleport(teleport, player);
	}
	
	private void _teleportParty(L2PcInstance player, AbstractTeleporter teleporter, String teleId)
	{
		GroupTeleport teleport = teleporter.getPartyTeleports().get(teleId);
		if (teleport == null)
		{
			debug(player, "Invalid party teleport id: " + teleId);
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
		GroupTeleport teleport = teleporter.getCommandChannelTeleports().get(teleId);
		if (teleport == null)
		{
			debug(player, "Invalid command channel teleport id: " + teleId);
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
	
	//
	// //////////////////////////////
	
	@Override
	protected boolean executeHtmlCommand(L2PcInstance player, L2Npc npc, CommandProcessor command)
	{
		AbstractTeleporter teleporter = YANTeleporterData.getInstance().getConfig().determineTeleporter(npc, player);
		if (teleporter == null)
		{
			// not an authorized npc or npc is null and voiced buffer is disabled
			player.sendMessage("No authorization!");
			return false;
		}
		
		if (command.matchAndRemove("main", "m"))
		{
			if (!teleporter.getSoloTeleports().isEmpty() && teleporter.getPartyTeleports().isEmpty() && teleporter.getCommandChannelTeleports().isEmpty())
			{
				return _showSoloListHtml(player, teleporter, npc);
			}
			else if (teleporter.getSoloTeleports().isEmpty() && !teleporter.getPartyTeleports().isEmpty() && teleporter.getCommandChannelTeleports().isEmpty())
			{
				return _showPartyListHtml(player, teleporter, npc);
			}
			else if (teleporter.getSoloTeleports().isEmpty() && teleporter.getPartyTeleports().isEmpty() && !teleporter.getCommandChannelTeleports().isEmpty())
			{
				return _showCommandChannelListHtml(player, teleporter, npc);
			}
			
			return _showMainHtml(player, teleporter, npc);
		}
		else if (command.matchAndRemove("solo_list", "sl"))
		{
			return _showSoloListHtml(player, teleporter, npc);
		}
		else if (command.matchAndRemove("party_list", "pl"))
		{
			return _showPartyListHtml(player, teleporter, npc);
		}
		else if (command.matchAndRemove("command_channel_list", "ccl"))
		{
			return _showCommandChannelListHtml(player, teleporter, npc);
		}
		else if (command.matchAndRemove("solo_teleport ", "st "))
		{
			return _showSoloTeleportHtml(player, teleporter, npc, command.getRemaining());
		}
		else if (command.matchAndRemove("party_teleport ", "pt "))
		{
			return _showPartyTeleportHtml(player, teleporter, npc, command.getRemaining());
		}
		else if (command.matchAndRemove("command_channel_teleport ", "cct "))
		{
			return _showCommandChannelTeleportHtml(player, teleporter, npc, command.getRemaining());
		}
		
		return false;
	}
	
	@Override
	protected boolean executeActionCommand(L2PcInstance player, L2Npc npc, CommandProcessor command)
	{
		AbstractTeleporter teleporter = YANTeleporterData.getInstance().getConfig().determineTeleporter(npc, player);
		if (teleporter == null)
		{
			player.sendMessage("No authorization!");
			return false;
		}
		
		if (command.matchAndRemove("teleport ", "t "))
		{
			_executeTeleportCommand(player, teleporter, command);
			return false;
		}
		
		return true;
	}
	
	@Override
	protected boolean isDebugEnabled()
	{
		return YANTeleporterData.getInstance().getConfig().getGlobal().getDebug();
	}
}