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

import YANModPack.YANBuffer.src.model.entity.BuffCategory;
import YANModPack.YANBuffer.src.model.entity.BuffSkill;

/**
 * @author HorridoJoho
 */
public final class GlobalConfig
{
	private int healCooldown;
	private int maxUniqueLists;
	private int uniqueMaxBuffs;
	private int uniqueMaxSongsDances;
	private boolean debug;
	
	private Map<String, BuffSkill> buffs;
	private Map<String, BuffCategory> categories;
	
	public GlobalConfig()
	{
		healCooldown = 0;
		maxUniqueLists = 0;
		uniqueMaxBuffs = 0;
		uniqueMaxSongsDances = 0;
		debug = false;
	}
	
	public void afterDeserilize(BufferConfig config)
	{
		for (BuffSkill buff : buffs.values())
		{
			buff.afterDeserialize(config);
		}

		for (BuffCategory category : categories.values())
		{
			category.afterDeserialize(config);
		}
	}
	
	public BuffSkill getBuff(String id)
	{
		return buffs.get(id);
	}
	
	public int getHealCooldown()
	{
		return healCooldown;
	}
	
	public int getMaxUniqueLists()
	{
		return maxUniqueLists;
	}
	
	public int getUniqueMaxBuffs()
	{
		return uniqueMaxBuffs;
	}
	
	public int getUniqueMaxSongsDances()
	{
		return uniqueMaxSongsDances;
	}
	
	public boolean getDebug()
	{
		return debug;
	}
	
	public final Map<String, BuffSkill> getBuffs()
	{
		return buffs;
	}
	
	public final Map<String, BuffCategory> getCategories()
	{
		return categories;
	}
}