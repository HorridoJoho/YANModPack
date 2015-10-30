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

import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class YANBufferVoicedCommandHandler implements IVoicedCommandHandler
{
	private static final class SingletonHolder
	{
		protected static final YANBufferVoicedCommandHandler INSTANCE = new YANBufferVoicedCommandHandler();
	}
	
	public static final String VOICED_COMMAND = "yanbuffer";
	private static final String[] _VOICE_COMMAND_LIST =
	{
		VOICED_COMMAND
	};
	
	static YANBufferVoicedCommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	YANBufferVoicedCommandHandler()
	{
	}
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		YANBuffer.getInstance().executeCommand(activeChar, null, params);
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _VOICE_COMMAND_LIST;
	}
}
