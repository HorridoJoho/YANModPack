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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import YANModPack.src.util.htmltmpls.HTMLTemplateFunc;
import YANModPack.src.util.htmltmpls.HTMLTemplateParser;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.HTMLTemplateUtils;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class implements the following function syntax:<br>
 * [FOREACH(alias_placeholder_name IN placeholder_name DO text per iteration)ENDEACH]<br>
 * <br>
 * This construct is able to iterate over the childs of the "placeholder_name" placeholder.<br>
 * For each child in this placeholder, the text after "DO" is placed in the content.<br>
 * The current child is placed as an alias toplevel placeholder. This means, in this example<br>
 * you can use %alias_placeholder_name% inside the foreach block.
 * @author HorridoJoho
 */
public final class ForeachFunc extends HTMLTemplateFunc
{
	public static final ForeachFunc INSTANCE = new ForeachFunc();
	
	private static final Pattern _FIRST_PLACEHOLDER_PATTERN = Pattern.compile("\\s*[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*");
	private static final Pattern _IN_PATTERN = Pattern.compile("\\s*\\sIN\\s");
	private static final Pattern _SECOND_PLACEHOLDER_PATTERN = Pattern.compile("\\s*[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*");
	private static final Pattern _DO_PATTERN = Pattern.compile("\\s*\\sDO\\s");
	
	private static Matcher _getMatcher(Pattern pattern, StringBuilder content, int findIndex) throws Exception
	{
		Matcher m = pattern.matcher(content);
		if (!m.find(findIndex) || (m.start() > findIndex))
		{
			throw new Exception();
		}
		
		return m;
	}
	
	private ForeachFunc()
	{
		super("FOREACH", "ENDEACH", false);
	}
	
	@Override
	public HashMap<String, HTMLTemplatePlaceholder> handle(StringBuilder content, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc[] funcs)
	{
		try
		{
			Matcher matcher = _getMatcher(_FIRST_PLACEHOLDER_PATTERN, content, 0);
			String aliasPlaceholderName = matcher.group().trim();
			int findIndex = matcher.end();
			
			matcher = _getMatcher(_IN_PATTERN, content, findIndex);
			findIndex = matcher.end();
			
			matcher = _getMatcher(_SECOND_PLACEHOLDER_PATTERN, content, findIndex);
			Map<String, HTMLTemplatePlaceholder> childPlaceholders = HTMLTemplateUtils.getPlaceholderChilds(matcher.group().trim(), placeholders);
			findIndex = matcher.end();
			
			matcher = _getMatcher(_DO_PATTERN, content, findIndex);
			findIndex = matcher.end();
			
			content.delete(0, findIndex);
			HashMap<String, HTMLTemplatePlaceholder> newPlaceholders = new HashMap<>();
			newPlaceholders.putAll(placeholders);
			StringBuilder orgContent = new StringBuilder(content);
			StringBuilder modContent = new StringBuilder(content.length());
			content.setLength(0);
			// we don't need to save an overwritten placeholder, we create our own map
			for (Entry<String, HTMLTemplatePlaceholder> childPlaceholder : childPlaceholders.entrySet())
			{
				modContent.setLength(0);
				modContent.append(orgContent);
				newPlaceholders.put(aliasPlaceholderName, childPlaceholder.getValue().createAlias(aliasPlaceholderName));
				HTMLTemplateParser.fromStringBuilder(modContent, player, newPlaceholders, funcs);
				content.append(modContent);
			}
		}
		catch (Exception e)
		{
			content.setLength(0);
		}
		return null;
	}
}
