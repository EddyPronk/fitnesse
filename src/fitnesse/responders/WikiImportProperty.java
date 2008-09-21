package fitnesse.responders;

import fitnesse.html.*;
import fitnesse.wiki.*;

import java.util.Date;

public class WikiImportProperty extends WikiPageProperty
{
	public static final String PROPERTY_NAME = "WikiImport";

	private WikiImportProperty()
	{}

	public WikiImportProperty(String source)
	{
		set("Source", source);
	}

	public String getSourceUrl()
	{
		return get("Source");
	}

	public boolean isRoot()
	{
		return has("IsRoot");
	}

	public void setRoot(boolean value)
	{
		if(value)
			set("IsRoot");
		else
			remove("IsRoot");
	}

	public boolean isAutoUpdate()
	{
		return has("AutoUpdate");
	}

	public void setAutoUpdate(boolean value)
	{
		if(value)
			set("AutoUpdate");
		else
			remove("AutoUpdate");
	}

	public static boolean isImported(PageData pageData) {
		try {
			return pageData.getProperties().has(PROPERTY_NAME);
		} catch (Exception e) {
		}
		return false;
	}
	
	public static WikiImportProperty createFrom(WikiPageProperty property)
	{
		if(property.has(PROPERTY_NAME))
		{
			WikiImportProperty importProperty = new WikiImportProperty();
			WikiPageProperty rawImportProperty = property.getProperty(PROPERTY_NAME);
			importProperty.set("Source", rawImportProperty.getProperty("Source"));
			importProperty.set("LastRemoteModification", rawImportProperty.getProperty("LastRemoteModification"));
			if(rawImportProperty.has("IsRoot"))
				importProperty.set("IsRoot", rawImportProperty.getProperty("IsRoot"));
			if(rawImportProperty.has("AutoUpdate"))
				importProperty.set("AutoUpdate", rawImportProperty.getProperty("AutoUpdate"));

			return importProperty;
		}
		else
			return null;
	}

	public void addTo(WikiPageProperty rootProperty)
	{
		rootProperty.set(PROPERTY_NAME, this);
	}

	public void setLastRemoteModificationTime(Date date)
	{
		set("LastRemoteModification", getTimeFormat().format(date));
	}

	public Date getLastRemoteModificationTime() throws Exception
	{
		Date date = new Date(0);
		String strValue = get("LastRemoteModification");
		if(strValue != null)
			date = getTimeFormat().parse(strValue);

		return date;
	}

	public static void handleImportProperties(HtmlPage html, WikiPage page, PageData pageData) throws Exception
	{
		html.actions.add(HtmlUtil.makeNavBreak());
		if (isImported(pageData)) 
		{
			html.body.addAttribute("class", "imported");
			WikiPagePath localPagePath = page.getPageCrawler().getFullPath(page);
			String localPageName = PathParser.render(localPagePath);
			html.actions.add(makeEditLocallyLink(localPageName));
			html.actions.add(HtmlUtil.makeNavBreak());
			String remoteInput = makeRemoteEditQueryParameters();
			WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
			html.actions.add(makeEditRemotelyLink(remoteInput, importProperty.getSourceUrl()));
		}
		else if (page instanceof ProxyPage)
			html.body.addAttribute("class", "virtual");
	}

  private static HtmlTag makeEditRemotelyLink(String remoteInput, String pageName) {
    HtmlUtil.ActionLink link = new HtmlUtil.ActionLink(pageName, "Edit Remotely");
    link.setQuery(remoteInput);
    return link.getHtml();
  }

  private static HtmlTag makeEditLocallyLink(String localPageName) {
    HtmlUtil.ActionLink link = new HtmlUtil.ActionLink(localPageName, "Edit Locally");
    link.setQuery("edit");
    return link.getHtml();
  }

  public static String makeRemoteEditQueryParameters() {
		return "responder=edit&amp;redirectToReferer=true&amp;redirectAction=importAndView";
	}
}
