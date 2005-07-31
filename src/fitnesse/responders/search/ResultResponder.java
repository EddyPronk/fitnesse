// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.search;

import fitnesse.html.*;
import fitnesse.components.SearchObserver;
import fitnesse.wiki.*;
import fitnesse.responders.*;
import fitnesse.authentication.*;

public abstract class ResultResponder extends ChunkingResponder implements SearchObserver, SecureResponder
{
	private int hits = 0;

	protected PageCrawler getPageCrawler()
	{
		return root.getPageCrawler();
	}

	protected void doSending() throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		String renderedPath = getRenderedPath();
		page.title.use(getTitle() + ": " + renderedPath);
		page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(renderedPath, getTitle()));
		page.main.use(HtmlPage.BreakPoint);
		page.divide();

		response.add(page.preDivision);
		response.add(buildClientSideSortScriptTag().html());
		response.add(buildFeedbackDiv().html());
		response.add(getTableOpen());
		response.add(buildHeaderRow().html());
		response.add(getTbodyOpen());
		startSearching();
		response.add(getTbodyClose());
		response.add(getTableClose());
		response.add(buildTableSorterScript().html());
		response.add(buildFeedbackModificationScript().html());
		response.add(page.postDivision);
		response.closeAll();
	}

	private String getTbodyClose()
	{
		return "</tbody>";
	}

	private String getTbodyOpen()
	{
		return "<tbody>";
	}

	private HtmlTag buildClientSideSortScriptTag()
	{
		HtmlTag tag = new HtmlTag("script");
		tag.addAttribute("src", "/files/javascript/clientSideSort.js");
		tag.add(" ");
		return tag;
	}

	private String getTableClose()
	{
		return ChunkedResultsListingUtil.getTableCloseHtml();
	}

	private String getTableOpen()
	{
		return ChunkedResultsListingUtil.getTableOpenHtml("searchResultsTable");
	}

	private HtmlTag buildFeedbackModificationScript() throws Exception
	{
		HtmlTag script = new HtmlTag("script");
		script.addAttribute("language", "javascript");
		script.add("document.getElementById(\"feedback\").innerHTML = '" + getPageFooterInfo(hits) + "'");
		return script;
	}

	private HtmlTag buildTableSorterScript() throws Exception
	{
		HtmlTag script = new HtmlTag("script");
		script.addAttribute("language", "javascript");
		script.add("tableSorter = new TableSorter('searchResultsTable', new DateParser(" + getDateFormatJavascriptRegex() + ",8,2,3,4,5,6));");
		return script;
	}

	public static String getDateFormatJavascriptRegex()
	{
		return "/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/";
	}

	private HtmlTag buildHeaderRow()
	{
		HtmlTag thead = new HtmlTag("thead");
		HtmlTag headerRow = new HtmlTag("tr");
		headerRow.add(buildPageColumnHeader());
		headerRow.add(buildLastModifiedColumnHeader());
		thead.add(headerRow);
		return thead;
	}

	private HtmlTag buildLastModifiedColumnHeader()
	{
		HtmlTag lastModifiedColumnHeader = new HtmlTag("td", buildSortLink("LastModified", "1, 'date'"));
		lastModifiedColumnHeader.addAttribute("class", "resultsHeader");
		return lastModifiedColumnHeader;
	}

	private HtmlTag buildPageColumnHeader()
	{
		HtmlTag pageColumnHeader = new HtmlTag("td", buildSortLink("Page", "0"));
		pageColumnHeader.addAttribute("class", "resultsHeader");
		return pageColumnHeader;
	}

	private HtmlTag buildSortLink(String text, String args)
	{
		HtmlTag link = new HtmlTag("a");
		link.addAttribute("href", "javascript:void(tableSorter.sort(" + args + "));");
		link.add(text);
		return link;
	}

	private HtmlTag buildFeedbackDiv()
	{
		HtmlTag feedback = new HtmlTag("div", "Searching...");
		feedback.addAttribute("id", "feedback");
		return feedback;
	}

	public void hit(WikiPage page) throws Exception
	{
		hits++;
		String fullPathName = PathParser.render(getPageCrawler().getFullPath(page));

		HtmlTag row = new HtmlTag("tr");
		row.addAttribute("class", "resultsRow" + getRow());

		HtmlTag link = new HtmlTag("a", fullPathName) ;
		link.addAttribute("href", fullPathName);

		row.add(new HtmlTag("td", link));
		row.add(new HtmlTag("td", "" + page.getData().getLastModificationTime()));
		response.add(row.html());
	}

	private int nextRow = 0;

	private int getRow()
	{
		return (nextRow++ % 2) + 1;
	}

	protected abstract String getTitle() throws Exception;

	protected abstract String getPageFooterInfo(int hits) throws Exception;

	protected abstract void startSearching() throws Exception;

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}
}

