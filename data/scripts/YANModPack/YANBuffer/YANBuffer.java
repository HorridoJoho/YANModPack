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
package YANModPack.YANBuffer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import YANModPack.YANBuffer.YANBufferData.Buff;
import YANModPack.YANBuffer.YANBufferData.BuffCategory;
import YANModPack.YANBuffer.YANBufferData.Buffer;
import YANModPack.YANBuffer.YANBufferData.BufferNpc;
import YANModPack.YANBuffer.YANBufferData.HtmlType;
import YANModPack.YANBuffer.YANBufferData.VoicedBuffer;
import YANModPack.util.ItemRequirement;
import YANModPack.util.htmltmpls.HTMLTemplateParser;
import YANModPack.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.util.htmltmpls.funcs.ChildsCountFunc;
import YANModPack.util.htmltmpls.funcs.ExistsFunc;
import YANModPack.util.htmltmpls.funcs.ForeachFunc;
import YANModPack.util.htmltmpls.funcs.IfChildsFunc;
import YANModPack.util.htmltmpls.funcs.IfFunc;
import YANModPack.util.htmltmpls.funcs.IncludeFunc;
import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.handler.BypassHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.handler.VoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jserver.gameserver.util.Util;

/**
 * @author HorridoJoho
 */
public final class YANBuffer extends AbstractNpcAI
{
	private static final class SingletonHolder
	{
		protected static final YANBuffer INSTANCE = new YANBuffer();
	}
	
	private static final Logger _LOGGER = Logger.getLogger(YANBuffer.class.getName());
	public static final Path SCRIPTS_SUBFOLDER = Paths.get("YANModPack");
	public static final Path SCRIPT_TOP_FOLDER = Paths.get("YANBuffer");
	public static final Path SCRIPT_SUBFOLDER = Paths.get(SCRIPTS_SUBFOLDER.toString(), SCRIPT_TOP_FOLDER.toString());
	
	static YANBuffer getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public static void main(String[] args)
	{
		try
		{
			YANBufferData.initInstance();
		}
		catch (Exception ex)
		{
			_LOGGER.log(Level.WARNING, "YanBuffer - Data: Exception while loading npc buffer data, not registering mod!", ex);
			return;
		}
		
		YANBuffer instance = getInstance();
		
		for (Entry<Integer, BufferNpc> npc : YANBufferData.getInstance().getBufferNpcs().entrySet())
		{
			instance.addFirstTalkId(npc.getKey());
			instance.addStartNpc(npc.getKey());
			instance.addTalkId(npc.getKey());
		}
	}
	
	private static final ConcurrentHashMap<Integer, Long> _LAST_PLAYABLES_HEAL_TIME = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, String> _LAST_PLAYER_HTMLS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, String> _ACTIVE_PLAYER_BUFFLISTS = new ConcurrentHashMap<>();
	
	YANBuffer()
	{
		super(SCRIPT_TOP_FOLDER.toString(), SCRIPTS_SUBFOLDER.toString());
		
		BypassHandler.getInstance().registerHandler(YANBufferNpcBypassHandler.getInstance());
		
		if (YANBufferData.getInstance().getVoicedBuffer().enabled)
		{
			VoicedCommandHandler.getInstance().registerHandler(YANBufferVoicedCommandHandler.getInstance());
			ItemHandler.getInstance().registerHandler(YANBufferItemHandler.getInstance());
		}
	}
	
