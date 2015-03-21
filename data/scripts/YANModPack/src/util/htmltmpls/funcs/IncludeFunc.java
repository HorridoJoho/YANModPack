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
package YANModPack.src.util.htmltmpls.funcs;

import java.util.HashMap;
import java.util.Map;

import YANModPack.src.util.htmltmpls.HTMLTemplateFunc;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class IncludeFunc extends HTMLTemplateFunc
{
	public static final IncludeFunc INSTANCE = new IncludeFunc();
	
	private IncludeFunc()
	{
		super("INC", "ENDINC", true);
	}
	
	@Override
	public HashMap<String, HTMLTemplatePlaceholder> handle(StringBuilder content, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc[] funcs)
	{
		String fileContent = HtmCache.getInstance().getHtm(player != null ? player.getHtmlPrefix() : null, content.toString());
		if (fileContent != null)
		{
			content.ensureCapacity(fileContent.length());
			content.setLength(0);
			content.append(fileContent);
		}
		
		return null;
	}
}
