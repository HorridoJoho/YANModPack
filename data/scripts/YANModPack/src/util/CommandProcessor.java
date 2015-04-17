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
package YANModPack.src.util;

import java.util.Objects;

/**
 * @author HorridoJoho
 */
public final class CommandProcessor
{
	private String _remaining;
	
	public CommandProcessor(String command)
	{
		Objects.requireNonNull(command);
		_remaining = command;
	}
	
	public boolean matchAndRemove(String... expectations)
	{
		Objects.requireNonNull(expectations);
		for (String expectation : expectations)
		{
			Objects.requireNonNull(expectation);
			if (!expectation.isEmpty() && _remaining.startsWith(expectation))
			{
				_remaining = _remaining.substring(expectation.length());
				return true;
			}
		}
		return false;
	}
	
	public String[] splitRemaining(String regex)
	{
		return _remaining.split(regex);
	}
	
	public String getRemaining()
	{
		return _remaining;
	}
}
