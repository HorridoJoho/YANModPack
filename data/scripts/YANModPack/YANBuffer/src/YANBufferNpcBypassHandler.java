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
package YANModPack.YANBuffer.src;

import com.l2jserver.gameserver.handler.IBypassHandler;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public class YANBufferNpcBypassHandler implements IBypassHandler
{
	private static final class SingletonHolder
	{
		protected static final YANBufferNpcBypassHandler INSTANCE = new YANBufferNpcBypassHandler();
	}
	
	public static final String BYPASS = "YANBufferNpc";
	private static final String[] _BYPASS_LIST = new String[]
	{
		BYPASS
	};
	
	static YANBufferNpcBypassHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	YANBufferNpcBypassHandler()
	{
	}
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if ((target == null) || !target.isNpc())
		{
			return false;
		}
		
		YANBuffer.getInstance().executeCommand(activeChar, (L2Npc) target, command.substring(BYPASS.length()).trim());
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return _BYPASS_LIST;
	}
}
