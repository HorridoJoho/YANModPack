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
package YANModPack.YANBuffer.src.model.adapter.direct;

import java.util.Map;

import YANModPack.YANBuffer.src.model.entity.BuffSkill;
import YANModPack.src.model.adapter.direct.AbstractMapAdapter;

/**
 * @author HorridoJoho
 */
public final class BuffMapAdapter extends AbstractMapAdapter<String, BuffSkill>
{
	@Override
	protected String getKey(BuffSkill v)
	{
		return v.id;
	}
	
	@Override
	protected BuffSkill[] mapToArray(Map<String, BuffSkill> v)
	{
		return v.values().toArray(new BuffSkill[0]);
	}
}
