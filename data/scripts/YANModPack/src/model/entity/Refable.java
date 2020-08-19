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
package YANModPack.src.model.entity;

import java.util.Objects;

import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

/**
 * @author HorridoJoho
 */
public abstract class Refable implements IRefable<String>
{
	private String id;

	protected final transient HTMLTemplatePlaceholder placeholder;
	
	protected Refable()
	{
		id = null;
		
		placeholder = new HTMLTemplatePlaceholder("placeholder", null);
	}
	
	protected Refable(String id)
	{
		Objects.requireNonNull(id);
		this.id = id;
		
		placeholder = new HTMLTemplatePlaceholder("placeholder", null);
	}
	
	public void afterDeserialize()
	{
		placeholder.addChild("ident", id.toString());
	}
	
	@Override
	public final String getId()
	{
		return id;
	}
	
	public final HTMLTemplatePlaceholder getPlaceholder()
	{
		return placeholder;
	}
}
