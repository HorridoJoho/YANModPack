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

import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author HorridoJoho
 * @param <V> the reference type
 */
public abstract class AbstractRefAdapter<V> extends XmlAdapter<String, V>
{
	private final Map<String, V> _map;
	
	protected AbstractRefAdapter(Map<String, V> map)
	{
		Objects.requireNonNull(map);
		_map = map;
	}
	
	@Override
	public final V unmarshal(String v)
	{
		final V ref = _map.get(v);
		Objects.requireNonNull(ref);
		return ref;
	}
	
	@Override
	public final String marshal(V v)
	{
		return getKey(v);
	}
	
	protected abstract String getKey(V v);
}
