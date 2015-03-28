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
package YANModPack.src.model.wrapper;

import javax.xml.bind.annotation.XmlElement;

import YANModPack.src.model.entity.ItemReqDef;

/**
 * @author HorridoJoho
 */
public final class ItemReqListWrapper implements IListWrapper<ItemReqDef>
{
	@XmlElement(name = "item_requirement")
	private final ItemReqDef[] defs;
	
	public ItemReqListWrapper()
	{
		defs = null;
	}
	
	public ItemReqListWrapper(ItemReqDef[] defs)
	{
		this.defs = defs;
	}
	
	@Override
	public ItemReqDef[] getList()
	{
		return defs;
	}
}
