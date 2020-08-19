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
package com.l2jserver.datapack.yanmodpack.base.model.entity;

import com.l2jserver.datapack.yanmodpack.base.util.htmltmpls.HTMLTemplatePlaceholder;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.items.L2Item;

/**
 * @author HorridoJoho
 */
public class ItemRequirement
{
	private int id;
	private long amount;
	
	protected final transient HTMLTemplatePlaceholder placeholder;
	
	public ItemRequirement()
	{
		id = 0;
		amount = 0;
		
		placeholder = new HTMLTemplatePlaceholder("placeholder", null);
	}
	
	public ItemRequirement(int itemId, long itemAmount)
	{
		this.id = itemId;
		this.amount = itemAmount;
		
		placeholder = new HTMLTemplatePlaceholder("placeholder", null);
		
		afterDeserialize();
	}
	
	public void afterDeserialize()
	{
		final L2Item item = getItem();
		placeholder.addChild("id", String.valueOf(item.getId())).addChild("icon", item.getIcon()).addChild("name", item.getName()).addChild("amount", String.valueOf(amount));
	}
	
	public final int getItemId()
	{
		return id;
	}
	
	public final long getItemAmount()
	{
		return amount;
	}
	
	public final L2Item getItem()
	{
		return ItemTable.getInstance().getTemplate(id);
	}
}
