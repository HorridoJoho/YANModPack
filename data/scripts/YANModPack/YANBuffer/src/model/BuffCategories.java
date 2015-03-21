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
package YANModPack.YANBuffer.src.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import YANModPack.YANBuffer.src.model.adapter.direct.BuffCategoryMapAdapter;
import YANModPack.YANBuffer.src.model.adapter.reference.BuffCategoryRefMapAdapter;
import YANModPack.YANBuffer.src.model.entity.BuffCategory;

/**
 * @author HorridoJoho
 */
@XmlRootElement(name = "buff_cats")
public final class BuffCategories
{
	@XmlElement(name = "buff_cat")
	@XmlJavaTypeAdapter(BuffCategoryMapAdapter.class)
	private final Map<String, BuffCategory> _categories;
	
	public BuffCategories()
	{
		_categories = null;
	}
	
	public BuffCategory get(String ident)
	{
		return _categories.get(ident);
	}
	
	public BuffCategoryRefMapAdapter getRefAdapter()
	{
		return new BuffCategoryRefMapAdapter(_categories);
	}
}
