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
package YANModPack.YANTeleporter.src;

import java.util.logging.Logger;

import YANModPack.YANTeleporter.src.model.TeleporterConfig;

/**
 * @author HorridoJoho
 */
final class YANTeleporterData
{
	protected static final Logger _LOGGER = Logger.getLogger(YANTeleporterData.class.getName());
	private static YANTeleporterData _INSTANCE = null;
	
	public synchronized static void initInstance() throws Exception
	{
		_INSTANCE = new YANTeleporterData();
	}
	
	public synchronized static YANTeleporterData getInstance()
	{
		return _INSTANCE;
	}
	
	protected final TeleporterConfig _config;
	
	private YANTeleporterData() throws Exception
	{
		_config = new TeleporterConfig();
	}
	
	public TeleporterConfig getConfig()
	{
		return _config;
	}
}
