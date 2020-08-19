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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public abstract class YANModProduct extends Refable
{
	public Map<String, ItemRequirement> items;
	
	protected YANModProduct()
	{}
	
	@Override
	public void afterDeserialize()
	{
		super.afterDeserialize();
		
		if (!items.isEmpty())
		{
			HTMLTemplatePlaceholder itemsPlaceholder = this.placeholder.addChild("items", null).getChild("items");
			for (ItemRequirement item : items.values())
			{
				itemsPlaceholder.addAliasChild(String.valueOf(itemsPlaceholder.getChildsSize()), item.placeholder);
			}
		}
	}
	
	public final Map<String, ItemRequirement> getItems()
	{
		return items;
	}
}
