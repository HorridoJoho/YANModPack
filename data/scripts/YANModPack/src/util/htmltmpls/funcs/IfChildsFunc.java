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
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.HTMLTemplateUtils;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class IfChildsFunc extends HTMLTemplateFunc
{
	public static final IfChildsFunc INSTANCE = new IfChildsFunc();
	
	private static final Pattern _CHILDS_OF_PLACEHOLDER_PATTERN = Pattern.compile("\\s*[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*\\s*,");
	private static final Pattern _CHILD_PLACEHOLDER_PATTERN = Pattern.compile("\\s*[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*");
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
	
	private IfChildsFunc()
	{
		super("IFCHILDS", "ENDIFCHILDS", false);
	}
	
	@Override
	public HashMap<String, HTMLTemplatePlaceholder> handle(StringBuilder content, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc[] funcs)
	{
		try
		{
			Matcher matcher = _getMatcher(_CHILDS_OF_PLACEHOLDER_PATTERN, content, 0);
			String childsPlaceholderString = matcher.group().substring(0, matcher.group().length() - 1);
			HTMLTemplatePlaceholder childsPlaceholder = HTMLTemplateUtils.getPlaceholder(childsPlaceholderString, placeholders);
			if (childsPlaceholder == null)
			{
				content.setLength(0);
				return null;
			}
			
			matcher = _getMatcher(_CHILD_PLACEHOLDER_PATTERN, content, matcher.end());
			String childPlaceholderString = matcher.group().trim();
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
			
			for (Entry<String, HTMLTemplatePlaceholder> entry : childsPlaceholder.getChilds().entrySet())
			{
				HTMLTemplatePlaceholder childPlaceholder = entry.getValue().getChild(childPlaceholderString);
				if (childPlaceholder == null)
				{
					continue;
				}
				
				try
				{
					boolean ok = false;
					switch (op)
					{
						case "<":
							ok = Integer.parseInt(childPlaceholder.getValue()) < Integer.parseInt(rValue);
							break;
						case ">":
							ok = Integer.parseInt(childPlaceholder.getValue()) > Integer.parseInt(rValue);
							break;
						case "<=":
							ok = Integer.parseInt(childPlaceholder.getValue()) <= Integer.parseInt(rValue);
							break;
						case ">=":
							ok = Integer.parseInt(childPlaceholder.getValue()) >= Integer.parseInt(rValue);
							break;
						case "==":
							ok = childPlaceholder.getValue().equals(rValue);
							break;
						case "!=":
							ok = !childPlaceholder.getValue().equals(rValue);
							break;
						case "ENDS_WITH":
							ok = childPlaceholder.getValue().endsWith(rValue);
							break;
						case "STARTS_WITH":
							ok = childPlaceholder.getValue().startsWith(rValue);
							break;
					}
					
					if (!ok)
					{
						// condition is not met, no content to show
						content.setLength(0);
						return null;
					}
				}
				catch (Exception e)
				{
					// on an exception the types are incompatible with the operator, this function ignores such cases
				}
			}
			
			content.delete(0, findIndex);
		}
		catch (Exception e)
		{
			content.setLength(0);
		}
		
		return null;
	}
}
