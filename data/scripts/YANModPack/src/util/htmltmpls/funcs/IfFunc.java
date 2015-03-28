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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import YANModPack.src.util.htmltmpls.HTMLTemplateFunc;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.HTMLTemplateUtils;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class implements the following function syntax:<br>
 * [IF(placeholder_name == "text in string" THEN text when the condition matches)ENDIF]<br>
 * [IF(placeholder_name == another_placeholder_name THEN text when the condition matches)ENDIF]<br>
 * [IF(int_placeholder_name > another_int_placeholder_name THEN text when the condition matches)ENDIF]<br>
 * <br>
 * First is always a placeholder name.<br>
 * <br>
 * Second comes one of the following operators:<br>
 * <, >, <=, >=, !=, ==, STARTS_WITH or ENDS_WITH, where <, >, <= and >= are only to be used with placeholders/strings<br>
 * which have a numeric value.<br>
 * <br>
 * Third comes either a placeholder name or a string("text in string").<br>
 * <br>
 * After the "THEN" word comes the text to place in the content when the condition is met.
 * @author HorridoJoho
 */
public final class IfFunc extends HTMLTemplateFunc
{
	public static final IfFunc INSTANCE = new IfFunc();
	
	private static final Pattern _LVALUE_PATTERN = Pattern.compile("\\s*[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*");
	private static final Pattern _OP_PATTERN = Pattern.compile("\\s*(<|>|<=|>=|==|!=|\\sENDS_WITH\\s|\\sSTARTS_WITH\\s)\\s*");
	private static final Pattern _RVALUE_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*|\"(\\\\.|\\\\\\s|\\s|[^\\\\\"])*\")");
	private static final Pattern _THEN_PATTERN = Pattern.compile("\\s*\\sTHEN\\s");
	
	private static Matcher _getMatcher(Pattern pattern, StringBuilder content, int findIndex) throws Exception
	{
		Matcher m = pattern.matcher(content);
		if (!m.find(findIndex) || (m.start() > findIndex))
		{
			throw new Exception();
		}
		
		return m;
	}
	
	private IfFunc()
	{
		super("IF", "ENDIF", false);
	}
	
	@Override
	public HashMap<String, HTMLTemplatePlaceholder> handle(StringBuilder content, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc[] funcs)
	{
		try
		{
			Matcher matcher = _getMatcher(_LVALUE_PATTERN, content, 0);
			String lValue = HTMLTemplateUtils.getPlaceholderValue(matcher.group().trim(), placeholders);
			int findIndex = matcher.end();
			
			matcher = _getMatcher(_OP_PATTERN, content, findIndex);
			String op = matcher.group().trim();
			findIndex = matcher.end();
			
			matcher = _getMatcher(_RVALUE_PATTERN, content, findIndex);
			String rValue = matcher.group();
			if (rValue.charAt(0) == '"')
			{
				rValue = rValue.substring(1, rValue.length() - 1);
			}
			else
			{
				rValue = HTMLTemplateUtils.getPlaceholderValue(rValue, placeholders);
			}
			findIndex = matcher.end();
			
			matcher = _getMatcher(_THEN_PATTERN, content, findIndex);
			findIndex = matcher.end();
			
			boolean ok = false;
			switch (op)
			{
				case "<":
					ok = Integer.parseInt(lValue) < Integer.parseInt(rValue);
					break;
				case ">":
					ok = Integer.parseInt(lValue) > Integer.parseInt(rValue);
					break;
				case "<=":
					ok = Integer.parseInt(lValue) <= Integer.parseInt(rValue);
					break;
				case ">=":
					ok = Integer.parseInt(lValue) >= Integer.parseInt(rValue);
					break;
				case "==":
					ok = lValue.equals(rValue);
					break;
				case "!=":
					ok = !lValue.equals(rValue);
					break;
				case "ENDS_WITH":
					ok = lValue.endsWith(rValue);
					break;
				case "STARTS_WITH":
					ok = lValue.startsWith(rValue);
					break;
			}
			
			if (ok)
			{
				// this ensures only the replacement content is left
				content.delete(0, findIndex);
			}
			else
			{
				// condition is not met, no content to show
				content.setLength(0);
				return null;
			}
		}
		catch (Exception e)
		{
			content.setLength(0);
		}
		
		return null;
	}
}
