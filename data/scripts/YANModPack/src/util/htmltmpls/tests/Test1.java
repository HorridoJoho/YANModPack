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
package YANModPack.src.util.htmltmpls.tests;

import java.util.HashMap;

import YANModPack.src.util.htmltmpls.HTMLTemplateParser;
import YANModPack.src.util.htmltmpls.HTMLTemplatePlaceholder;
import YANModPack.src.util.htmltmpls.funcs.ForeachFunc;

/**
 * @author: HorridoJoho
 */
public final class Test1
{
	public static void main(String[] args)
	{
		HashMap<String, HTMLTemplatePlaceholder> placeholders = new HashMap<>();
		
		HTMLTemplatePlaceholder playerPlaceholder = new HTMLTemplatePlaceholder("player", null);
		playerPlaceholder.addChild("name", "FBIagent").addChild("level", "57").addChild("items", null);
		playerPlaceholder.getChild("items").addChild("0", "Adena").addChild("1", "Ancient Adena");
		placeholders.put(playerPlaceholder.getName(), playerPlaceholder);
		
		StringBuilder builder = new StringBuilder("<html><body>\n" + "Name: %player.name%<br1>\n" + "Level: %player.level%<br1>\n" + "Items:<br1>\n" + "[FOREACH(item IN player.items DO\n" + " %item%<br1>\n" + ")ENDEACH]\n" + "</body></html>");
		HTMLTemplateParser.fromStringBuilder(builder, null, placeholders, ForeachFunc.INSTANCE);
		System.out.println(builder);
	}
}
