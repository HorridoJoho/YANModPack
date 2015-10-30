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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author HorridoJoho
 */
@XmlRootElement(name = "yanbuffer")
@XmlAccessorType(XmlAccessType.FIELD)
public final class BufferConfig
{
	@XmlElement(name = "heal_cooldown", required = true)
	public final int healCooldown;
	@XmlElement(name = "max_unique_lists", required = true)
	public final int maxUniqueLists;
	@XmlElement(name = "unique_max_buffs", required = true)
	public final int uniqueMaxBuffs;
	@XmlElement(name = "unique_max_songs_dances", required = true)
	public final int uniqueMaxSongsDances;
	@XmlElement(name = "debug", required = true)
	public final boolean debug;
	
	public BufferConfig()
	{
		healCooldown = 0;
		maxUniqueLists = 0;
		uniqueMaxBuffs = 0;
		uniqueMaxSongsDances = 0;
		debug = false;
	}
}
