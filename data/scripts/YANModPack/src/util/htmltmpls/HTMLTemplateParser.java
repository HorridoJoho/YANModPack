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
package YANModPack.src.util.htmltmpls;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class HTMLTemplateParser
{
	/** pattern to find placeholder references */
	private static final Pattern _PLACEHOLDER_PATTERN = Pattern.compile("%[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*%");
	
	public static String fromCache(String path, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc... funcs)
	{
		return fromCache(path, null, placeholders, funcs);
	}
	
	public static String fromCache(String path, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc... funcs)
	{
		String string = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), path);
		if (string == null)
		{
			return null;
		}
		StringBuilder builder = new StringBuilder(string);
		fromStringBuilder(builder, player, placeholders, funcs);
		return builder.toString();
	}
	
	/**
	 * Method to process a template. The string is directly modifyed and will contain the results of the template processing.
	 * @param string the template content
	 * @param player the player the template is processed for
	 * @param placeholders a map of placeholders(map has to be modifyable)
	 * @param funcs the functions to use while processing the template
	 */
	public static void fromStringBuilder(StringBuilder string, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc... funcs)
	{
		// System.out.println("fromStringBuilder(" + string + ") {");
		if (string == null)
		{
			return;
		}
		
		int indexOfOffset = 0;
		
		while (indexOfOffset < (string.length() - 1))
		{
			// System.out.println("-------------------------------------------");
			// System.out.println("indexOfOffset=" + indexOfOffset);
			// find the first position of a placeholder or a custom func
			Matcher placeholderMatcher = _PLACEHOLDER_PATTERN.matcher(string);
			int nextFuncStartOffset = -1;
			int nextFuncEndOffset = -1;
			if (placeholderMatcher.find(indexOfOffset))
			{
				nextFuncStartOffset = placeholderMatcher.start();
				nextFuncEndOffset = placeholderMatcher.end();
			}
			
			HTMLTemplateFunc nextFunc = null;
			for (HTMLTemplateFunc func : funcs)
			{
				int funcOffset = string.indexOf(func.getSequenceStart(), indexOfOffset);
				if ((funcOffset > -1) && ((nextFuncStartOffset == -1) || (funcOffset < nextFuncStartOffset)))
				{
					nextFuncStartOffset = funcOffset;
					nextFuncEndOffset = HTMLTemplateUtils.findSequenceEnd(string, nextFuncStartOffset + func.getSequenceStart().length(), func);
					nextFunc = func;
				}
			}
			
			// System.out.println("nextFuncStartOffset=" + nextFuncStartOffset + "\nnextFuncEndOffset=" + nextFuncEndOffset + "\nfunc?" + (nextFunc != null));
			
			if (nextFuncStartOffset == -1)
			{
				break;
			}
			else if (nextFunc == null)
			{
				String placeholderString = placeholderMatcher.group().substring(1, placeholderMatcher.group().length() - 1);
				// System.out.println("Placeholder: " + placeholderString);
				HTMLTemplatePlaceholder placeholder = HTMLTemplateUtils.getPlaceholder(placeholderString, placeholders);
				
				if (placeholder != null)
				{
					// System.out.println("Value: " + placeholder.getValue());
					string.replace(nextFuncStartOffset, nextFuncEndOffset, placeholder.getValue());
					// 2 !!! placeholder replacement can contain more placeholders and func sequences start so we set the index to search to the start of the placeholder
					indexOfOffset = nextFuncStartOffset;
				}
				else
				// skip placeholder?
				{
					// if placeholder can not be found, just remove it from the string
					// 1 <<< string.delete(nextFuncStartOffset, nextFuncEndOffset);
					
					// 2 !!! l2j compatible mode, we don't want to manually add things like %objectId% placeholders all the time
					indexOfOffset += placeholderMatcher.end() - placeholderMatcher.start();
				}
				
				// placeholder replacement can contain more placeholders and func sequences start so we set the index to search to the start of the placeholder
				// 1 <<< indexOfOffset = nextFuncStartOffset;
			}
			else
			{
				if (nextFuncEndOffset == -1)
				{
					// this is to ignore the starting sequnces which have no ending sequence
					++indexOfOffset;
				}
				else
				{
					// System.out.println("Func: " + nextFunc.getClass().getName());
					
					StringBuilder content = new StringBuilder(string.subSequence(nextFuncStartOffset + nextFunc.getSequenceStart().length(), nextFuncEndOffset - nextFunc.getSequenceEnd().length()));
					
					// the func needs preprocessing?
					if (nextFunc.requiresPreprocessing())
					{
						fromStringBuilder(content, player, placeholders, funcs);
					}
					
					Map<String, HTMLTemplatePlaceholder> tmpPlaceholders = nextFunc.handle(content, player, placeholders == null ? null : Collections.unmodifiableMap(placeholders), funcs);
					
					// add new entries and replace entries(temp)
					if (tmpPlaceholders != null)
					{
						for (HTMLTemplatePlaceholder newPlaceholder : tmpPlaceholders.values())
						{
							if (placeholders == null)
							{
								placeholders = new HashMap<>();
							}
							tmpPlaceholders.put(newPlaceholder.getName(), placeholders.put(newPlaceholder.getName(), newPlaceholder));
						}
					}
					
					fromStringBuilder(content, player, placeholders, funcs);
					string.replace(nextFuncStartOffset, nextFuncEndOffset, content.toString());
					
					// remove entries which were new and restore old entries
					if ((tmpPlaceholders != null) && (placeholders != null))
					{
						for (Entry<String, HTMLTemplatePlaceholder> oldPlaceholder : tmpPlaceholders.entrySet())
						{
							if (oldPlaceholder.getValue() == null)
							{
								placeholders.remove(oldPlaceholder.getKey());
							}
							else
							{
								placeholders.put(oldPlaceholder.getKey(), oldPlaceholder.getValue());
							}
						}
					}
					
					// set the current offset to the next func sequence start found, replaced content can contain more placeholders and funcs
					indexOfOffset = nextFuncStartOffset;
				}
			}
		}
		
		// System.out.println("}");
	}
}