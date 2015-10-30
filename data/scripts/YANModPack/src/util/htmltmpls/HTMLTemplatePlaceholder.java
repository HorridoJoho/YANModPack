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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is the class for the buildin value placeholders.<br>
 * It has a name, a value and can contain child placeholders.<br>
 * To reference the value of a placeholder in a template document<br>
 * you use <b>%placeholder_name%</b>. To reference the value of a child<br>
 * placeholder you use <b>%placeholder_name.child_placeholder_name%.</b>
 * @author HorridoJoho
 */
public final class HTMLTemplatePlaceholder
{
	/** the name of this placeholder */
	private final String _name;
	/** the value of this placeholder */
	private volatile String _value;
	/** the child placeholders of this placeholder */
	private final Map<String, HTMLTemplatePlaceholder> _childs;
	
	/**
	 * Public constructor to create a new placeholder
	 * @param name the name of the new placeholder
	 * @param value the value of the new placeholder
	 */
	public HTMLTemplatePlaceholder(String name, String value)
	{
		this(name, value, new LinkedHashMap<String, HTMLTemplatePlaceholder>());
	}
	
	/**
	 * Private constructor to create alias placeholders of other placeholders
	 * @param name the name of the alias placeholder
	 * @param value the value of the alias placeholder
	 * @param childs the childs of the alias placeholder
	 */
	private HTMLTemplatePlaceholder(String name, String value, Map<String, HTMLTemplatePlaceholder> childs)
	{
		_name = name;
		_value = value;
		_childs = childs;
	}
	
	/**
	 * Creates an alias for this placeholder.<br>
	 * An alias placeholder will hold the reference to the childs map from the original placeholder. This means, adding a new child to the alias will also add the child to the original placeholder and vice versa.
	 * @param name name of the alias placeholder
	 * @return the newly created alias placeholder
	 */
	public HTMLTemplatePlaceholder createAlias(String name)
	{
		return new HTMLTemplatePlaceholder(name, _value, _childs);
	}
	
	/**
	 * Adds a child placeholder to this placeholder.
	 * @param name the name of the new child placeholder
	 * @param value the value of the new child placeholder
	 * @return this placeholder
	 */
	public HTMLTemplatePlaceholder addChild(String name, String value)
	{
		_childs.put(name, new HTMLTemplatePlaceholder(name, value));
		return this;
	}
	
	public HTMLTemplatePlaceholder addAliasChild(String aliasName, HTMLTemplatePlaceholder placeholder)
	{
		_childs.put(aliasName, placeholder.createAlias(aliasName));
		return this;
	}
	
	public void setValue(String value)
	{
		_value = value;
	}
	
	/**
	 * @return the name of this placeholder
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @return the value of this placeholder
	 */
	public String getValue()
	{
		return _value;
	}
	
	/**
	 * Method to get a child placeholder of this placeholder by name
	 * @param name the name of the child placeholder to find
	 * @return the child placeholder
	 */
	public HTMLTemplatePlaceholder getChild(String name)
	{
		return HTMLTemplateUtils.getPlaceholder(name, _childs);
	}
	
	/**
	 * @return the child placeholder map of this placeholder as unmodifyable map
	 */
	public Map<String, HTMLTemplatePlaceholder> getChilds()
	{
		return Collections.unmodifiableMap(_childs);
	}
	
	/**
	 * @return the count of child placeholders in this placeholder
	 */
	public int getChildsSize()
	{
		return _childs.size();
	}
}