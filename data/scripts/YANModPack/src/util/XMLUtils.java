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
package YANModPack.src.util;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author HorridoJoho
 */
public class XMLUtils
{
	public static final String YAN_MOD_PACK_ATTACHMENT_PROPERTY = "YAN_MOD_PACK_ATTACHMENT_PROPERTY";
	
	public static Document CreateDocument(Path xml, Path xsd) throws SAXException, IOException, ParserConfigurationException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
		if (xsd != null)
		{
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", xsd.toString());
		}
		return factory.newDocumentBuilder().parse(xml.toString());
	}
}
