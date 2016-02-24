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

import YANModPack.YANBuffer.src.model.adapter.BuffSkillRefListToMap;
import YANModPack.src.model.entity.Definition;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public class BuffCategory extends Definition
{
	@XmlAttribute(name = "name", required = true)
	public final String name;
	
	@XmlElement(name = "buffs", required = true)
	@XmlJavaTypeAdapter(BuffSkillRefListToMap.class)
	public final Map<String, BuffSkill> buffSkills;
	
	public BuffCategory()
	{
		name = null;
		
		buffSkills = Collections.unmodifiableMap(new LinkedHashMap<>());
	}
	
	@Override
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		super.afterUnmarshal(unmarshaller, parent);
		
		placeholder.addChild("name", name);
		if (!buffSkills.isEmpty())
		{
			HTMLTemplatePlaceholder buffsPlaceholder = this.placeholder.addChild("buffs", null).getChild("buffs");
			for (Entry<String, BuffSkill> buff : buffSkills.entrySet())
			{
				buffsPlaceholder.addAliasChild(String.valueOf(buffsPlaceholder.getChildsSize()), buff.getValue().placeholder);
			}
		}
	}
	
	public BuffSkill getBuff(String id)
	{
		return buffSkills.get(id);
	}
}
