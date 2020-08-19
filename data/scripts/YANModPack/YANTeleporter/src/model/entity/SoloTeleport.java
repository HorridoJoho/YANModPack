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

import YANModPack.YANTeleporter.src.model.TeleporterConfig;
import YANModPack.src.model.entity.YANModProduct;

/**
 * @author HorridoJoho
 */
public class SoloTeleport extends YANModProduct
{
	private String name;
	private int x;
	private int y;
	private int z;
	private int heading;
	private int randomOffset;
	private String instance;
	
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
	
	public void afterDeserialize(TeleporterConfig config)
	{
		super.afterDeserialize();
		
		placeholder.addChild("name", name);
	}
	
	public String getName() {
		return name;
	}

	public int getRandomOffset() {
		return randomOffset;
	}

	public int getHeading() {
		return heading;
	}

	public String getInstance() {
		return instance;
	}

	public int getZ() {
		return z;
	}

	public int getY() {
		return y;
	}

	public int getX() {
		return x;
	}
}
