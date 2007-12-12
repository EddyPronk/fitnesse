// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.responders.*;
import fitnesse.wiki.*;
import fitnesse.wikitext.Utils;

import java.util.*;

public class PropertiesResponder implements SecureResponder
{
	private WikiPage page;
	public PageData pageData;
	private String resource;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		resource = request.getResource();
		WikiPagePath path = PathParser.parse(resource);
		PageCrawler crawler = context.root.getPageCrawler();
		if(!crawler.pageExists(context.root, path))
		{
			crawler.setDeadEndStrategy(new MockingPageCrawler());
			page = crawler.getPage(context.root, path);
		}
		else
			page = crawler.getPage(context.root, path);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);

		pageData = page.getData();
		String html = makeHtml(context);

		response.setContent(html);
		response.setMaxAge(0);

		return response;
	}

	private String makeHtml(FitNesseContext context) throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		page.title.use("Properties: " + resource);
		page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Page Properties"));
		page.main.use(makeLastModifiedTag());
		page.main.add(makeFormSections());

		return page.html();
	}

	private HtmlTag makeAttributeCheckbox(String attribute, PageData pageData) throws Exception
	{
		HtmlTag checkbox = makeCheckbox(attribute);
		if(pageData.hasAttribute(attribute))
			checkbox.addAttribute("checked", "true");
		return checkbox;
	}

	private HtmlTag makeCheckbox(String attribute)
	{
		HtmlTag checkbox = HtmlUtil.makeInputTag("checkbox", attribute);
		checkbox.tail = " - " + attribute;
		return checkbox;
	}

	private HtmlTag makeLastModifiedTag() throws Exception
	{
		HtmlTag tag = HtmlUtil.makeDivTag("right");
		String username = pageData.getAttribute(WikiPage.LAST_MODIFYING_USER);
		if(username == null || "".equals(username))
			tag.use("Last modified anonymously");
		else
			tag.use("Last modified by " + username);
		return tag;
	}

	private HtmlTag makeFormSections() throws Exception
	{
		TagGroup html = new TagGroup();
		html.add(makePropertiesForm());

		WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
		if(importProperty != null)
			html.add(makeImportUpdateForm(importProperty));
		else
			html.add(makeImportForm());

		html.add(makeSymbolicLinkSection());

		return html;
	}

	private HtmlTag makePropertiesForm() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("post", resource);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveProperties"));

		HtmlTag trisection = new HtmlTag("div");
		trisection.addAttribute("style", "width:100%");
		trisection.add(makeTestActionCheckboxesHtml(pageData));
		trisection.add(makeNavigationCheckboxesHtml(pageData));
		trisection.add(makeSecurityCheckboxesHtml(pageData));
		trisection.add(makeVirtualWikiHtml());
		trisection.add(makeSuitesHtml(pageData));
		trisection.add(makeHelpTextHtml(pageData));
		form.add(trisection);

		HtmlTag buttonSection = new HtmlTag("div");
		buttonSection.add(HtmlUtil.BR);
		HtmlTag saveButton = HtmlUtil.makeInputTag("submit", "Save", "Save Properties");
		saveButton.addAttribute("accesskey", "s");
		buttonSection.add(saveButton);
		form.add(buttonSection);
		return form;
	}

	private HtmlTag makeVirtualWikiHtml() throws Exception
	{
		HtmlTag virtualWiki = new HtmlTag("div");
		virtualWiki.addAttribute("style", "float: left;");
		virtualWiki.add("VirtualWiki URL: ");
		HtmlTag deprecated = new HtmlTag("span", "(DEPRECATED)");
		deprecated.addAttribute("style", "color: #FF0000;");
		virtualWiki.add(deprecated);
		virtualWiki.add(HtmlUtil.BR);
		HtmlTag vwInput = HtmlUtil.makeInputTag("text", "VirtualWiki", getVirtualWikiValue(pageData));
		vwInput.addAttribute("size", "40");
		virtualWiki.add(vwInput);
		virtualWiki.add(HtmlUtil.NBSP);
		virtualWiki.add(HtmlUtil.NBSP);
		return virtualWiki;
	}

	private HtmlTag makeImportForm()
	{
		HtmlTag form = HtmlUtil.makeFormTag("post", resource + "#end");
		form.add(HtmlUtil.HR);
		form.add("Wiki Import.  Supply the URL for the wiki you'd like to import.");
		form.add(HtmlUtil.BR);
		form.add("Remote Wiki URL:");
		HtmlTag remoteUrlField = HtmlUtil.makeInputTag("text", "remoteUrl");
		remoteUrlField.addAttribute("size", "70");
		form.add(remoteUrlField);
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("checkbox", "autoUpdate", "0"));
		form.add("- Automatically update imported content when executing tests");
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
		form.add(HtmlUtil.makeInputTag("submit", "save", "Import"));
		return form;
	}

	private HtmlTag makeImportUpdateForm(WikiImportProperty importProps) throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("post", resource + "#end");

		form.add(HtmlUtil.HR);
		form.add(new HtmlTag("strong", "Wiki Import Update"));
		form.add(HtmlUtil.BR);
		String buttonMessage = "";
		form.add(HtmlUtil.makeLink(page.getName(), page.getName()));
		if(importProps.isRoot())
		{
			form.add(" imports its subpages from ");
			buttonMessage = "Update Subpages";
		}
		else
		{
			form.add(" imports its content and subpages from ");
			buttonMessage = "Update Content and Subpages";
		}
		form.add(HtmlUtil.makeLink(importProps.getSourceUrl(), importProps.getSourceUrl()));
		form.add(".");
		form.add(HtmlUtil.BR);
		HtmlTag autoUpdateCheckBox = HtmlUtil.makeInputTag("checkbox", "autoUpdate");
		if(importProps.isAutoUpdate())
			autoUpdateCheckBox.addAttribute("checked", "true");
		form.add(autoUpdateCheckBox);

		form.add("- Automatically update imported content when executing tests");
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
		form.add(HtmlUtil.makeInputTag("submit", "save", buttonMessage));

		return form;
	}

	private HtmlTag makeSymbolicLinkSection() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("get", resource, "symbolics");
		form.add(HtmlUtil.HR);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "symlink"));
		form.add(new HtmlTag("strong", "Symbolic Links"));

		HtmlTableListingBuilder table = new HtmlTableListingBuilder();
		table.getTable().addAttribute("style", "width:80%");
		table.addRow(new HtmlElement[]
		                 { new HtmlTag("strong", "Name")
				 	        , new HtmlTag("strong", "Path to Page")
				 	        , new HtmlTag("strong", "Actions")
                       , new HtmlTag("strong", "New Name")
		                 }
		            );
		addSymbolicLinkRows(table);
		addFormRow(table);
		form.add(table.getTable());

		return form;
	}

	private void addFormRow(HtmlTableListingBuilder table) throws Exception
	{
		HtmlTag nameInput = HtmlUtil.makeInputTag("text", "linkName");
		nameInput.addAttribute("size", "16%");
		HtmlTag pathInput = HtmlUtil.makeInputTag("text", "linkPath");
		pathInput.addAttribute("size", "60%");
		HtmlTag submitButton = HtmlUtil.makeInputTag("submit", "submit", "Create/Replace");
		submitButton.addAttribute("style", "width:8em");
		table.addRow(new HtmlElement[]{nameInput, pathInput, submitButton});
	}

	private void addSymbolicLinkRows(HtmlTableListingBuilder table) throws Exception
	{
		WikiPageProperty symLinksProperty = pageData.getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
		if(symLinksProperty == null)
			return;
		Set symbolicLinkNames = symLinksProperty.keySet();
		for(Iterator iterator = symbolicLinkNames.iterator(); iterator.hasNext();)
		{
			String linkName = (String) iterator.next();
			HtmlElement nameItem = new RawHtml(linkName);
			HtmlElement pathItem = makeHtmlForSymbolicPath(symLinksProperty, linkName);
			//---Unlink---
			HtmlTag actionItems = HtmlUtil.makeLink(resource + "?responder=symlink&removal=" + linkName, "Unlink&nbsp;");
			//---Rename---
			String callScript = "javascript:symbolicLinkRename('" + linkName + "','" + resource + "');";
			actionItems.tail = HtmlUtil.makeLink(callScript, "&nbsp;Rename:").html(); //..."linked list"
			
			HtmlTag newNameInput = HtmlUtil.makeInputTag("text", linkName);
			newNameInput.addAttribute("size", "16%");
			table.addRow(new HtmlElement[]{nameItem, pathItem, actionItems, newNameInput});
		}
	}

	private HtmlElement makeHtmlForSymbolicPath(WikiPageProperty symLinksProperty, String linkName) throws Exception
	{
		String linkPath = symLinksProperty.get(linkName);
		WikiPagePath wikiPagePath = PathParser.parse(linkPath);
		
		if(wikiPagePath != null)
		{
			WikiPage parent = wikiPagePath.isRelativePath()? page.getParent() : page; // TODO -AcD- a better way?
			PageCrawler crawler = parent.getPageCrawler();
			WikiPage target = crawler.getPage(parent, wikiPagePath);
			WikiPagePath fullPath;
			if (target != null)
			{
				fullPath = crawler.getFullPath(target);
				fullPath.makeAbsolute();
			}
			else
				fullPath = new WikiPagePath();
			return HtmlUtil.makeLink(fullPath.toString(), Utils.escapeText(linkPath));
		}
		else
			return new RawHtml(linkPath);
	}

	public static String getVirtualWikiValue(PageData data) throws Exception
	{
		String value = data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
		if(value == null)
			return "";
		else
			return value;
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}

	public HtmlTag makeTestActionCheckboxesHtml(PageData pageData) throws Exception
	{
		return makeAttributeCheckboxesHtml("Actions:", WikiPage.ACTION_ATTRIBUTES, pageData);
	}

	public HtmlElement makeNavigationCheckboxesHtml(PageData pageData) throws Exception
	{
		return makeAttributeCheckboxesHtml("Navigation:", WikiPage.NAVIGATION_ATTRIBUTES, pageData);
	}

	public HtmlTag makeSecurityCheckboxesHtml(PageData pageData) throws Exception
	{
		return makeAttributeCheckboxesHtml("Security:", WikiPage.SECURITY_ATTRIBUTES, pageData);
	}

	public HtmlTag makeSuitesHtml(PageData pageData) throws Exception
	{
		return makeInputField("Suites:", PageData.PropertySUITES, "Suites", 40, pageData);
	}

	public HtmlTag makeHelpTextHtml(PageData pageData) throws Exception
	{
		return makeInputField("Help Text:", PageData.PropertyHELP, "HelpText", 90, pageData);
	}

	public HtmlTag makeInputField (String label, String propertyName, String fieldId, int size, PageData pageData) 
		throws Exception
	{
		HtmlTag div = new HtmlTag("div");
		div.addAttribute("style", "float: left;");
		div.add(label);

		String textValue = "";
		WikiPageProperty theProp = pageData.getProperties().getProperty(propertyName);
		if(theProp != null)
		{
			String propValue = theProp.getValue();
			if (propValue != null)  textValue = propValue;
		}

		div.add(HtmlUtil.BR);
		HtmlTag input = HtmlUtil.makeInputTag("text", fieldId, textValue);
		input.addAttribute("size", Integer.toString(size));
		div.add(input);
		return div;
	}

	private HtmlTag makeAttributeCheckboxesHtml(String label, String[] attributes, PageData pageData)
		throws Exception
	{
		HtmlTag div = new HtmlTag("div");
		div.addAttribute("style", "float: left; width: 150px;");

		div.add(label);
		for(int i = 0; i < attributes.length; i++)
		{
			String attribute = attributes[i];
			div.add(HtmlUtil.BR);
			div.add(makeAttributeCheckbox(attribute, pageData));
		}
		div.add(HtmlUtil.BR);
		div.add(HtmlUtil.BR);
		return div;
	}

}
