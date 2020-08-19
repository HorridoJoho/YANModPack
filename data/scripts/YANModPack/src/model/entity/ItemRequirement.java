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
package YANModPack.src.model.entity;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.items.L2Item;

import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public class ItemRequirement
{
	private int itemId;
	private long itemAmount;
	
	protected final transient HTMLTemplatePlaceholder placeholder;
	
	public ItemRequirement()
	{
		itemId = 0;
		itemAmount = 0;
		
		placeholder = new HTMLTemplatePlaceholder("placeholder", null);
	}
	
	public ItemRequirement(int itemId, long itemAmount)
	{
		this.itemId = itemId;
		this.itemAmount = itemAmount;
		
		placeholder = new HTMLTemplatePlaceholder("placeholder", null);
		
		afterDeserialize();
	}
	
	public void afterDeserialize()
	{
		final L2Item item = getItem();
		placeholder.addChild("id", String.valueOf(item.getId())).addChild("icon", item.getIcon()).addChild("name", item.getName()).addChild("amount", String.valueOf(itemAmount));
	}
	
	public final int getItemId()
	{
		return itemId;
	}
	
	public final long getItemAmount()
	{
		return itemAmount;
	}
	
	public final L2Item getItem()
	{
		return ItemTable.getInstance().getTemplate(itemId);
	}
}
