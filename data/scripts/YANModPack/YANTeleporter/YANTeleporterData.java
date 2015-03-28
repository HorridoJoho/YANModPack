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
package YANModPack.YANTeleporter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import YANModPack.src.model.entity.ItemReqDef;
import YANModPack.src.util.XMLUtils;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.items.L2Item;

/**
 * @author HorridoJoho
 */
final class YANTeleporterData
{
	public final class TeleportLocation
	{
		protected final String ident;
		protected final String name;
		protected final int minMembers;
		protected final int maxMemberDistance;
		protected final Location pos;
		protected final Map<String, ItemReqDef> items;
		/** HTMLTemplatePlaceholder */
		protected final HTMLTemplatePlaceholder placeholder;
		
		protected TeleportLocation(String ident, String name, int minMembers, int maxMemberDistance, Location pos, Element elem)
		{
			this.ident = ident;
			this.name = name;
			this.minMembers = minMembers;
			this.maxMemberDistance = maxMemberDistance;
			this.pos = pos;
			this.items = _parseItems(elem);
			
			this.placeholder = new HTMLTemplatePlaceholder("tele_loc", null);
			this.placeholder.addChild("ident", ident).addChild("name", name).addChild("min_members", String.valueOf(minMembers));
			if (!this.items.isEmpty())
			{
				HTMLTemplatePlaceholder itemsPlaceholder = this.placeholder.addChild("items", null).getChild("items");
				for (Entry<String, ItemReqDef> item : this.items.entrySet())
				{
					itemsPlaceholder.addAliasChild(String.valueOf(itemsPlaceholder.getChildsSize()), item.getValue().placeholder);
				}
			}
		}
		
		private Map<String, ItemReqDef> _parseItems(Element elem)
		{
			Map<String, ItemReqDef> items = new HashMap<>();
			Node curNode = elem.getFirstChild();
			while (curNode != null)
			{
				switch (curNode.getNodeType())
				{
					case Node.ELEMENT_NODE:
						Element curElem = (Element) curNode;
						String ident = curElem.getAttribute("ident");
						ItemReqDef req = _itemRequirements.get(ident);
						if (req == null)
						{
							_LOGGER.warning("YANTeleporter - teleport_locations.xml: Item requirement with ident " + ident + " does not exists!");
						}
						else
						{
							items.put(ident, req);
						}
						break;
				}
				
				curNode = curNode.getNextSibling();
			}
			return Collections.unmodifiableMap(items);
		}
	}
	
	public final class TeleportNpc
	{
		public final L2NpcTemplate npc;
		public final Map<String, TeleportLocation> locations;
		/** HTMLTemplatePlaceholder */
		protected final HTMLTemplatePlaceholder placeholder;
		
		protected TeleportNpc(L2NpcTemplate npc, Element elem)
		{
			this.npc = npc;
			this.locations = _parseLocations(elem);
			
			this.placeholder = new HTMLTemplatePlaceholder("teleporter", null);
			this.placeholder.addChild("name", npc.getName());
			if (!this.locations.isEmpty())
			{
				HTMLTemplatePlaceholder locsPlaceholder = this.placeholder.addChild("locs", null).getChild("locs");
				for (Entry<String, TeleportLocation> location : this.locations.entrySet())
				{
					locsPlaceholder.addAliasChild(String.valueOf(locsPlaceholder.getChildsSize()), location.getValue().placeholder);
				}
			}
		}
		
		private Map<String, TeleportLocation> _parseLocations(Element elem)
		{
			Map<String, TeleportLocation> locations = new HashMap<>();
			Node curNode = elem.getFirstChild();
			while (curNode != null)
			{
				switch (curNode.getNodeType())
				{
					case Node.ELEMENT_NODE:
						Element curElem = (Element) curNode;
						String ident = curElem.getAttribute("ident");
						TeleportLocation location = _teleLocs.get(ident);
						if (location == null)
						{
							_LOGGER.warning("YANTeleporter - teleport_npcs.xml: Teleport location with ident " + ident + " does not exists!");
						}
						else
						{
							locations.put(ident, location);
						}
						break;
				}
				
				curNode = curNode.getNextSibling();
			}
			return Collections.unmodifiableMap(locations);
		}
	}
	
	protected static final Logger _LOGGER = Logger.getLogger(YANTeleporterData.class.getName());
	private static YANTeleporterData _INSTANCE = null;
	
	public synchronized static void INIT_INSTANCE() throws Exception
	{
		_INSTANCE = new YANTeleporterData();
	}
	
