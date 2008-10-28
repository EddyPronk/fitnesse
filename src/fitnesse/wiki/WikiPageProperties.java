// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.components.XmlWriter;
import fitnesse.util.XmlUtil;
import fitnesse.wikitext.Utils;

import org.w3c.dom.*;

import java.io.*;
import java.util.*;

public class WikiPageProperties extends WikiPageProperty implements Serializable
{
	public static final String VIRTUAL_WIKI_ATTRIBUTE = "VirtualWiki";
	private Map symbolicLinks;

	public WikiPageProperties() throws Exception
	{
		symbolicLinks = new HashMap();
	}

	public WikiPageProperties(InputStream inputStream) throws Exception
	{
		this();
		loadFromXmlStream(inputStream);
	}

	public WikiPageProperties(Element rootElement) throws Exception
	{
		this();
		loadFromRootElement(rootElement);
	}

	public WikiPageProperties(WikiPageProperties that) throws Exception
	{
		if(that != null && that.children != null)
			children = new HashMap<String, WikiPageProperty>(that.children);
		symbolicLinks = new HashMap(that.symbolicLinks);
	}

	public void loadFromXmlStream(InputStream inputStream) throws Exception
	{
		Document document = XmlUtil.newDocument(inputStream);
		Element root = document.getDocumentElement();
		loadFromRootElement(root);
	}

	public void loadFromRootElement(Element root) throws Exception
	{
		NodeList nodes = root.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if(node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String key = node.getNodeName();
			LoadElement(this, (Element) node, key);
		}
	}

	private void LoadElement(WikiPageProperty context, Element element, String key)
	{
		WikiPageProperty newProperty = new WikiPageProperty();
		context.set(key, newProperty);

		NodeList nodes = element.getChildNodes();
		if(element.hasAttribute("value"))
			newProperty.setValue(element.getAttribute("value"));
		else if(nodes.getLength() == 1)
			newProperty.setValue(nodes.item(0).getNodeValue());

		for(int i = 0; i < nodes.getLength(); i++)
		{
			Node childNode = nodes.item(i);
			if(childNode instanceof Element)
				LoadElement(newProperty, (Element) childNode, childNode.getNodeName());
		}
	}

	public void save(OutputStream outputStream) throws Exception
	{
		Document document = null;
		XmlWriter writer = null;
		try {
			document = XmlUtil.newDocument();
			document.appendChild(makeRootElement(document));
	
			writer = new XmlWriter(outputStream);
			writer.write(document);
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}

	public Element makeRootElement(Document document)
	{
		Element root = document.createElement("properties");
		List keys = new ArrayList(keySet());
		Collections.sort(keys);

		for(Iterator iterator = keys.iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			WikiPageProperty childProperty = getProperty(key);
			toXml(childProperty, key, document, root);
		}

		return root;
	}

	private void toXml(WikiPageProperty context, String key, Document document, Element parent)
	{
		Element element = document.createElement(key);

		String value = context.getValue();
		if(context.hasChildren())
		{
			if(value != null)
				element.setAttribute("value", value);

			Set childKeys = context.keySet();
			for(Iterator iterator = childKeys.iterator(); iterator.hasNext();)
			{
				String childKey = (String) iterator.next();
				WikiPageProperty child = context.getProperty(childKey);
				if (child == null) {
					System.err.println("Property key \""+childKey+"\" has null value for {"+context+"}");
				} else {
					toXml(child, childKey, document, element);
				}
			}
		}
		else if(value != null)
			element.appendChild(document.createTextNode(Utils.escapeHTML(value)));

		parent.appendChild(element);
	}

	public String toString()
	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString("WikiPageProperties", 0));
		return s.toString();
	}

	public Date getLastModificationTime() throws Exception
	{
		String dateStr = get("LastModified");
		if(dateStr == null)
			return new Date();
		else
			return getTimeFormat().parse(dateStr);
	}

	public void setLastModificationTime(Date date)
	{
		set("LastModified", getTimeFormat().format(date));
	}
}
