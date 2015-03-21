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

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import YANModPack.YANBuffer.src.YANBufferData.BuffType;
import YANModPack.src.model.adapter.reference.ItemReqDefRefMapAdapter;
import YANModPack.src.model.entity.ItemReqDef;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author HorridoJoho
 */
public final class BuffSkill
{
	@XmlAttribute(name = "ident")
	public final String id;
	@XmlAttribute(name = "skill_id")
	public final int skillId;
	@XmlAttribute(name = "skill_level")
	public final int skillLevel;
	@XmlAttribute(name = "type")
	public final BuffType type;
	
	@XmlElementWrapper(name = "item_requirements")
	@XmlElement(name = "item")
	@XmlJavaTypeAdapter(ItemReqDefRefMapAdapter.class)
	public final Map<String, ItemReqDef> items;
	
	@XmlTransient
	public final HTMLTemplatePlaceholder placeholder;
	
	protected BuffSkill()
	{
		id = null;
		skillId = 0;
		skillLevel = 0;
		type = BuffType.BUFF;
		items = null;
		
		placeholder = new HTMLTemplatePlaceholder("buff", null);
	}
	
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		final Skill skill = getSkill();
		placeholder.addChild("ident", id).addChild("skill_id", String.valueOf(skill.getId())).addChild("skill_name", skill.getName()).addChild("skill_icon", _getClientSkillIconSource(skill.getId())).addChild("type", type.toString());
		if (!items.isEmpty())
		{
			HTMLTemplatePlaceholder itemsPlaceholder = this.placeholder.addChild("items", null).getChild("items");
			for (ItemReqDef item : items.values())
			{
				itemsPlaceholder.addAliasChild(String.valueOf(itemsPlaceholder.getChildsSize()), item.placeholder);
			}
		}
	}
	
	public Skill getSkill()
	{
		return SkillData.getInstance().getSkill(skillId, skillLevel);
	}
	
	private String _getClientSkillIconSource(int skillId)
	{
		String format = "";
		if (skillId < 100)
		{
			format = "00" + skillId;
		}
		else if ((skillId > 99) && (skillId < 1000))
		{
			format = "0" + skillId;
		}
		else if (skillId == 1517)
		{
			format = "1499";
		}
		else if (skillId == 1518)
		{
			format = "1502";
		}
		else
		{
			if ((skillId > 4698) && (skillId < 4701))
			{
				format = "1331";
			}
			else if ((skillId > 4701) && (skillId < 4704))
			{
				format = "1332";
			}
			else
			{
				format = Integer.toString(skillId);
			}
		}
		
		return "icon.skill" + format;
	}
}