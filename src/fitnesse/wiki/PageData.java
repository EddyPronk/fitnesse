// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fitnesse.components.SaveRecorder;
import fitnesse.responders.editing.EditResponder;
import fitnesse.responders.run.SuiteResponder;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.ClasspathWidget;
import fitnesse.wikitext.widgets.FixtureWidget;
import fitnesse.wikitext.widgets.IncludeWidget;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.PreformattedWidget;
import fitnesse.wikitext.widgets.TextIgnoringWidgetRoot;
import fitnesse.wikitext.widgets.VariableDefinitionWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import fitnesse.wikitext.widgets.WidgetWithTextArgument;
import fitnesse.wikitext.widgets.XRefWidget;

public class PageData implements Serializable
{
	public static WidgetBuilder classpathWidgetBuilder = new WidgetBuilder(new Class[]{IncludeWidget.class, VariableDefinitionWidget.class, ClasspathWidget.class});
	public static WidgetBuilder fixtureWidgetBuilder = new WidgetBuilder(new Class[]{FixtureWidget.class});
	public static WidgetBuilder xrefWidgetBuilder = new WidgetBuilder(new Class[]{XRefWidget.class});

	public static WidgetBuilder
		variableDefinitionWidgetBuilder = new WidgetBuilder(new Class[]
		{IncludeWidget.class,
			PreformattedWidget.class,
			VariableDefinitionWidget.class
		});

	public static final String PropertyHELP = "Help";
	public static final String PropertyPRUNE     = "Prune";
	//TODO -AcD: refactor add other properties such as "Edit", "Suite", "Test", ...
	public static final String PropertySUITES = "Suites";

	private transient WikiPage wikiPage;
	private String content;
	private WikiPageProperties properties = new WikiPageProperties();
	private Set versions;
	private ParentWidget variableRoot;
	private List literals;

	public PageData(WikiPage page) throws Exception
	{
		wikiPage = page;
		initializeAttributes();
		versions = new HashSet();
	}

	public PageData(WikiPage page, String content) throws Exception
	{
		this(page);
		setContent(content);
	}

	public PageData(PageData data) throws Exception
	{
		this(data.getWikiPage());
		wikiPage = data.wikiPage;
		content = data.content;
		properties = new WikiPageProperties(data.properties);
		versions.addAll(data.versions);
	}

	public String getStringOfAllAttributes()
	{
		return properties.toString();
	}

	public void initializeAttributes() throws Exception
	{
		properties.set("Edit", "true");
		properties.set("Versions", "true");
		properties.set("Properties", "true");
		properties.set("Refactor", "true");
		properties.set("WhereUsed", "true");
		properties.set("Files", "true");
		properties.set("RecentChanges", "true");
		properties.set("Search", "true");
		properties.set(EditResponder.TICKET_ID, SaveRecorder.newTicket() + "");
		properties.setLastModificationTime(new Date());

		initTestOrSuiteProperty();
	}

	private void initTestOrSuiteProperty() throws Exception {
		final String pageName = wikiPage.getName();
		if (pageName == null) {
			handleInvalidPageName(wikiPage);
			return;
		}
		if(pageName.startsWith("Test") || pageName.endsWith("Test"))
			properties.set("Test", "true");
		if((pageName.startsWith("Suite") || pageName.endsWith("Suite")) &&
			!pageName.equals(SuiteResponder.SUITE_SETUP_NAME) &&
			!pageName.equals(SuiteResponder.SUITE_TEARDOWN_NAME))
		{
			properties.set("Suite", "true");
		}
	}

	// TODO: Should be written to a real logger, but it doesn't like FitNesse's logger is
	// really intended for general logging.
	private void handleInvalidPageName(WikiPage wikiPage) {
		try {
			System.err.println("WikiPage "+wikiPage+" does not have a valid name!"+wikiPage.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public WikiPageProperties getProperties() throws Exception
	{
		return properties;
	}

	public String getAttribute(String key) throws Exception
	{
		return properties.get(key);
	}

	public void removeAttribute(String key) throws Exception
	{
		properties.remove(key);
	}

	public void setAttribute(String key, String value) throws Exception
	{
		properties.set(key, value);
	}

	public void setAttribute(String key) throws Exception
	{
		properties.set(key);
	}

	public boolean hasAttribute(String attribute) throws Exception
	{
		return properties.has(attribute);
	}

	public void setProperties(WikiPageProperties properties)
	{
		this.properties = properties;
	}

	public String getContent() throws Exception
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getHtml() throws Exception
	{
		return processHTMLWidgets(getContent(), wikiPage);
	}

	public String getHtml(WikiPage context) throws Exception
	{
		return processHTMLWidgets(getContent(), context);
	}

	public String getVariable(String name) throws Exception
	{
		initializeVariableRoot();
		return variableRoot.getVariable(name);
	}

	public void setLiterals(List literals)
	{
		this.literals = literals;
	}

	private void initializeVariableRoot() throws Exception
	{
		if(variableRoot == null)
		{
			variableRoot = new TextIgnoringWidgetRoot(getContent(), wikiPage, literals, variableDefinitionWidgetBuilder);
			variableRoot.render();
		}
	}

	public void addVariable(String name, String value) throws Exception
	{
		initializeVariableRoot();
		variableRoot.addVariable(name, value);
	}

	private String processHTMLWidgets(String content, WikiPage context) throws Exception
	{
		ParentWidget root = new WidgetRoot(content, context, WidgetBuilder.htmlWidgetBuilder);
		return root.render();
	}

	public void setWikiPage(WikiPage page)
	{
		wikiPage = page;
	}

	public WikiPage getWikiPage()
	{
		return wikiPage;
	}

	public List<String> getClasspaths() throws Exception
	{
		return getTextOfWidgets(classpathWidgetBuilder);
	}

	public List getFixtureNames() throws Exception
	{
		return getTextOfWidgets(fixtureWidgetBuilder);
	}

	public List getXrefPages() throws Exception
	{
		return getTextOfWidgets(xrefWidgetBuilder);
	}

	private List<String> getTextOfWidgets(WidgetBuilder builder) throws Exception
	{
		ParentWidget root = new TextIgnoringWidgetRoot(getContent(), wikiPage, builder);
		List<WikiWidget> widgets = root.getChildren();
		List<String> values = new ArrayList<String>();
		for(WikiWidget widget : widgets)
		{
			if(widget instanceof WidgetWithTextArgument)
				values.add(((WidgetWithTextArgument) widget).getText());
			else
				widget.render();
		}
		return values;
	}

	public Set getVersions()
	{
		return versions;
	}

	public void addVersions(Collection newVersions)
	{
		versions.addAll(newVersions);
	}
}
