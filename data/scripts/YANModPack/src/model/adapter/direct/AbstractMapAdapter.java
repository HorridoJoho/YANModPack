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
package YANModPack.src.model.adapter.direct;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author HorridoJoho
 * @param <K> The key type of the map
 * @param <V> The value type of the map
 */
public abstract class AbstractMapAdapter<K, V> extends XmlAdapter<V[], Map<K, V>>
{
	@Override
	public final Map<K, V> unmarshal(V[] v)
	{
		if (v == null)
		{
			return null;
		}
		
		Map<K, V> mapped = new LinkedHashMap<>();
		for (V e : v)
		{
			mapped.put(getKey(e), e);
		}
		return Collections.unmodifiableMap(mapped);
	}
	
	@Override
	public final V[] marshal(Map<K, V> v)
	{
		if (v == null)
		{
			return null;
		}
		return mapToArray(v);
	}
	
	protected abstract K getKey(V v);
	
	protected abstract V[] mapToArray(Map<K, V> v);
}
