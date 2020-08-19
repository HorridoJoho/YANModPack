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
package YANModPack.YANBuffer.src;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.gameserver.handler.BypassHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.handler.VoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.BuffInfo;

import YANModPack.YANBuffer.src.model.entity.AbstractBuffer;
import YANModPack.YANBuffer.src.model.entity.BuffCategory;
import YANModPack.YANBuffer.src.model.entity.BuffSkill;
import YANModPack.src.YANModScript;
import YANModPack.src.util.CommandProcessor;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;


/**
 * @author HorridoJoho
 */
public final class YANBuffer extends YANModScript
{
	private static final class SingletonHolder
	{
		protected static final YANBuffer INSTANCE = new YANBuffer();
	}
	
	private static final Logger _LOGGER = Logger.getLogger(YANBuffer.class.getName());
	public static final String SCRIPT_NAME = "YANBuffer";
	public static final Path SCRIPT_PATH = Paths.get(SCRIPT_COLLECTION.toString(), SCRIPT_NAME);
	
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
		
		YANBuffer scriptInstance = getInstance();
		YANBufferData.getInstance().getConfig().registerNpcs(scriptInstance);
	}
	
	private static final ConcurrentHashMap<Integer, Long> _LAST_PLAYABLES_HEAL_TIME = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, String> _ACTIVE_PLAYER_BUFFLISTS = new ConcurrentHashMap<>();
	
	YANBuffer()
	{
		super(SCRIPT_NAME);
		
		BypassHandler.getInstance().registerHandler(YANBufferBypassHandler.getInstance());
		
		if (YANBufferData.getInstance().getConfig().getVoiced().getEnabled())
		{
			VoicedCommandHandler.getInstance().registerHandler(YANBufferVoicedCommandHandler.getInstance());
			ItemHandler.getInstance().registerHandler(YANBufferItemHandler.getInstance());
		}
	}
	
	// ///////////////////////////////////
	// UTILITY METHODS
	// ///////////////////////////////////
	private void _castBuff(L2Playable playable, BuffSkill buff)
	{
		buff.getSkill().applyEffects(playable, playable);
	}
	
	// //////////////////////////////////
	// HTML COMMANDS
	// //////////////////////////////////
	private void _showAdvancedHtml(L2PcInstance player, AbstractBuffer buffer, L2Npc npc, String htmlPath, Map<String, HTMLTemplatePlaceholder> placeholders)
	{
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
		
		showAdvancedHtml(player, buffer, npc, htmlPath, placeholders);
	}
	
	private boolean _htmlShowMain(L2PcInstance player, AbstractBuffer buffer, L2Npc npc)
	{
		_showAdvancedHtml(player, buffer, npc, "main.html", new HashMap<String, HTMLTemplatePlaceholder>());
		return true;
	}
	
	private boolean _htmlShowCategory(L2PcInstance player, AbstractBuffer buffer, L2Npc npc, String categoryIdent)
	{
		BuffCategory buffCat = buffer.getBuffCats().get(categoryIdent);
		if (buffCat == null)
		{
			debug(player, "Invalid buff category: " + categoryIdent);
			return false;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put("category", buffCat.getPlaceholder());
		
		_showAdvancedHtml(player, buffer, npc, "category.html", placeholders);
		return true;
	}
	
	private boolean _htmlShowBuff(L2PcInstance player, AbstractBuffer buffer, L2Npc npc, String categoryIdent, String buffIdent)
	{
		BuffCategory buffCat = buffer.getBuffCats().get(categoryIdent);
		if (buffCat == null)
		{
			debug(player, "Invalid buff category: " + categoryIdent);
			return false;
		}
		BuffSkill buff = buffCat.getBuff(buffIdent);
		if (buff == null)
		{
			debug(player, "Invalid buff skill: " + buffIdent);
			return false;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put("category", buffCat.getPlaceholder());
		placeholders.put("buff", buff.getPlaceholder());
		
		_showAdvancedHtml(player, buffer, npc, "buff.html", placeholders);
		return true;
	}
	
	private boolean _htmlShowPreset(L2PcInstance player, AbstractBuffer buffer, L2Npc npc, String presetBufflistIdent)
	{
		BuffCategory presetBufflist = buffer.getPresetBuffCats().get(presetBufflistIdent);
		if (presetBufflist == null)
		{
			debug(player, "Invalid preset buff category: " + presetBufflistIdent);
			return false;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put("preset", presetBufflist.getPlaceholder());
		
		_showAdvancedHtml(player, buffer, npc, "preset.html", placeholders);
		return true;
	}
	
	private boolean _htmlShowUnique(L2PcInstance player, AbstractBuffer buffer, L2Npc npc, String uniqueName)
	{
		HTMLTemplatePlaceholder uniquePlaceholder = YANBufferData.getInstance().getPlayersUListPlaceholder(player.getObjectId(), uniqueName);
		if (uniquePlaceholder == null)
		{
			// redirect to main html if uniqueName is not valid, will most likely happen when the player deletes a unique bufflist he is currently viewing
			executeHtmlCommand(player, npc, new CommandProcessor("main"));
			return false;
		}
		
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		placeholders.put(uniquePlaceholder.getName(), uniquePlaceholder);
		
		_showAdvancedHtml(player, buffer, npc, "unique.html", placeholders);
		return true;
	}
	
	//
	// ////////////////////////////////
	
	// /////////////////////////////////////////////
	// TARGET COMMANDS
	// /////////////////////////////////////////////
	private void _targetBuffBuff(L2PcInstance player, L2Playable target, AbstractBuffer buffer, String categoryIdent, String buffIdent)
	{
		BuffCategory bCat = buffer.getBuffCats().get(categoryIdent);
		if (bCat == null)
		{
			debug(player, "Invalid buff category: " + categoryIdent);
			return;
		}
		BuffSkill buff = bCat.getBuff(buffIdent);
		if (buff == null)
		{
			debug(player, "Invalid buff skill: " + buffIdent);
			return;
		}
		
		if (!buff.items.isEmpty())
		{
			HashMap<Integer, Long> items = new HashMap<>();
			fillItemAmountMap(items, buff);
			
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
	
	private void _targetBuffUnique(L2PcInstance player, L2Playable target, AbstractBuffer buffer, String uniqueName)
	{
		List<BuffSkill> buffs = YANBufferData.getInstance().getUniqueBufflist(player.getObjectId(), uniqueName);
		
		if (buffs != null)
		{
			HashMap<Integer, Long> items = null;
			for (BuffSkill buff : buffs)
			{
				if (!buff.items.isEmpty())
				{
					if (items == null)
					{
						items = new HashMap<>();
					}
					fillItemAmountMap(items, buff);
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
			
			for (BuffSkill buff : buffs)
			{
				_castBuff(target, buff);
			}
		}
	}
	
	private void _targetBuffPreset(L2PcInstance player, L2Playable target, AbstractBuffer buffer, String presetBufflistIdent)
	{
		BuffCategory presetBufflist = buffer.getPresetBuffCats().get(presetBufflistIdent);
		if (presetBufflist == null)
		{
			debug(player, "Invalid preset buff category: " + presetBufflistIdent);
			return;
		}
		
		Collection<BuffSkill> buffs = presetBufflist.getBuffs().values();
		
		if (buffs != null)
		{
			HashMap<Integer, Long> items = null;
			for (BuffSkill buff : buffs)
			{
				if (!buff.items.isEmpty())
				{
					if (items == null)
					{
						items = new HashMap<>();
					}
					fillItemAmountMap(items, buff);
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
			
			for (BuffSkill buff : buffs)
			{
				_castBuff(target, buff);
			}
		}
	}
	
	private void _targetHeal(L2PcInstance player, L2Playable target, AbstractBuffer buffer)
	{
		if (!buffer.getCanHeal())
		{
			debug(player, "This buffer can not heal!");
			return;
		}
		
		// prevent heal spamming, process cooldown on heal target
		Long lastPlayableHealTime = _LAST_PLAYABLES_HEAL_TIME.get(target.getObjectId());
		if (lastPlayableHealTime != null)
		{
			Long elapsedTime = System.currentTimeMillis() - lastPlayableHealTime;
			Long healCooldown = YANBufferData.getInstance().getConfig().getGlobal().getHealCooldown() * 1000L;
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
	
	private void _targetCancel(L2PcInstance player, L2Playable target, AbstractBuffer buffer)
	{
		if (!buffer.getCanCancel())
		{
			debug(player, "This buffer can not cancel!");
			return;
		}
		target.stopAllEffectsExceptThoseThatLastThroughDeath();
	}
	
	private void _executeTargetCommand(L2PcInstance player, AbstractBuffer buffer, CommandProcessor command)
	{
		// /////////////////////////////////
		// first determine the target
		L2Playable target;
		if (command.matchAndRemove("player ", "p "))
		{
			target = player;
		}
		else if (command.matchAndRemove("summon ", "s "))
		{
			target = player.getSummon();
			if (target == null)
			{
				debug(player, "No summon available!");
				return;
			}
		}
		else
		{
			debug(player, "Invalid target command target!");
			return;
		}
		
		// //////////////////////////////////////////
		// run the choosen action on the target
		if (command.matchAndRemove("buff ", "b "))
		{
			String[] argsSplit = command.splitRemaining(" ");
			if (argsSplit.length != 2)
			{
				debug(player, "Missing arguments!");
				return;
			}
			_targetBuffBuff(player, target, buffer, argsSplit[0], argsSplit[1]);
		}
		else if (command.matchAndRemove("unique ", "u "))
		{
			_targetBuffUnique(player, target, buffer, command.getRemaining());
		}
		else if (command.matchAndRemove("preset ", "p "))
		{
			_targetBuffPreset(player, target, buffer, command.getRemaining());
		}
		else if (command.matchAndRemove("heal", "h"))
		{
			_targetHeal(player, target, buffer);
		}
		else if (command.matchAndRemove("cancel", "c"))
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
	
	private void _uniqueAdd(L2PcInstance player, AbstractBuffer buffer, String uniqueName, String categoryIdent, String buffIdent)
	{
		BuffCategory bCat = buffer.getBuffCats().get(categoryIdent);
		if (bCat == null)
		{
			return;
		}
		BuffSkill buff = bCat.getBuff(buffIdent);
		if (buff == null)
		{
			return;
		}
		
		YANBufferData.getInstance().addToUniqueBufflist(player.getObjectId(), uniqueName, buff);
	}
	
	private void _uniqueRemove(L2PcInstance player, String uniqueName, String buffIdent)
	{
		BuffSkill buff = YANBufferData.getInstance().getConfig().getGlobal().getBuff(buffIdent);
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
	
	private void _executeUniqueCommand(L2PcInstance player, AbstractBuffer buffer, CommandProcessor command)
	{
		if (command.matchAndRemove("create ", "c "))
		{
			_uniqueCreate(player, command.getRemaining());
		}
		else if (command.matchAndRemove("create_from_effects ", "cfe "))
		{
			String uniqueName = command.getRemaining();
			if (!_uniqueCreate(player, uniqueName))
			{
				return;
			}
			
			final List<BuffInfo> effects = player.getEffectList().getEffects();
			for (final BuffInfo effect : effects)
			{
				for (Entry<String, BuffCategory> buffCatEntry : buffer.getBuffCats().entrySet())
				{
					boolean added = false;
					
					for (Entry<String, BuffSkill> buffEntry : buffCatEntry.getValue().getBuffs().entrySet())
					{
						final BuffSkill buff = buffEntry.getValue();
						
						if (buff.getSkill().getId() == effect.getSkill().getId())
						{
							_uniqueAdd(player, buffer, uniqueName, buffCatEntry.getKey(), buff.getId());
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
		else if (command.matchAndRemove("delete ", "del "))
		{
			_uniqueDelete(player, command.getRemaining());
		}
		else if (command.matchAndRemove("add ", "a "))
		{
			String[] argsSplit = command.splitRemaining(" ");
			if (argsSplit.length != 3)
			{
				debug(player, "Missing arguments!");
				return;
			}
			_uniqueAdd(player, buffer, argsSplit[0], argsSplit[1], argsSplit[2]);
		}
		else if (command.matchAndRemove("remove ", "r "))
		{
			String[] argsSplit = command.splitRemaining(" ");
			if (argsSplit.length != 2)
			{
				debug(player, "Missing arguments!");
				return;
			}
			_uniqueRemove(player, argsSplit[0], argsSplit[1]);
		}
		else if (command.matchAndRemove("select ", "s "))
		{
			_uniqueSelect(player, command.getRemaining());
		}
		else if (command.matchAndRemove("deselect", "des"))
		{
			_uniqueDeselect(player);
		}
	}
	
	//
	// ////////////////////////////////
	
	@Override
	public boolean executeHtmlCommand(L2PcInstance player, L2Npc npc, CommandProcessor command)
	{
		AbstractBuffer buffer = YANBufferData.getInstance().getConfig().determineBuffer(npc, player);
		if (buffer == null)
		{
			player.sendMessage("No authorization!");
			return false;
		}
		
		if (command.matchAndRemove("main", "m"))
		{
			return _htmlShowMain(player, buffer, npc);
		}
		else if (command.matchAndRemove("category ", "c "))
		{
			return _htmlShowCategory(player, buffer, npc, command.getRemaining());
		}
		else if (command.matchAndRemove("preset ", "p "))
		{
			return _htmlShowPreset(player, buffer, npc, command.getRemaining());
		}
		else if (command.matchAndRemove("buff ", "b "))
		{
			String[] argsSplit = command.splitRemaining(" ");
			if (argsSplit.length != 2)
			{
				debug(player, "Missing arguments!");
				return false;
			}
			return _htmlShowBuff(player, buffer, npc, argsSplit[0], argsSplit[1]);
		}
		else if (command.matchAndRemove("unique ", "u "))
		{
			return _htmlShowUnique(player, buffer, npc, command.getRemaining());
		}
		
		return false;
	}
	
	@Override
	public boolean executeActionCommand(L2PcInstance player, L2Npc npc, CommandProcessor command)
	{
		AbstractBuffer buffer = YANBufferData.getInstance().getConfig().determineBuffer(npc, player);
		if (buffer == null)
		{
			player.sendMessage("No authorization!");
			return false;
		}
		
		if (command.matchAndRemove("target ", "t "))
		{
			_executeTargetCommand(player, buffer, command);
		}
		else if (command.matchAndRemove("unique ", "u "))
		{
			_executeUniqueCommand(player, buffer, command);
		}
		
		return true;
	}
	
	@Override
	protected boolean isDebugEnabled()
	{
		return YANBufferData.getInstance().getConfig().getGlobal().getDebug();
	}
}
