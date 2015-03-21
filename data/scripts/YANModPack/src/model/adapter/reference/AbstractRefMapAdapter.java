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
package YANModPack.src.model.adapter.reference;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author HorridoJoho
 * @param <V> the reference type
 */
public abstract class AbstractRefMapAdapter<V> extends XmlAdapter<String[], Map<String, V>>
{
	private final Map<String, V> _map;
	
	protected AbstractRefMapAdapter(Map<String, V> map)
	{
		Objects.requireNonNull(map);
		_map = map;
	}
	
	@Override
	public final Map<String, V> unmarshal(String[] v)
	{
		final Map<String, V> listMap = new LinkedHashMap<>(v.length);
		for (String key : v)
		{
			final V ref = _map.get(key);
			Objects.requireNonNull(ref);
			listMap.put(key, ref);
		}
		
		return Collections.unmodifiableMap(listMap);
	}
	
	@Override
	public final String[] marshal(Map<String, V> v)
	{
		final String[] array = new String[v.size()];
		int i = 0;
		for (Map.Entry<String, V> e : v.entrySet())
		{
			final V ref = e.getValue();
			array[i] = getKey(ref);
			++i;
		}
		return array;
	}
	
	protected abstract String getKey(V ref);
}