	// ////////////////////////////////////
	// AI METHOD OVERRIDES
	// ////////////////////////////////////
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		executeCommand(player, npc, null);
		return null;
	}
	
	// ///////////////////////////////////
	// UTILITY METHODS
	// ///////////////////////////////////
	private Buffer _determineBuffer(L2Npc npc, L2PcInstance player)
	{
		if (npc == null)
		{
			VoicedBuffer buffer = YANBufferData.getInstance().getVoicedBuffer();
			if (!buffer.enabled || ((buffer.requiredItem > 0) && (player.getInventory().getItemByItemId(buffer.requiredItem) == null)))
			{
				return null;
			}
			return buffer;
		}
		return YANBufferData.getInstance().getBufferNpc(npc.getId());
	}
	
	private String _generateAdvancedHtml(L2PcInstance player, String path, Map<String, HTMLTemplatePlaceholder> placeholders, HtmlType dialogType)
	{
		return HTMLTemplateParser.fromCache("/data/scripts/" + SCRIPT_SUBFOLDER + "/data/html/" + dialogType.toString().toLowerCase(Locale.ENGLISH) + "/" + path, player, placeholders, IncludeFunc.INSTANCE, IfFunc.INSTANCE, ForeachFunc.INSTANCE, ExistsFunc.INSTANCE, IfChildsFunc.INSTANCE, ChildsCountFunc.INSTANCE);
	}
	
	private void _fillItemAmountMap(Map<Integer, Long> items, Buff buff)
	{
		for (Entry<String, ItemRequirement> item : buff.items.entrySet())
		{
			Long amount = items.get(item.getValue().item.getId());
			if (amount == null)
			{
				amount = 0L;
			}
			items.put(item.getValue().item.getId(), amount + item.getValue().amount);
		}
	}
	
	private void _castBuff(L2Playable playable, Buff buff)
	{
		buff.skill.applyEffects(playable, playable);
	}
	
	// //////////////////////////////////
	// HTML COMMANDS
	// //////////////////////////////////
	private void _showAdvancedHtml(L2PcInstance player, Buffer buffer, L2Npc npc, String htmlPath, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
		placeholders.put(buffer.placeholder.getName(), buffer.placeholder);
		
		HTMLTemplatePlaceholder ulistsPlaceholder = YANBufferData.getInstance().getPlayersUListsPlaceholder(player.getObjectId());
		if (ulistsPlaceholder != null)
		{
			placeholders.put(ulistsPlaceholder.getName(), ulistsPlaceholder);
		}
		
		String activeUniqueName = _ACTIVE_PLAYER_BUFFLISTS.get(player.getObjectId());
		if (activeUniqueName != null)
		{
			HTMLTemplatePlaceholder ulistPlaceholder = YANBufferData.getInstance().getPlayersUListPlaceholder(player.getObjectId(), activeUniqueName);
			if (ulistPlaceholder != null)
			{
				placeholders.put("active_unique", ulistPlaceholder);
			}
		}
		
		HtmlType dialogType = YANBufferData.getInstance().getHtmlType();
		
		String html = _generateAdvancedHtml(player, htmlPath, placeholders, dialogType);
		switch (dialogType)
		{
			case NPC:
				player.sendPacket(new NpcHtmlMessage(npc == null ? 0 : npc.getObjectId(), html));
				break;
			case COMMUNITY:
				Util.sendCBHtml(player, html);
				break;
		}
	}
	
	private void _htmlShowMain(L2PcInstance player, Buffer buffer, L2Npc npc)
	{
		_showAdvancedHtml(player, buffer, npc, "main.html", new HashMap<String, HTMLTemplatePlaceholder>());
	}
	
	private void _htmlShowCategory(L2PcInstance player, Buffer buffer, L2Npc npc, String categoryIdent)
	{
		BuffCategory buffCat = buffer.getBuffCat(categoryIdent);
		if (buffCat == null)
		{
			return;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put("category", buffCat.placeholder);
		
		_showAdvancedHtml(player, buffer, npc, "category.html", placeholders);
	}
	
	private void _htmlShowBuff(L2PcInstance player, Buffer buffer, L2Npc npc, String categoryIdent, String buffIdent)
	{
		BuffCategory buffCat = buffer.getBuffCat(categoryIdent);
		if (buffCat == null)
		{
			return;
		}
		Buff buff = buffCat.getBuff(buffIdent);
		if (buff == null)
		{
			return;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put("category", buffCat.placeholder);
		placeholders.put("buff", buff.placeholder);
		
		_showAdvancedHtml(player, buffer, npc, "buff.html", placeholders);
	}
	
	private void _htmlShowPreset(L2PcInstance player, Buffer buffer, L2Npc npc, String presetBufflistIdent)
	{
		BuffCategory presetBufflist = buffer.getPresetBufflist(presetBufflistIdent);
		if (presetBufflist == null)
		{
			return;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put("preset", presetBufflist.placeholder);
		
		_showAdvancedHtml(player, buffer, npc, "preset.html", placeholders);
	}
	
	private void _htmlShowUnique(L2PcInstance player, Buffer buffer, L2Npc npc, String uniqueName)
	{
		HTMLTemplatePlaceholder uniquePlaceholder = YANBufferData.getInstance().getPlayersUListPlaceholder(player.getObjectId(), uniqueName);
		if (uniquePlaceholder == null)
		{
			// redirect to main html if uniqueName is not valid, will most likely happen when the player deletes a unique bufflist he is currently viewing
			_executeHtmlCommand(player, buffer, npc, "main");
			return;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put(uniquePlaceholder.getName(), uniquePlaceholder);
		
		_showAdvancedHtml(player, buffer, npc, "unique.html", placeholders);
	}
	
	private void _executeHtmlCommand(L2PcInstance player, Buffer buffer, L2Npc npc, String command)
	{
		_LAST_PLAYER_HTMLS.put(player.getObjectId(), command);
		
		if ("main".equals(command))
		{
			_htmlShowMain(player, buffer, npc);
		}
		else if (command.startsWith("category "))
		{
			_htmlShowCategory(player, buffer, npc, command.substring(9));
		}
		else if (command.startsWith("preset "))
		{
			_htmlShowPreset(player, buffer, npc, command.substring(7));
		}
		else if (command.startsWith("buff "))
		{
			String[] argsSplit = command.substring(5).split(" ", 2);
			if (argsSplit.length != 2)
			{
				return;
			}
			_htmlShowBuff(player, buffer, npc, argsSplit[0], argsSplit[1]);
		}
		else if (command.startsWith("unique "))
		{
			_htmlShowUnique(player, buffer, npc, command.substring(7));
		}
		else
		{
			// all other malformed bypasses
			_htmlShowMain(player, buffer, npc);
		}
	}
	
	//
	// ////////////////////////////////
	
	// /////////////////////////////////////////////
	// TARGET COMMANDS
	// /////////////////////////////////////////////
	private void _targetBuffBuff(L2PcInstance player, L2Playable target, Buffer buffer, String categoryIdent, String buffIdent)
	{
		BuffCategory bCat = buffer.getBuffCat(categoryIdent);
		if (bCat == null)
		{
			return;
		}
		Buff buff = bCat.getBuff(buffIdent);
		if (buff == null)
		{
			return;
		}
		
		if (!buff.items.isEmpty())
		{
			HashMap<Integer, Long> items = new HashMap<>();
			_fillItemAmountMap(items, buff);
			
			for (Entry<Integer, Long> item : items.entrySet())
			{
				if (player.getInventory().getInventoryItemCount(item.getKey(), 0, true) < item.getValue())
				{
					player.sendMessage("Not enough items!");
					return;
				}
			}
			
			for (Entry<Integer, Long> item : items.entrySet())
			{
				player.destroyItemByItemId("YANBuffer", item.getKey(), item.getValue(), player, true);
			}
		}
		
		_castBuff(target, buff);
	}
	
	private void _targetBuffUnique(L2PcInstance player, L2Playable target, Buffer buffer, String uniqueName)
	{
		List<Buff> buffs = YANBufferData.getInstance().getUniqueBufflist(player.getObjectId(), uniqueName);
		
		if (buffs != null)
		{
			HashMap<Integer, Long> items = null;
			for (Buff buff : buffs)
			{
				if (!buff.items.isEmpty())
				{
					if (items == null)
					{
						items = new HashMap<>();
					}
					_fillItemAmountMap(items, buff);
				}
			}
			
			if (items != null)
			{
				for (Entry<Integer, Long> item : items.entrySet())
				{
					if (player.getInventory().getInventoryItemCount(item.getKey(), 0, true) < item.getValue())
					{
						player.sendMessage("Not enough items!");
						return;
					}
				}
				
				for (Entry<Integer, Long> item : items.entrySet())
				{
					player.destroyItemByItemId("YANBuffer", item.getKey(), item.getValue(), player, true);
				}
			}
			
			for (Buff buff : buffs)
			{
				_castBuff(target, buff);
			}
		}
	}
	
	private void _targetBuffPreset(L2PcInstance player, L2Playable target, Buffer buffer, String presetBufflistIdent)
	{
		BuffCategory presetBufflist = buffer.getPresetBufflist(presetBufflistIdent);
		if (presetBufflist == null)
		{
			return;
		}
		
		Collection<Buff> buffs = presetBufflist.buffs.values();
		
		if (buffs != null)
		{
			HashMap<Integer, Long> items = null;
			for (Buff buff : buffs)
			{
				if (!buff.items.isEmpty())
				{
					if (items == null)
					{
						items = new HashMap<>();
					}
					_fillItemAmountMap(items, buff);
				}
			}
			
			if (items != null)
			{
				for (Entry<Integer, Long> item : items.entrySet())
				{
					if (player.getInventory().getInventoryItemCount(item.getKey(), 0, true) < item.getValue())
					{
						player.sendMessage("Not enough items!");
						return;
					}
				}
				
				for (Entry<Integer, Long> item : items.entrySet())
				{
					player.destroyItemByItemId("YANBuffer", item.getKey(), item.getValue(), player, true);
				}
			}
			
			for (Buff buff : buffs)
			{
				_castBuff(target, buff);
			}
		}
	}
	
	private void _targetHeal(L2PcInstance player, L2Playable target, Buffer buffer)
	{
		if (!buffer.canHeal)
		{
			return;
		}
		
		// prevent heal spamming, process cooldown on heal target
		Long lastPlayableHealTime = _LAST_PLAYABLES_HEAL_TIME.get(target.getObjectId());
		if (lastPlayableHealTime != null)
		{
			Long elapsedTime = System.currentTimeMillis() - lastPlayableHealTime;
			Long healCooldown = YANBufferData.getInstance().getHealCooldown();
			if (elapsedTime < healCooldown)
			{
				Long remainingTime = healCooldown - elapsedTime;
				if (target == player)
				{
					player.sendMessage("You can heal yourself again in " + (remainingTime / 1000) + " seconds.");
				}
				else
				{
					player.sendMessage("You can heal your pet again in " + (remainingTime / 1000) + " seconds.");
				}
				return;
			}
		}
		
		_LAST_PLAYABLES_HEAL_TIME.put(target.getObjectId(), System.currentTimeMillis());
		
		if (player == target)
		{
			player.setCurrentCp(player.getMaxCp());
		}
		target.setCurrentHp(target.getMaxHp());
		target.setCurrentMp(target.getMaxMp());
		target.broadcastStatusUpdate();
	}
	
	private void _targetCancel(L2PcInstance player, L2Playable target, Buffer buffer)
	{
		if (!buffer.canCancel)
		{
			return;
		}
		target.stopAllEffectsExceptThoseThatLastThroughDeath();
	}
	
	private void _executeTargetCommand(L2PcInstance player, Buffer buffer, String command)
	{
		// /////////////////////////////////
		// first determine the target
		L2Playable target;
		if (command.startsWith("player "))
		{
			target = player;
			command = command.substring(7);
		}
		else if (command.startsWith("summon "))
		{
			target = player.getSummon();
			if (target == null)
			{
				return;
			}
			command = command.substring(7);
		}
		else
		{
			return;
		}
		
		// //////////////////////////////////////////
		// run the choosen action on the target
		if (command.startsWith("buff "))
		{
			String[] argsSplit = command.substring(5).split(" ", 2);
			if (argsSplit.length != 2)
			{
				return;
			}
			_targetBuffBuff(player, target, buffer, argsSplit[0], argsSplit[1]);
		}
		else if (command.startsWith("unique "))
		{
			_targetBuffUnique(player, target, buffer, command.substring(7));
		}
		else if (command.startsWith("preset "))
		{
			_targetBuffPreset(player, target, buffer, command.substring(7));
		}
		else if ("heal".equals(command))
		{
			_targetHeal(player, target, buffer);
		}
		else if ("cancel".equals(command))
		{
			_targetCancel(player, target, buffer);
		}
	}
	
	//
	// ////////////////////////////////
	
	// ////////////////////////////////
	// UNIQUE COMMANDS
	// ////////////////////////////////
	private boolean _uniqueCreate(L2PcInstance player, String uniqueName)
	{
		if (!YANBufferData.getInstance().canHaveMoreBufflists(player))
		{
			player.sendMessage("Maximum number of unique bufflists reached!");
			return false;
		}
		
		// only allow alpha numeric names because we use this name on the htmls
		if (!uniqueName.matches("[A-Za-z0-9]+"))
		{
			return false;
		}
		
		return YANBufferData.getInstance().createUniqueBufflist(player.getObjectId(), uniqueName);
	}
	
	private void _uniqueDelete(L2PcInstance player, String uniqueName)
	{
		YANBufferData.getInstance().deleteUniqueBufflist(player.getObjectId(), uniqueName);
		// also remove from active bufflist when it's the deleted
		String activeUniqueName = _ACTIVE_PLAYER_BUFFLISTS.get(player.getObjectId());
		if ((activeUniqueName != null) && activeUniqueName.equals(uniqueName))
		{
			_ACTIVE_PLAYER_BUFFLISTS.remove(player.getObjectId());
		}
	}
	
	private void _uniqueAdd(L2PcInstance player, Buffer buffer, String uniqueName, String categoryIdent, String buffIdent)
	{
		BuffCategory bCat = buffer.getBuffCat(categoryIdent);
		if (bCat == null)
		{
			return;
		}
		Buff buff = bCat.getBuff(buffIdent);
		if (buff == null)
		{
			return;
		}
		
		YANBufferData.getInstance().addToUniqueBufflist(player.getObjectId(), uniqueName, buff);
	}
	
	private void _uniqueRemove(L2PcInstance player, String uniqueName, String buffIdent)
	{
		Buff buff = YANBufferData.getInstance().getBuff(buffIdent);
		if (buff == null)
		{
			return;
		}
		
		YANBufferData.getInstance().removeFromUniqueBufflist(player.getObjectId(), uniqueName, buff);
	}
	
	private void _uniqueSelect(L2PcInstance player, String uniqueName)
	{
		if (YANBufferData.getInstance().hasUniqueBufflist(player.getObjectId(), uniqueName))
		{
			_ACTIVE_PLAYER_BUFFLISTS.put(player.getObjectId(), uniqueName);
		}
	}
	
	private void _uniqueDeselect(L2PcInstance player)
	{
		_ACTIVE_PLAYER_BUFFLISTS.remove(player.getObjectId());
	}
	
	private void _executeUniqueCommand(L2PcInstance player, Buffer buffer, String command)
	{
		if (command.startsWith("create "))
		{
			_uniqueCreate(player, command.substring(7));
		}
		else if (command.startsWith("create_from_effects "))
		{
			String uniqueName = command.substring(20);
			if (!_uniqueCreate(player, uniqueName))
			{
				return;
			}
			
			final List<BuffInfo> effects = player.getEffectList().getEffects();
			for (final BuffInfo effect : effects)
			{
				for (Entry<String, BuffCategory> buffCatEntry : buffer.buffCats.entrySet())
				{
					boolean added = false;
					
					for (Entry<String, Buff> buffEntry : buffCatEntry.getValue().buffs.entrySet())
					{
						final Buff buff = buffEntry.getValue();
						
						if (buff.skill.getId() == effect.getSkill().getId())
						{
							_uniqueAdd(player, buffer, uniqueName, buffCatEntry.getKey(), buff.ident);
							added = true;
							break;
						}
					}
					
					if (added)
					{
						break;
					}
				}
			}
		}
		else if (command.startsWith("delete "))
		{
			_uniqueDelete(player, command.substring(7));
		}
		else if (command.startsWith("add "))
		{
			String[] argsSplit = command.substring(4).split(" ", 3);
			if (argsSplit.length != 3)
			{
				return;
			}
			_uniqueAdd(player, buffer, argsSplit[0], argsSplit[1], argsSplit[2]);
		}
		else if (command.startsWith("remove "))
		{
			String[] argsSplit = command.substring(7).split(" ", 2);
			if (argsSplit.length != 2)
			{
				return;
			}
			_uniqueRemove(player, argsSplit[0], argsSplit[1]);
		}
		else if (command.startsWith("select "))
		{
			_uniqueSelect(player, command.substring(7));
		}
		else if (command.startsWith("deselect"))
		{
			_uniqueDeselect(player);
		}
	}
	
	//
	// ////////////////////////////////
	
	private static boolean _isInsideAnyZoneOf(L2Character character, ZoneId first, ZoneId... more)
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
	
	void executeCommand(L2PcInstance player, L2Npc npc, String command)
	{
		if (_isInsideAnyZoneOf(player, ZoneId.PVP, ZoneId.SIEGE, ZoneId.WATER, ZoneId.JAIL, ZoneId.DANGER_AREA))
		{
			player.sendMessage("The buffer cannot be used here.");
			return;
		}
		else if ((player.getEventStatus() != null) || (player.getBlockCheckerArena() != -1) || player.isOnEvent() || player.isInOlympiadMode())
		{
			player.sendMessage("The buffer cannot be used in events.");
			return;
		}
		
		else if (player.isInDuel() || (player.getPvpFlag() == 1))
		{
			player.sendMessage("The buffer cannot be used in duells or pvp.");
			return;
		}
		
		else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			player.sendMessage("The buffer cannot be used while in combat.");
			return;
		}
		
		Buffer buffer = _determineBuffer(npc, player);
		if (buffer == null)
		{
			// not an authorized npc or npc is null and voiced buffer is disabled
			return;
		}
		
		if ((command == null) || command.isEmpty())
		{
			command = "html main";
		}
		
		if (command.startsWith("html "))
		{
			_executeHtmlCommand(player, buffer, npc, command.substring(5));
		}
		else
		{
			if (command.startsWith("target "))
			{
				_executeTargetCommand(player, buffer, command.substring(7));
			}
			else if (command.startsWith("unique "))
			{
				_executeUniqueCommand(player, buffer, command.substring(7));
			}
			
			// display last html again
			// since somebody could use the chat as a command line(eg.: .yanbuffer target player heal), we check if the player has opened a html before
			String lastHtmlCommand = _LAST_PLAYER_HTMLS.get(player.getObjectId());
			if (lastHtmlCommand != null)
			{
				_executeHtmlCommand(player, buffer, npc, _LAST_PLAYER_HTMLS.get(player.getObjectId()));
			}
		}
	}
}
