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
package com.l2jserver.datapack.yanmodpack.teleporter;

import com.l2jserver.gameserver.handler.IItemHandler;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author HorridoJoho
 */
public final class YANTeleporterItemHandler implements IItemHandler
{
	private static final class SingletonHolder
	{
		protected static final YANTeleporterItemHandler INSTANCE = new YANTeleporterItemHandler();
	}
	
	static YANTeleporterItemHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	YANTeleporterItemHandler()
	{
	}
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			return false;
		}
		
		YANTeleporter.getInstance().executeCommand((L2PcInstance) playable, null, null);
		return true;
	}
}
