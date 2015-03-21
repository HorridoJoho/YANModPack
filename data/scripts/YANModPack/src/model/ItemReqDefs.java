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
package YANModPack.src.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import YANModPack.src.model.adapter.direct.ItemReqDefMapAdapter;
import YANModPack.src.model.adapter.reference.ItemReqDefRefMapAdapter;
import YANModPack.src.model.entity.ItemReqDef;

/**
 * @author HorridoJoho
 */
@XmlRootElement(name = "item_requirements")
public final class ItemReqDefs
{
	@XmlElement(name = "item")
	@XmlJavaTypeAdapter(ItemReqDefMapAdapter.class)
	private final Map<String, ItemReqDef> _items;
	
	public ItemReqDefs()
	{
		_items = null;
	}
	
	public ItemReqDef get(String ident)
	{
		return _items.get(ident);
	}
	
	public ItemReqDefRefMapAdapter getRefAdapter()
	{
		return new ItemReqDefRefMapAdapter(_items);
	}
}
