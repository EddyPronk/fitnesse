// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.components.PageReferencer;
import fitnesse.wiki.*;

import java.util.*;
import java.util.regex.*;

public class IncludeWidget extends ParentWidget implements PageReferencer
{
	public static final String REGEXP = "^!include(?: +-setup| +-teardown| +-seamless| +-c)? " + WikiWordWidget.REGEXP + LineBreakWidget.REGEXP + "?";
	static final Pattern pattern = Pattern.compile("^!include *(-setup|-teardown|-seamless|-c)? (.*)");

	public static final String COLLAPSE_SETUP = "COLLAPSE_SETUP";
	public static final String COLLAPSE_TEARDOWN = "COLLAPSE_TEARDOWN";

	protected String pageName;
	protected WikiPage includingPage;
   protected WikiPage includedPage; //[acd] !include: Retain from getIncludedPageContent
	protected WikiPage parentPage;

	private static Map optionPrefixMap = buildOptionPrefixMap();
	private static Map optionCssMap = buildOptionsCssMap();

	public IncludeWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher matcher = pattern.matcher(text);
		if(matcher.find())
		{
			pageName = getPageName(matcher);
			includingPage = parent.getWikiPage();
			parentPage = includingPage.getParent();
			buildWidget(getOption(matcher));
		}
	}

	protected String getIncludedPageContent() throws Exception
	{
		PageCrawler crawler = parentPage.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		WikiPagePath pagePath = PathParser.parse(pageName);
      includedPage = crawler.getSiblingPage(includingPage, pagePath); //[acd] !include: Retain this.
      
		if(includedPage != null)
		{
         includedPage.setParentForVariables(this.getWikiPage().getParentForVariables());
			return includedPage.getData().getContent();
		}
		else if(includingPage instanceof ProxyPage)
		{
			ProxyPage proxy = (ProxyPage) includingPage;
			String host = proxy.getHost();
			int port = proxy.getHostPort();
			try
			{
				ProxyPage remoteIncludedPage = new ProxyPage("RemoteIncludedPage", null, host, port, pagePath);
				return remoteIncludedPage.getData().getContent();
			}
			catch(Exception e)
			{
				return "!meta '''Remote page " + host + ":" + port + "/" + pageName + " does not exist.'''";
			}
		}
		else
		{
			return "!meta '''Page include failed because the page " + pageName + " does not exist.'''";
		}
	}

	protected WikiPage getIncludedPage() throws Exception
	{
		PageCrawler crawler = parentPage.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		return crawler.getPage(parentPage, PathParser.parse(pageName));
	}

	protected WikiPage getParentPage() throws Exception
	{
		return parent.getWikiPage().getParent();
	}

	private String getOption(Matcher match)
	{
		return match.group(1);
	}

	private String getPageName(Matcher match)
	{
		return match.group(2);
	}

	//TODO MDM I know this is bad...  But it seems better then creating two new widgets.
	private void buildWidget(String option) throws Exception
	{
		String widgetText = processLiterals(getIncludedPageContent());

      //[acd] !include: Create imposter root with alias = this if included page found.
      ParentWidget incRoot = (includedPage == null)? this : new WidgetRoot(includedPage, this);

		if("-seamless".equals(option) || getRoot().isGatheringInfo())
		{  //[acd] !include: Use the imposter if found.
         incRoot.addChildWidgets(widgetText + "\n");
		}
		else
		{  //[acd] !include: Use new constructor with dual scope.  
         new CollapsableWidget(incRoot, this, getPrefix(option) + pageName, widgetText, getCssClass(option), isCollapsed(option));
		}
	}

	private String getCssClass(String option)
	{
		return (String) optionCssMap.get(option);
	}

	private String getPrefix(String option)
	{
		return (String) optionPrefixMap.get(option);
	}

	private boolean isCollapsed(String option)
		throws Exception
	{
		if(isSetup(option) && isSetupCollapsed())
			return true;
		else if(isTeardown(option) && isTeardownCollapsed())
			return true;
		else if("-c".equals(option))
			return true;
		return false;
	}

	private static Map buildOptionsCssMap()
	{
		Map optionCssMap = new HashMap();
		optionCssMap.put("-setup", "setup");
		optionCssMap.put("-teardown", "teardown");
		optionCssMap.put("-c", "included");
		optionCssMap.put(null, "included");
		return optionCssMap;
	}

	private static Map buildOptionPrefixMap()
	{
		Map optionPrefixMap = new HashMap();
		optionPrefixMap.put("-setup", "Set Up: ");
		optionPrefixMap.put("-teardown", "Tear Down: ");
		optionPrefixMap.put("-c", "Included page: ");
		optionPrefixMap.put(null, "Included page: ");
		return optionPrefixMap;
	}

	private boolean isTeardownCollapsed()
		throws Exception
	{
		return "true".equals(parent.getVariable(COLLAPSE_TEARDOWN));
	}

	private boolean isTeardown(String option)
	{
		return "-teardown".equals(option);
	}

	private boolean isSetupCollapsed()
		throws Exception
	{
		return "true".equals(parent.getVariable(COLLAPSE_SETUP));
	}

	private boolean isSetup(String option)
	{
		return "-setup".equals(option);
	}

	public String render() throws Exception
	{
		return childHtml();
	}

	public WikiPage getReferencedPage() throws Exception
	{
		return getParentPage().getPageCrawler().getPage(getParentPage(), PathParser.parse(pageName));
	}

}
