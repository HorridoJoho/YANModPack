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
package YANModPack.YANTeleporter.src.model.entity;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;

import YANModPack.src.model.entity.YANModProduct;

/**
 * @author HorridoJoho
 */
public class SoloTeleport extends YANModProduct
{
	@XmlAttribute(name = "name", required = true)
	public final String name;
	@XmlAttribute(name = "x", required = true)
	public final int x;
	@XmlAttribute(name = "y", required = true)
	public final int y;
	@XmlAttribute(name = "z", required = true)
	public final int z;
	@XmlAttribute(name = "heading", required = true)
	public final int heading;
	@XmlAttribute(name = "random_offset")
	public final int randomOffset;
	@XmlAttribute(name = "instance")
	public final String instance;
	
	public SoloTeleport()
	{
		name = null;
		x = 0;
		y = 0;
		z = 0;
		heading = 0;
		randomOffset = 0;
		instance = "";
	}
	
	@Override
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent)
	{
		super.afterUnmarshal(unmarshaller, parent);
		
		placeholder.addChild("name", name);
	}
}
