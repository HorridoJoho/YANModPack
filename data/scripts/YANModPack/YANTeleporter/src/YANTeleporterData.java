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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import YANModPack.YANTeleporter.src.model.GroupTeleports;
import YANModPack.YANTeleporter.src.model.SoloTeleports;
import YANModPack.YANTeleporter.src.model.TeleporterConfig;
import YANModPack.YANTeleporter.src.model.Teleporters;
import YANModPack.YANTeleporter.src.model.adapter.GroupTeleportRefListToMap;
import YANModPack.YANTeleporter.src.model.adapter.SoloTeleportRefListToMap;
import YANModPack.YANTeleporter.src.model.entity.GroupTeleport;
import YANModPack.YANTeleporter.src.model.entity.SoloTeleport;
import YANModPack.src.model.ItemRequirements;
import YANModPack.src.model.adapter.ItemRequirementRefListToMap;

import com.l2jserver.Config;

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
	protected final ItemRequirements _itemRequirements;
	protected final SoloTeleports _soloTeleports;
	protected final GroupTeleports _groupTeleports;
	protected final Teleporters _teleporters;
	
	private YANTeleporterData() throws Exception
	{
		Path xmlPath = Paths.get(Config.DATAPACK_ROOT.getAbsolutePath(), "data", "scripts", YANTeleporter.SCRIPT_PATH.toString(), "data", "xml");
		
		JAXBContext ctx = JAXBContext.newInstance(TeleporterConfig.class, ItemRequirements.class, SoloTeleports.class, GroupTeleports.class, Teleporters.class);
		Unmarshaller u = ctx.createUnmarshaller();
		// validate against document specified schema
		// u.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI).newSchema());
		
		_config = u.unmarshal(new StreamSource(xmlPath.resolve("yanteleporter.xml").toFile()), TeleporterConfig.class).getValue();
		
		_itemRequirements = u.unmarshal(new StreamSource(xmlPath.resolve("item_requirements.xml").toFile()), ItemRequirements.class).getValue();
		u.setAdapter(ItemRequirementRefListToMap.class, new ItemRequirementRefListToMap(_itemRequirements.items));
		
		_soloTeleports = u.unmarshal(new StreamSource(xmlPath.resolve("solo_teleports.xml").toFile()), SoloTeleports.class).getValue();
		u.setAdapter(SoloTeleportRefListToMap.class, new SoloTeleportRefListToMap(_soloTeleports.teleports));
		
		_groupTeleports = u.unmarshal(new StreamSource(xmlPath.resolve("group_teleports.xml").toFile()), GroupTeleports.class).getValue();
		u.setAdapter(GroupTeleportRefListToMap.class, new GroupTeleportRefListToMap(_groupTeleports.teleports));
		
		_teleporters = u.unmarshal(new StreamSource(xmlPath.resolve("teleporters.xml").toFile()), Teleporters.class).getValue();
	}
	
	public TeleporterConfig getConfig()
	{
		return _config;
	}
	
	public SoloTeleport getSoloTeleport(String ident)
	{
		return _soloTeleports.teleports.get(ident);
	}
	
	public GroupTeleport getGroupTeleport(String ident)
	{
		return _groupTeleports.teleports.get(ident);
	}
	
	public Teleporters getTeleporters()
	{
		return _teleporters;
	}
}
