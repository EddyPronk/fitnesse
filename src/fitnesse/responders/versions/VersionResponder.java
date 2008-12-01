// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.versions;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.responders.SecureResponder;
import fitnesse.wiki.*;

public class VersionResponder implements SecureResponder {
  private WikiPage page;
  private String version;
  private String resource;

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    resource = request.getResource();
    version = (String) request.getInput("version");

    PageCrawler pageCrawler = context.root.getPageCrawler();
    WikiPagePath path = PathParser.parse(resource);
    page = pageCrawler.getPage(context.root, path);
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);
    PageData pageData = page.getDataVersion(version);

    String fullPathName = PathParser.render(pageCrawler.getFullPath(page));
    HtmlPage html = makeHtml(fullPathName, pageData, context);

    SimpleResponse response = new SimpleResponse();
    response.setContent(html.html());

    return response;
  }

  private HtmlPage makeHtml(String name, PageData pageData, FitNesseContext context) throws Exception {
    HtmlPage html = context.htmlPageFactory.newPage();
    html.title.use("Version " + version + ": " + name);
    html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Version " + version));
    html.actions.use(makeRollbackLink(name));
    html.main.use(HtmlUtil.makeNormalWikiPageContent(pageData));
    return html;
  }

  private HtmlTag makeRollbackLink(String name) {
    HtmlUtil.ActionLink link = new HtmlUtil.ActionLink(name, "Rollback");
    link.setQuery("responder=rollback&version=" + version);
    link.setShortcutKey("");
    return link.getHtml();
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
