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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import YANModPack.YANBuffer.src.model.adapter.reference.BuffSkillRefListMapAdapter;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public class BuffCategory
{
	@XmlAttribute(name = "name")
	public final String name;
	
	@XmlElement(name = "buffs")
	@XmlJavaTypeAdapter(BuffSkillRefListMapAdapter.class)
	public final Map<String, BuffSkillDef> buffSkills;
	
	@XmlTransient
	public final HTMLTemplatePlaceholder placeholder;
	
	public BuffCategory()
	{
		name = null;
		buffSkills = null;
		
		placeholder = new HTMLTemplatePlaceholder("category", null);
	}
	
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		placeholder.addChild("name", name);
		if (!buffSkills.isEmpty())
		{
			HTMLTemplatePlaceholder buffsPlaceholder = this.placeholder.addChild("buffs", null).getChild("buffs");
			for (Entry<String, BuffSkillDef> buff : buffSkills.entrySet())
			{
				buffsPlaceholder.addAliasChild(String.valueOf(buffsPlaceholder.getChildsSize()), buff.getValue().placeholder);
			}
		}
	}
	
	public BuffSkillDef getBuff(String id)
	{
		return buffSkills.get(id);
	}
}
