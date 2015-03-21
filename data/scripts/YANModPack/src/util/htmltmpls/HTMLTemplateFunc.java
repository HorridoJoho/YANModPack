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

import java.util.Map;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class represents a template function.<br>
 * It has a sequence start and end.<br>
 * ----- Example -----<br>
 * -- startSequence = "INC"<br>
 * -- endSequence = "ENDINC"<br>
 * We have a template file template.tmpl: [INC(template2.tmpl)ENDINC]<br>
 * Now when the handlers {@link #handle(StringBuilder, L2PcInstance, Map, HTMLTemplateFunc[])} method is called, contents will contain "template2.tmpl"<br>
 * @author HorridoJoho
 */
public abstract class HTMLTemplateFunc
{
	/** how the function sequence starts */
	private final String _sequenceStart;
	/** how the function sequence ends */
	private final String _sequenceEnd;
	/** flag to determine if the template function needs processing of it's contents before contents are passed to the {@link #handle(StringBuilder, L2PcInstance, Map, HTMLTemplateFunc[])} method */
	private final boolean _requiresPreprocessing;
	
	/**
	 * Protected constructor for template function implementations. In a template document
	 * @param sequenceStart how the template function sequence starts (in a template document you use [sequenceStart(
	 * @param sequenceEnd how the template function sequence starts (in a template document you use )sequenceEnd]
	 * @param requiresPreprocessing flag to determine if the template function needs processing of it's contents before contents are passed to the {@link #handle(StringBuilder, L2PcInstance, Map, HTMLTemplateFunc[])}
	 */
	protected HTMLTemplateFunc(String sequenceStart, String sequenceEnd, boolean requiresPreprocessing)
	{
		_sequenceStart = "[" + sequenceStart + "(";
		_sequenceEnd = ")" + sequenceEnd + "]";
		_requiresPreprocessing = requiresPreprocessing;
	}
	
	/**
	 * @return the sequence this function starts with in a template document
	 */
	public final String getSequenceStart()
	{
		return _sequenceStart;
	}
	
	/**
	 * @return the sequence this function ends with in a template document
	 */
	public final String getSequenceEnd()
	{
		return _sequenceEnd;
	}
	
	/**
	 * @return true when the handler needs the contents preprocessed by the template engine before it is passed to the {@link #handle(StringBuilder, L2PcInstance, Map, HTMLTemplateFunc[])} metod, false otherwise
	 */
	public final boolean requiresPreprocessing()
	{
		return _requiresPreprocessing;
	}
	
	/**
	 * Called by template parser to give the function the possibility to<br>
	 * create new placeholders and modify the contents of the function<br>
	 * in the template document.
	 * @param content the content which can be modified by the handler
	 * @param player the player the template is processed for
	 * @param placeholders the currently avilable placeholders as unmodifyable map
	 * @param funcs supported functions the template is parsed with
	 * @return placeholder to add to the currently available placeholders, added before the content is processed after this call, and removed again after content processing
	 */
	public abstract Map<String, HTMLTemplatePlaceholder> handle(StringBuilder content, L2PcInstance player, Map<String, HTMLTemplatePlaceholder> placeholders, HTMLTemplateFunc[] funcs);
}