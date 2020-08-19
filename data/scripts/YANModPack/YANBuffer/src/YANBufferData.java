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

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import YANModPack.YANBuffer.src.model.BufferConfig;
import YANModPack.YANBuffer.src.model.entity.BuffSkill;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;

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
	
	private final BufferConfig _config;
	protected final ConcurrentHashMap<Integer, Map<Integer, UniqueBufflist>> _uniqueBufflists = new ConcurrentHashMap<>();
	
	private YANBufferData() throws Exception
	{
		// TODO: Load data from json via gson library
		_config = new BufferConfig();
		
		_loadUniqueBufflists();
	}
	
	private void _loadUniqueBufflists() throws Exception
	{
		try (Connection con = ConnectionFactory.getInstance().getConnection();)
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
								BuffSkill buff = _config.getGlobal().getBuff(buffIdent);
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
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
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
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
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
	
	public boolean addToUniqueBufflist(int playerObjectId, String ulistName, BuffSkill buff)
	{
		UniqueBufflist ulist = _getPlayersUList(playerObjectId, ulistName);
		// prevent duplicate entry with ulist.contains(buff)
		if ((ulist == null) || ulist.contains(buff) || ((buff.getType() == BuffType.BUFF) && (ulist.numBuffs >= _config.getGlobal().getUniqueMaxBuffs())) || ((buff.getType() == BuffType.SONG_DANCE) && (ulist.numSongsDances >= _config.getGlobal().getUniqueMaxSongsDances())))
		{
			return false;
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("INSERT INTO yanb_ulist_buffs VALUES(?,?)");)
		{
			stmt.setInt(1, ulist.ulistId);
			stmt.setString(2, buff.getId());
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
	
	public void removeFromUniqueBufflist(int playerObjectId, String ulistName, BuffSkill buff)
	{
		UniqueBufflist ulist = _getPlayersUList(playerObjectId, ulistName);
		if ((ulist == null) || !ulist.contains(buff))
		{
			return;
		}
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement("DELETE FROM yanb_ulist_buffs WHERE ulist_id=? AND ulist_buff_ident=?");)
		{
			stmt.setInt(1, ulist.ulistId);
			stmt.setString(2, buff.getId());
			stmt.executeUpdate();
			ulist.remove(buff);
		}
		catch (SQLException sqle)
		{
			_LOGGER.log(Level.WARNING, "Failed to remove buff from unique bufflist!", sqle);
		}
	}
	
	public BufferConfig getConfig()
	{
		return _config;
	}
	
	public boolean canHaveMoreBufflists(L2PcInstance player)
	{
		return _getPlayersULists(player.getObjectId()).size() < _config.getGlobal().getMaxUniqueLists();
	}
	
	public boolean hasUniqueBufflist(int playerObjectId, String ulistName)
	{
		return _getPlayersUList(playerObjectId, ulistName) != null;
	}
	
	public List<BuffSkill> getUniqueBufflist(int playerObjectId, String ulistName)
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
