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
package YANModPack.YANBuffer.src;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;

import YANModPack.YANBuffer.src.model.BuffCategories;
import YANModPack.YANBuffer.src.model.BuffSkills;
import YANModPack.YANBuffer.src.model.Buffers;
import YANModPack.YANBuffer.src.model.adapter.reference.BuffCategoryRefListMapAdapter;
import YANModPack.YANBuffer.src.model.adapter.reference.BuffSkillRefListMapAdapter;
import YANModPack.YANBuffer.src.model.entity.BuffCategoryDef;
import YANModPack.YANBuffer.src.model.entity.BuffSkillDef;
import YANModPack.YANBuffer.src.model.entity.NpcBuffer;
import YANModPack.src.model.ItemReqDefs;
import YANModPack.src.model.adapter.ItemReqRefListToMapAdapter;
import YANModPack.src.model.entity.ItemReqDef;
import YANModPack.src.util.XMLUtils;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

import com.l2jserver.Config;
import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 */
public final class YANBufferData
{
	public static enum BuffType
	{
		BUFF,
		SONG_DANCE
	}
	
	public static enum HtmlType
	{
		NPC,
		COMMUNITY
	}
	
	protected static final Logger _LOGGER = Logger.getLogger(YANBufferData.class.getName());
	private static YANBufferData _INSTANCE = null;
	
	public static void main(String[] args) throws Exception
	{
		initInstance();
	}
	
	public synchronized static void initInstance() throws Exception
	{
		_INSTANCE = new YANBufferData();
	}
	
	public synchronized static YANBufferData getInstance()
	{
		return _INSTANCE;
	}
	
	private final long _healCooldown;
	protected final int _maxUniqueLists;
	protected final int _uniqueMaxBuffs;
	protected final int _uniqueMaxSongsDances;
	protected final boolean _debug;
	protected final ItemReqDefs _itemRequirements;
	protected final BuffSkills _buffs;
	protected final BuffCategories _buffCats;
	private final Buffers _buffers;
	protected final ConcurrentHashMap<Integer, Map<Integer, UniqueBufflist>> _uniqueBufflists = new ConcurrentHashMap<>();
	
	private YANBufferData() throws Exception
	{
		Path xmlPath = Paths.get(Config.DATAPACK_ROOT.getAbsolutePath(), "data", "scripts", YANBuffer.SCRIPT_SUBFOLDER.toString(), "data", "xml");
		Path xsdPath = Paths.get(xmlPath.toString(), "xsd");
		
		Element elem = XMLUtils.CreateDocument(xmlPath.resolve("yanbuffer.xml"), xsdPath.resolve("yanbuffer.xsd")).getDocumentElement();
		_healCooldown = Integer.parseInt(elem.getAttribute("heal_cooldown")) * 1000;
		_maxUniqueLists = Integer.parseInt(elem.getAttribute("max_unique_lists"));
		_uniqueMaxBuffs = Integer.parseInt(elem.getAttribute("unique_max_buffs"));
		_uniqueMaxSongsDances = Integer.parseInt(elem.getAttribute("unique_max_songs_dances"));
		_debug = Boolean.parseBoolean(elem.getAttribute("debug"));
		
		JAXBContext ctx = JAXBContext.newInstance(ItemReqDefs.class, BuffSkills.class, BuffCategories.class, Buffers.class);
		Unmarshaller u = ctx.createUnmarshaller();
		// validate against document specified schema
		// u.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI).newSchema());
		
		_itemRequirements = u.unmarshal(new StreamSource(xmlPath.resolve("item_requirements.xml").toFile()), ItemReqDefs.class).getValue();
		u.setAdapter(ItemReqRefListToMapAdapter.class, new ItemReqRefListToMapAdapter(_itemRequirements.items));
		
		_buffs = u.unmarshal(new StreamSource(xmlPath.resolve("buffs.xml").toFile()), BuffSkills.class).getValue();
		u.setAdapter(BuffSkillRefListMapAdapter.class, new BuffSkillRefListMapAdapter(_buffs.buffs));
		
		_buffCats = u.unmarshal(new StreamSource(xmlPath.resolve("buff_categories.xml").toFile()), BuffCategories.class).getValue();
		u.setAdapter(BuffCategoryRefListMapAdapter.class, new BuffCategoryRefListMapAdapter(_buffCats.cats));
		
		_buffers = u.unmarshal(new StreamSource(xmlPath.resolve("buffers.xml").toFile()), Buffers.class).getValue();
		
		// This is a workaround because we can't pass user supplied objects to
		// the JAXB unmarhsaller. That means we don't have access to the data
		// fields for max unique lists and max buffs on unique lists. Since the
		// beginning this properties are required to be in the buffers HTML
		// placeholder. To not break HTML compatibility we now do it here even
		// if this options are globally set and not per buffer.
		_buffers.voicedBuffer.placeholder.addChild("max_unique_lists", String.valueOf(_maxUniqueLists)).addChild("unique_max_buffs", String.valueOf(_uniqueMaxBuffs)).addChild("unique_max_songs_dances", String.valueOf(_uniqueMaxSongsDances));
		for (Map.Entry<Integer, NpcBuffer> e : _buffers.npcBuffers.entrySet())
		{
			e.getValue().placeholder.addChild("max_unique_lists", String.valueOf(_maxUniqueLists)).addChild("unique_max_buffs", String.valueOf(_uniqueMaxBuffs)).addChild("unique_max_songs_dances", String.valueOf(_uniqueMaxSongsDances));
		}
		
		_loadUniqueBufflists();
	}
	
