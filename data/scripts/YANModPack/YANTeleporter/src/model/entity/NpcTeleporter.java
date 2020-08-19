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
package YANModPack.YANTeleporter.src.model.entity;

import YANModPack.YANTeleporter.src.YANTeleporterBypassHandler;
import YANModPack.YANTeleporter.src.model.TeleporterConfig;
import YANModPack.src.model.entity.IRefable;

import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * @author HorridoJoho
 */
public final class NpcTeleporter extends AbstractTeleporter implements IRefable<Integer>
{
	private int npcId;
	private boolean directFirstTalk;
	
	public NpcTeleporter()
	{
		super(YANTeleporterBypassHandler.BYPASS);
		npcId = 0;
		directFirstTalk = false;
	}
	
	@Override
	public void afterDeserialize(TeleporterConfig config)
	{
		super.afterDeserialize(config);
		
		placeholder.addChild("ident", String.valueOf(npcId));
	}
	
	public L2NpcTemplate getNpc()
	{
		return NpcData.getInstance().getTemplate(npcId);
	}
	
	public int getNpcId()
	{
		return npcId;
	}
	
	public boolean getDirectFirstTalk()
	{
		return directFirstTalk;
	}
	
	@Override
	public final String getName()
	{
		return getNpc().getName();
	}
	
	@Override
	public final Integer getId()
	{
		return npcId;
	}
}
