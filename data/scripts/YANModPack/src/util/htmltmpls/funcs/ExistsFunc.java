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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import YANModPack.src.util.htmltmpls.HTMLTemplateFunc;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.HTMLTemplateUtils;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class ExistsFunc extends HTMLTemplateFunc
{
	public static final ExistsFunc INSTANCE = new ExistsFunc();
	
	private static final Pattern _NEGATE_PATTERN = Pattern.compile("\\s*!\\s*");
	private static final Pattern _PLACEHOLDER_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*\\s*,");
	
	private ExistsFunc()
	{
		super("EXISTS", "ENDEXISTS", false);
	}
	
	private static Matcher _getMatcher(Pattern pattern, StringBuilder content, int findIndex) throws Exception
	{
		Matcher m = pattern.matcher(content);
		if (!m.find(findIndex) || (m.start() > findIndex))
		{
			throw new Exception();
		}
		
		return m;
	}
	
	@Override
	public Map<String, HTMLTemplatePlaceholder> handle(StringBuilder content, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc[] funcs)
	{
		try
		{
			boolean negate = false;
			Matcher m = null;
			
			try
			{
				m = _getMatcher(_NEGATE_PATTERN, content, 0);
				negate = true;
			}
			catch (Exception e)
			{
				// ignore this exception, negate is optional
			}
			
			if (m != null)
			{
				m = _getMatcher(_PLACEHOLDER_PATTERN, content, m.end());
			}
			else
			{
				m = _getMatcher(_PLACEHOLDER_PATTERN, content, 0);
			}
			
			HTMLTemplatePlaceholder placeholder = HTMLTemplateUtils.getPlaceholder(m.group().substring(0, m.group().length() - 1).trim(), placeholders);
			if (((placeholder == null) && !negate) || ((placeholder != null) && negate))
			{
				content.setLength(0);
				return null;
			}
			
			content.delete(0, m.end());
		}
		catch (Exception e)
		{
			content.setLength(0);
		}
		
		return null;
	}
	
}
