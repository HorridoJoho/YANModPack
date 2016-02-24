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
package YANModPack.YANBuffer.src.model.entity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import YANModPack.YANBuffer.src.model.adapter.BuffCategoryRefListToMap;
import YANModPack.src.model.entity.YANModServer;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public abstract class AbstractBuffer extends YANModServer
{
	@XmlAttribute(name = "can_heal", required = true)
	public final boolean canHeal;
	@XmlAttribute(name = "can_cancel", required = true)
	public final boolean canCancel;
	
	@XmlElement(name = "preset_buff_categories", required = true)
	@XmlJavaTypeAdapter(BuffCategoryRefListToMap.class)
	public Map<String, BuffCategory> presetBuffCats;
	@XmlElement(name = "buff_categories", required = true)
	@XmlJavaTypeAdapter(BuffCategoryRefListToMap.class)
	public Map<String, BuffCategory> buffCats;
	
	public AbstractBuffer(String bypassPrefix)
	{
		super(bypassPrefix, "buffer");
		
		canHeal = false;
		canCancel = false;
		
		presetBuffCats = Collections.unmodifiableMap(new LinkedHashMap<>());
		buffCats = Collections.unmodifiableMap(new LinkedHashMap<>());
	}
	
	@Override
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		super.afterUnmarshal(unmarshaller, parent);
		
		if (canHeal)
		{
			placeholder.addChild("can_heal", null);
		}
		if (canCancel)
		{
			placeholder.addChild("can_cancel", null);
		}
		if (!presetBuffCats.isEmpty())
		{
			HTMLTemplatePlaceholder presetBufflistsPlaceholder = placeholder.addChild("presets", null).getChild("presets");
			for (Entry<String, BuffCategory> presetBufflist : presetBuffCats.entrySet())
			{
				presetBufflistsPlaceholder.addAliasChild(String.valueOf(presetBufflistsPlaceholder.getChildsSize()), presetBufflist.getValue().placeholder);
			}
		}
		if (!buffCats.isEmpty())
		{
			HTMLTemplatePlaceholder buffCatsPlaceholder = placeholder.addChild("categories", null).getChild("categories");
			for (Entry<String, BuffCategory> buffCat : buffCats.entrySet())
			{
				buffCatsPlaceholder.addAliasChild(String.valueOf(buffCatsPlaceholder.getChildsSize()), buffCat.getValue().placeholder);
			}
		}
	}
}
