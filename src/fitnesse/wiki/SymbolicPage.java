// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class SymbolicPage extends BaseWikiPage
{
	public static final String PROPERTY_NAME = "SymbolicLinks";

	private WikiPage realPage;

	public SymbolicPage(String name, WikiPage realPage, WikiPage parent)
	{
		super(name, parent);
		this.realPage = realPage;
	}

	public WikiPage getRealPage()
	{
		return realPage;
	}

	public WikiPage addChildPage(String name) throws Exception
	{
		return realPage.addChildPage(name);
	}

	public boolean hasChildPage(String name) throws Exception
	{
		return realPage.hasChildPage(name);
	}

	protected WikiPage getNormalChildPage(String name) throws Exception
	{
		WikiPage childPage = realPage.getChildPage(name);
		if (childPage != null && !(childPage instanceof SymbolicPage))
			childPage = new SymbolicPage(name, childPage, this);
		return childPage;
	}

	@Override
	protected WikiPage createInternalSymbolicPage(String linkPath, String linkName) throws Exception
	{
		WikiPagePath path = PathParser.parse(linkPath);
		WikiPage start = (path.isRelativePath())? getRealPage().getParent() : getRealPage();
		WikiPage page = getPageCrawler().getPage(start, path);
		if(page != null)
			page = new SymbolicPage(linkName, page, this);
		return page;
	}

	public void removeChildPage(String name) throws Exception
	{
		realPage.removeChildPage(name);
	}

	public List getNormalChildren() throws Exception
	{
		List children = realPage.getChildren();
		List symChildren = new LinkedList();
		//...Intentionally exclude symbolic links on symbolic pages
		//   to prevent infinite cyclic symbolic references.
		//TODO: -AcD- we need a better cyclic infinite recursion algorithm here.
		for(Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			WikiPage child = (WikiPage) iterator.next();
			symChildren.add(new SymbolicPage(child.getName(), child, this));
		}
		return symChildren;
	}

	public PageData getData() throws Exception
	{
		PageData data = realPage.getData();
		data.setWikiPage(this);
		return data;
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		PageData data = realPage.getDataVersion(versionName);
		data.setWikiPage(this);
		return data;
	}

	public VersionInfo commit(PageData data) throws Exception
	{
		return realPage.commit(data);
	}

	//TODO Delete these method alone with ProxyPage when the time is right.
	public boolean hasExtension(String extensionName)
	{
		return realPage.hasExtension(extensionName);
	}

	public Extension getExtension(String extensionName)
	{
		return realPage.getExtension(extensionName);
	}
}
