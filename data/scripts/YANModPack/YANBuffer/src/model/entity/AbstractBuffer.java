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
package YANModPack.YANBuffer.src.model.entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import YANModPack.YANBuffer.src.model.BufferConfig;
import YANModPack.src.model.entity.YANModServer;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public abstract class AbstractBuffer extends YANModServer
{
	private boolean canHeal;
	private boolean canCancel;
	private List<String> presetBuffCategories;
	private List<String> buffCategories;

	private transient Map<String, BuffCategory> presetBuffCatsMap;
	private transient Map<String, BuffCategory> buffCatsMap;
	
	public AbstractBuffer(String bypassPrefix)
	{
		super(bypassPrefix, "buffer");
		
		canHeal = false;
		canCancel = false;

		presetBuffCatsMap = new LinkedHashMap<>();
		buffCatsMap = new LinkedHashMap<>();
	}
	
	public void afterDeserialize(BufferConfig config)
	{
		super.afterDeserialize();

		for (String id : presetBuffCategories)
		{
			presetBuffCatsMap.put(id, config.getGlobal().getCategories().get(id));
		}

		for (String id : buffCategories)
		{
			buffCatsMap.put(id, config.getGlobal().getCategories().get(id));
		}
		
		if (canHeal)
		{
			placeholder.addChild("can_heal", null);
		}
		if (canCancel)
		{
			placeholder.addChild("can_cancel", null);
		}
		if (!presetBuffCategories.isEmpty())
		{
			HTMLTemplatePlaceholder presetBufflistsPlaceholder = placeholder.addChild("presets", null).getChild("presets");
			for (Entry<String, BuffCategory> presetBufflist : presetBuffCatsMap.entrySet())
			{
				presetBufflistsPlaceholder.addAliasChild(String.valueOf(presetBufflistsPlaceholder.getChildsSize()), presetBufflist.getValue().getPlaceholder());
			}
		}
		if (!buffCategories.isEmpty())
		{
			HTMLTemplatePlaceholder buffCatsPlaceholder = placeholder.addChild("categories", null).getChild("categories");
			for (Entry<String, BuffCategory> buffCat : buffCatsMap.entrySet())
			{
				buffCatsPlaceholder.addAliasChild(String.valueOf(buffCatsPlaceholder.getChildsSize()), buffCat.getValue().getPlaceholder());
			}
		}
	}
	
	public final boolean getCanHeal()
	{
		return canHeal;
	}
	
	public final boolean getCanCancel()
	{
		return canCancel;
	}
	
	public Map<String, BuffCategory> getPresetBuffCats()
	{
		return presetBuffCatsMap;
	}
	
	public final Map<String, BuffCategory> getBuffCats()
	{
		return buffCatsMap;
	}
}
