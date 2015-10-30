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
package YANModPack.src.model.adapter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import YANModPack.src.model.entity.IDefinition;
import YANModPack.src.model.wrapper.IListWrapper;

/**
 * @author HorridoJoho
 * @param <K> The key type of the map
 * @param <V> The value type of the map
 * @param <W> The wrapper type
 */
public abstract class AbstractListToMap<K, V extends IDefinition<K>, W extends IListWrapper<V>> extends XmlAdapter<W, Map<K, V>>
{
	@Override
	public final Map<K, V> unmarshal(W v)
	{
		if (v == null)
		{
			return Collections.unmodifiableMap(new LinkedHashMap<K, V>());
		}
		
		final V[] list = v.getList();
		final LinkedHashMap<K, V> map = new LinkedHashMap<>(list.length);
		for (V e : list)
		{
			map.put(e.getIdentifier(), e);
		}
		return Collections.unmodifiableMap(map);
	}
}