	public synchronized static YANTeleporterData GET_INSTANCE()
	{
		return _INSTANCE;
	}
	
	protected final Map<String, ItemReqDef> _itemRequirements = new HashMap<>();
	protected final Map<String, TeleportLocation> _teleLocs = new HashMap<>();
	protected final Map<Integer, TeleportNpc> _teleNpcs = new HashMap<>();
	
	private YANTeleporterData() throws Exception
	{
		Path dataPath = Paths.get(Config.DATAPACK_ROOT.getAbsolutePath(), "data", "scripts", YANTeleporter.SCRIPT_SUBFOLDER.toString(), "data");
		Path xsdPath = Paths.get(dataPath.toString(), "xsd");
		
		Element elem = XMLUtils.CreateDocument(dataPath.resolve("item_requirements.xml"), xsdPath.resolve("item_requirements.xsd")).getDocumentElement();
		_parseItemRequirements(elem);
		
		elem = XMLUtils.CreateDocument(dataPath.resolve("teleport_locations.xml"), xsdPath.resolve("teleport_locations.xsd")).getDocumentElement();
		_parseTeleLocs(elem);
		
		elem = XMLUtils.CreateDocument(dataPath.resolve("teleport_npcs.xml"), xsdPath.resolve("teleport_npcs.xsd")).getDocumentElement();
		_parseTeleNpcs(elem);
	}
	
	private void _parseItemRequirements(Element elem)
	{
		Node curNode = elem.getFirstChild();
		while (curNode != null)
		{
			switch (curNode.getNodeType())
			{
				case Node.ELEMENT_NODE:
					Element curElem = (Element) curNode;
					String ident = curElem.getAttribute("id");
					int itemId = Integer.parseInt(curElem.getAttribute("item_id"));
					long itemAmount = Long.parseLong(curElem.getAttribute("item_amount"));
					L2Item item = ItemTable.getInstance().getTemplate(itemId);
					if (item == null)
					{
						_LOGGER.warning("YANTeleporter - item_requirements.xml: Item with id " + itemId + " does not exists!");
					}
					else
					{
						_itemRequirements.put(ident, new ItemReqDef(item.getId(), itemAmount, ident));
					}
					break;
			}
			
			curNode = curNode.getNextSibling();
		}
	}
	
	private void _parseTeleLocs(Element elem)
	{
		Node curNode = elem.getFirstChild();
		while (curNode != null)
		{
			switch (curNode.getNodeType())
			{
				case Node.ELEMENT_NODE:
					Element curElem = (Element) curNode;
					String ident = curElem.getAttribute("ident");
					String name = curElem.getAttribute("name");
					int minMembers = Integer.parseInt(curElem.getAttribute("min_members"));
					int maxMemberDistance = Integer.parseInt(curElem.getAttribute("max_member_distance"));
					int x = Integer.parseInt(curElem.getAttribute("x"));
					int y = Integer.parseInt(curElem.getAttribute("y"));
					int z = Integer.parseInt(curElem.getAttribute("z"));
					int heading = Integer.parseInt(curElem.getAttribute("heading"));
					_teleLocs.put(ident, new TeleportLocation(ident, name, minMembers, maxMemberDistance, new Location(x, y, z, heading), curElem));
					break;
			}
			
			curNode = curNode.getNextSibling();
		}
	}
	
	private void _parseTeleNpcs(Element elem)
	{
		Node curNode = elem.getFirstChild();
		while (curNode != null)
		{
			switch (curNode.getNodeType())
			{
				case Node.ELEMENT_NODE:
					Element curElem = (Element) curNode;
					int id = Integer.parseInt(curElem.getAttribute("id"));
					L2NpcTemplate npc = NpcData.getInstance().getTemplate(id);
					if (npc == null)
					{
						_LOGGER.warning("YANTeleporter - teleport_npcs.xml: Npc with id " + id + " does not exists!");
					}
					else
					{
						_teleNpcs.put(id, new TeleportNpc(npc, curElem));
					}
					break;
			}
			
			curNode = curNode.getNextSibling();
		}
	}
	
	public TeleportLocation getLocation(String ident)
	{
		return _teleLocs.get(ident);
	}
	
	public Map<Integer, TeleportNpc> getTeleportNpcs()
	{
		return _teleNpcs;
	}
	
	public TeleportNpc getTeleportNpc(int npcId)
	{
		return _teleNpcs.get(npcId);
	}
}
