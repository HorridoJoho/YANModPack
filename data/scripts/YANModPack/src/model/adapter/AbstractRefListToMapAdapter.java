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
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import YANModPack.src.model.wrapper.ReferenceListWrapper;

/**
 * @author HorridoJoho
 * @param <V> the reference type
 */
public abstract class AbstractRefListToMapAdapter<V> extends XmlAdapter<ReferenceListWrapper, Map<String, V>>
{
	private final Map<String, V> _map;
	
	protected AbstractRefListToMapAdapter(Map<String, V> map)
	{
		Objects.requireNonNull(map);
		_map = map;
	}
	
	@Override
	public final Map<String, V> unmarshal(ReferenceListWrapper v)
	{
		if (v == null)
		{
			System.out.println("AbstractRefListToMapAdapter#unmarshal of " + getClass().getSimpleName() + ": v=null");
			return Collections.unmodifiableMap(new LinkedHashMap<>());
		}
		
		System.out.println("AbstractRefListToMapAdapter#unmarshal of " + getClass().getSimpleName() + ": v.refs=" + v.refs);
		final LinkedHashMap<String, V> map = new LinkedHashMap<>(v.refs.length);
		for (String key : v.refs)
		{
			final V ref = _map.get(key);
			Objects.requireNonNull(ref);
			map.put(key, ref);
		}
		return Collections.unmodifiableMap(map);
	}
	
	@Override
	public final ReferenceListWrapper marshal(Map<String, V> v)
	{
		final String[] list = new String[v.size()];
		int i = 0;
		for (Map.Entry<String, V> e : v.entrySet())
		{
			final V ref = e.getValue();
			list[i] = getKey(ref);
			++i;
		}
		return new ReferenceListWrapper(list);
	}
	
	protected abstract String getKey(V ref);
}
