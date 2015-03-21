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

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import YANModPack.YANBuffer.src.model.adapter.reference.BuffCategoryRefMapAdapter;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public abstract class AbstractBuffer
{
	@XmlAttribute(name = "can_heal")
	public final boolean canHeal;
	@XmlAttribute(name = "can_cancel")
	public final boolean canCancel;
	
	@XmlElementWrapper(name = "preset_buff_cats")
	@XmlElement(name = "preset_buff_cat")
	@XmlJavaTypeAdapter(BuffCategoryRefMapAdapter.class)
	public final Map<String, BuffCategory> presetBuffCats;
	@XmlElementWrapper(name = "buff_cats")
	@XmlElement(name = "buff_cat")
	@XmlJavaTypeAdapter(BuffCategoryRefMapAdapter.class)
	public final Map<String, BuffCategory> buffCats;
	
	@XmlTransient
	public final HTMLTemplatePlaceholder placeholder;
	@XmlTransient
	public final String bypassPrefix;
	
	public AbstractBuffer(String bypassPrefix)
	{
		canHeal = false;
		canCancel = false;
		presetBuffCats = null;
		buffCats = null;
		
		placeholder = new HTMLTemplatePlaceholder("buffer", null);
		this.bypassPrefix = bypassPrefix;
	}
	
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		placeholder.addChild("bypass_prefix", "bypass -h " + bypassPrefix).addChild("name", getName());
		if (canHeal)
		{
			placeholder.addChild("can_heal", null);
		}
		if (canCancel)
		{
			placeholder.addChild("can_cancel", null);
		}
		if (!buffCats.isEmpty())
		{
			HTMLTemplatePlaceholder buffCatsPlaceholder = placeholder.addChild("categories", null).getChild("categories");
			for (Entry<String, BuffCategory> buffCat : buffCats.entrySet())
			{
				buffCatsPlaceholder.addAliasChild(String.valueOf(buffCatsPlaceholder.getChildsSize()), buffCat.getValue().placeholder);
			}
		}
		if (!presetBuffCats.isEmpty())
		{
			HTMLTemplatePlaceholder presetBufflistsPlaceholder = placeholder.addChild("presets", null).getChild("presets");
			for (Entry<String, BuffCategory> presetBufflist : presetBuffCats.entrySet())
			{
				presetBufflistsPlaceholder.addAliasChild(String.valueOf(presetBufflistsPlaceholder.getChildsSize()), presetBufflist.getValue().placeholder);
			}
		}
	}
	
	protected abstract String getName();
}