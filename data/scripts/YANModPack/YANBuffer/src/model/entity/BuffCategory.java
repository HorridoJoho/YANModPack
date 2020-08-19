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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import YANModPack.YANBuffer.src.model.BufferConfig;
import YANModPack.src.model.entity.Refable;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public class BuffCategory extends Refable
{
	private String name;
	private List<String> buffSkills;

	private transient Map<String, BuffSkill> buffSkillsMap = null;
	
	public BuffCategory()
	{
		name = null;
		
		buffSkillsMap = new LinkedHashMap<>();
	}
	
	public void afterDeserialize(BufferConfig config)
	{
		super.afterDeserialize();
		
		for (String id : buffSkills)
		{
			buffSkillsMap.put(id, config.getGlobal().getBuff(id));
		}
		
		placeholder.addChild("name", name);
		if (!buffSkills.isEmpty())
		{
			HTMLTemplatePlaceholder buffsPlaceholder = this.placeholder.addChild("buffs", null).getChild("buffs");
			for (Entry<String, BuffSkill> buff : buffSkillsMap.entrySet())
			{
				buffsPlaceholder.addAliasChild(String.valueOf(buffsPlaceholder.getChildsSize()), buff.getValue().getPlaceholder());
			}
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public Map<String, BuffSkill> getBuffs()
	{
		return buffSkillsMap;
	}
	
	public BuffSkill getBuff(String id)
	{
		return buffSkillsMap.get(id);
	}
}
