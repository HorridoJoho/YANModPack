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

import YANModPack.YANBuffer.src.model.adapter.direct.BuffMapAdapter;
import YANModPack.YANBuffer.src.model.adapter.reference.BuffSkillRefMapAdapter;
import YANModPack.YANBuffer.src.model.entity.BuffSkill;

/**
 * @author HorridoJoho
 */
@XmlRootElement(name = "buffs")
public final class BuffSkills
{
	@XmlElement(name = "buff")
	@XmlJavaTypeAdapter(BuffMapAdapter.class)
	private final Map<String, BuffSkill> _buffs;
	
	public BuffSkills()
	{
		_buffs = null;
	}
	
	public BuffSkill get(String ident)
	{
		return _buffs.get(ident);
	}
	
	public BuffSkillRefMapAdapter getRefAdapter()
	{
		return new BuffSkillRefMapAdapter(_buffs);
	}
}
