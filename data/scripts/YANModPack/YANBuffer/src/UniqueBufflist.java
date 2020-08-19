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

import java.util.LinkedList;

import YANModPack.YANBuffer.src.YANBufferData.BuffType;
import YANModPack.YANBuffer.src.model.entity.BuffSkill;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;


/**
 * This class is here so we can actually get the name of this list and make placeholder adjustments easily while keeping outside code cleaner
 * @author HorridoJoho
 */
public class UniqueBufflist extends LinkedList<BuffSkill>
{
	private static final long serialVersionUID = -2586607798277226501L;
	
	public final int ulistId;
	public final String ulistName;
	public int numBuffs;
	public int numSongsDances;
	public HTMLTemplatePlaceholder placeholder;
	
	UniqueBufflist(int ulistId, String ulistName)
	{
		this.ulistId = ulistId;
		this.ulistName = ulistName;
		this.numBuffs = 0;
		this.numSongsDances = 0;
		this.placeholder = new HTMLTemplatePlaceholder("unique", null).addChild("buffs", null).addChild("name", ulistName).addChild("num_buffs", "0").addChild("num_songs_dances", "0");
	}
	
	@Override
	public boolean add(BuffSkill e)
	{
		if (super.add(e))
		{
			if (e.getType() == BuffType.BUFF)
			{
				++this.numBuffs;
				this.placeholder.getChild("num_buffs").setValue(String.valueOf(Integer.parseInt(this.placeholder.getChild("num_buffs").getValue()) + 1));
			}
			else
			{
				++this.numSongsDances;
				this.placeholder.getChild("num_songs_dances").setValue(String.valueOf(Integer.parseInt(this.placeholder.getChild("num_songs_dances").getValue()) + 1));
			}
			this.placeholder.getChild("buffs").addAliasChild(e.getId(), e.getPlaceholder());
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean remove(Object o)
	{
		if (super.remove(o))
		{
			switch (((BuffSkill) o).getType())
			{
				case BUFF:
					--numBuffs;
					break;
				case SONG_DANCE:
					--numSongsDances;
					break;
			}
			
			this.placeholder = new HTMLTemplatePlaceholder("unique", null).addChild("buffs", null).addChild("name", this.ulistName).addChild("num_buffs", String.valueOf(numBuffs)).addChild("num_songs_dances", String.valueOf(numSongsDances));
			for (BuffSkill buff : this)
			{
				this.placeholder.getChild("buffs").addAliasChild(buff.getId(), buff.getPlaceholder());
			}
			return true;
		}
		
		return false;
	}
}