	private void _loadUniqueBufflists() throws Exception
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
		{
			try (Statement stmt = con.createStatement();
				ResultSet rset = stmt.executeQuery("SELECT ulist_id,ulist_char_id,ulist_name FROM yanb_ulists ORDER BY ulist_char_id ASC");)
			{
				while (rset.next())
				{
					int charId = rset.getInt("ulist_char_id");
					int ulistId = rset.getInt("ulist_id");
					String ulistName = rset.getString("ulist_name");
					
					Map<Integer, UniqueBufflist> ulists = _getPlayersULists(charId);
					ulists.put(ulistId, new UniqueBufflist(ulistId, ulistName));
				}
			}
			
			for (Entry<Integer, Map<Integer, UniqueBufflist>> ulists : _uniqueBufflists.entrySet())
			{
				for (Entry<Integer, UniqueBufflist> ulist : ulists.getValue().entrySet())
				{
					try (PreparedStatement stmt = con.prepareStatement("SELECT ulist_buff_ident FROM yanb_ulist_buffs WHERE ulist_id=?");)
					{
						stmt.setInt(1, ulist.getKey());
						try (ResultSet rs = stmt.executeQuery();)
						{
							while (rs.next())
							{
								String buffIdent = rs.getString("ulist_buff_ident");
								BuffSkillDef buff = getBuff(buffIdent);
								if (buff == null)
								{
									_LOGGER.warning("YANBuffer - Data: Buff with ident does not exists!");
								}
								else
								{
									ulist.getValue().add(buff);
								}
							}
						}
					}
				}
			}
		}
		catch (SQLException sqle)
		{
			throw new SQLException(sqle);
		}
	}
	
	private Map<Integer, UniqueBufflist> _getPlayersULists(int playerObjectId)
	{
		Map<Integer, UniqueBufflist> ulists = _uniqueBufflists.get(playerObjectId);
		if (ulists == null)
		{
			ulists = new LinkedHashMap<>();
			_uniqueBufflists.put(playerObjectId, ulists);
		}
		
		return ulists;
	}
	
	private UniqueBufflist _getPlayersUList(int playerObjectId, String ulistName)
	{
		Map<Integer, UniqueBufflist> ulists = _getPlayersULists(playerObjectId);
		for (Entry<Integer, UniqueBufflist> entry : ulists.entrySet())
		{
			if (entry.getValue().ulistName.equals(ulistName))
			{
				return entry.getValue();
			}
		}
		return null;
	}
	
	public boolean createUniqueBufflist(int playerObjectId, String ulistName)
	{
		// prevent duplicate entry
		if (_getPlayersUList(playerObjectId, ulistName) != null)
		{
			return false;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("INSERT INTO yanb_ulists (ulist_char_id,ulist_name) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);)
		{
			stmt.setInt(1, playerObjectId);
			stmt.setString(2, ulistName);
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			rs.next();
			int newId = rs.getInt(1);
			_getPlayersULists(playerObjectId).put(newId, new UniqueBufflist(newId, ulistName));
			return true;
		}
		catch (SQLException sqle)
		{
			_LOGGER.log(Level.WARNING, "Failed to insert unique bufflist!", sqle);
			return false;
		}
	}
	
	public void deleteUniqueBufflist(int playerObjectId, String ulistName)
	{
		UniqueBufflist ulist = _getPlayersUList(playerObjectId, ulistName);
		if (ulist == null)
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM yanb_ulists WHERE ulist_char_id=? AND ulist_id=?");)
		{
			stmt.setInt(1, playerObjectId);
			stmt.setInt(2, ulist.ulistId);
			stmt.executeUpdate();
			_getPlayersULists(playerObjectId).remove(ulist.ulistId);
		}
		catch (SQLException sqle)
		{
			_LOGGER.log(Level.WARNING, "Failed to delete unique bufflist!", sqle);
		}
	}
	
	public boolean addToUniqueBufflist(int playerObjectId, String ulistName, BuffSkillDef buff)
	{
		UniqueBufflist ulist = _getPlayersUList(playerObjectId, ulistName);
		// prevent duplicate entry with ulist.contains(buff)
		if ((ulist == null) || ulist.contains(buff) || ((buff.type == BuffType.BUFF) && (ulist.numBuffs >= _uniqueMaxBuffs)) || ((buff.type == BuffType.SONG_DANCE) && (ulist.numSongsDances >= _uniqueMaxSongsDances)))
		{
			return false;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("INSERT INTO yanb_ulist_buffs VALUES(?,?)");)
		{
			stmt.setInt(1, ulist.ulistId);
			stmt.setString(2, buff.id);
			stmt.executeUpdate();
			ulist.add(buff);
		}
		catch (SQLException sqle)
		{
			_LOGGER.log(Level.WARNING, "Failed to insert buff into unique bufflist!", sqle);
			return false;
		}
		
		return true;
	}
	
	public void removeFromUniqueBufflist(int playerObjectId, String ulistName, BuffSkillDef buff)
	{
		UniqueBufflist ulist = _getPlayersUList(playerObjectId, ulistName);
		if ((ulist == null) || !ulist.contains(buff))
		{
			return;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM yanb_ulist_buffs WHERE ulist_id=? AND ulist_buff_ident=?");)
		{
			stmt.setInt(1, ulist.ulistId);
			stmt.setString(2, buff.id);
			stmt.executeUpdate();
			ulist.remove(buff);
		}
		catch (SQLException sqle)
		{
			_LOGGER.log(Level.WARNING, "Failed to remove buff from unique bufflist!", sqle);
		}
	}
	
	public long getHealCooldown()
	{
		return _healCooldown;
	}
	
	public boolean enabledDebugging()
	{
		return _debug;
	}
	
	public ItemReqDef getItemRequirement(String ident)
	{
		return _itemRequirements.items.get(ident);
	}
	
	public BuffSkillDef getBuff(String buffIdent)
	{
		return _buffs.buffs.get(buffIdent);
	}
	
	public BuffCategoryDef getBuffCat(String buffCatIdent)
	{
		return _buffCats.cats.get(buffCatIdent);
	}
	
	public Buffers getBuffers()
	{
		return _buffers;
	}
	
	public boolean canHaveMoreBufflists(L2PcInstance player)
	{
		return _getPlayersULists(player.getObjectId()).size() < _maxUniqueLists;
	}
	
	public boolean hasUniqueBufflist(int playerObjectId, String ulistName)
	{
		return _getPlayersUList(playerObjectId, ulistName) != null;
	}
	
	public List<BuffSkillDef> getUniqueBufflist(int playerObjectId, String ulistName)
	{
		UniqueBufflist ulist = _getPlayersUList(playerObjectId, ulistName);
		if (ulist == null)
		{
			return null;
		}
		return Collections.unmodifiableList(ulist);
	}
	
	public HTMLTemplatePlaceholder getPlayersUListPlaceholder(int playerObjectId, String ulistName)
	{
		UniqueBufflist ulist = _getPlayersUList(playerObjectId, ulistName);
		if (ulist == null)
		{
			return null;
		}
		return ulist.placeholder;
	}
	
	public HTMLTemplatePlaceholder getPlayersUListsPlaceholder(int playerObjectId)
	{
		Map<Integer, UniqueBufflist> ulists = _getPlayersULists(playerObjectId);
		if (ulists.isEmpty())
		{
			return null;
		}
		
		HTMLTemplatePlaceholder placeholder = new HTMLTemplatePlaceholder("uniques", null);
		for (Entry<Integer, UniqueBufflist> entry : ulists.entrySet())
		{
			placeholder.addAliasChild(String.valueOf(placeholder.getChildsSize()), entry.getValue().placeholder);
		}
		return placeholder;
	}
}
