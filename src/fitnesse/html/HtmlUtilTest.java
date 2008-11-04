// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import fitnesse.testutil.RegexTestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class HtmlUtilTest extends RegexTestCase {
  private static final String endl = HtmlElement.endl;

  private WikiPage root;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
  }

  public void testBreadCrumbsWithCurrentPageLinked() throws Exception {
    String trail = "1.2.3.4";
    HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithCurrentPageLinked(trail);
    String expected = getBreadCrumbsWithLastOneLinked();
    assertEquals(expected, breadcrumbs.html());
  }

  public void testBreadCrumbsWithCurrentPageNotLinked() throws Exception {
    String trail = "1.2.3.4";
    HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithCurrentPageNotLinked(trail);
    String expected = getBreadCrumbsWithLastOneNotLinked();
    assertEquals(expected, breadcrumbs.html());
  }

  public void testBreadCrumbsWithPageType() throws Exception {
    String trail = "1.2.3.4";
    HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithPageType(trail, "Some Type");
    String expected = getBreadCrumbsWithLastOneLinked() +
      "<br/><span class=\"page_type\">Some Type</span>" + endl;
    assertEquals(expected, breadcrumbs.html());
  }

  private String getBreadCrumbsWithLastOneLinked() {
    return getFirstThreeBreadCrumbs() +
      "<br/><a href=\"/1.2.3.4\" class=\"page_title\">4</a>" + endl;
  }

  private String getBreadCrumbsWithLastOneNotLinked() {
    return getFirstThreeBreadCrumbs() +
      "<br/><span class=\"page_title\">4</span>" + endl;
  }

  private String getFirstThreeBreadCrumbs() {
    return "<a href=\"/1\">1</a>." + endl +
      "<a href=\"/1.2\">2</a>." + endl +
      "<a href=\"/1.2.3\">3</a>." + endl;
  }

  public void testMakeFormTag() throws Exception {
    HtmlTag formTag = HtmlUtil.makeFormTag("method", "action");
    assertSubString("method", formTag.getAttribute("method"));
    assertSubString("action", formTag.getAttribute("action"));
  }

  public void testMakeDivTag() throws Exception {
    String expected = "<div class=\"myClass\"></div>" + HtmlElement.endl;
    assertEquals(expected, HtmlUtil.makeDivTag("myClass").html());
  }

  public void testMakeBreadCrumbsWithCurrentPageLinkedWithEmptyArray() throws Exception {
    try {
      HtmlUtil.makeBreadCrumbsWithCurrentPageLinked(".");
      HtmlUtil.makeBreadCrumbsWithCurrentPageLinked("");
    }
    catch (Exception e) {
      fail("should not throw exception");
    }
  }

  public void testMakeDefaultActions() throws Exception {
    String pageName = "SomePage";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, "SomePage");
  }

  public void testMakeActionsWithTestButtonWhenNameStartsWithTest() throws Exception {
    String pageName = "TestSomething";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  public void testMakeActionsWithSuffixButtonWhenNameEndsWithTest() throws Exception {
    String pageName = "SomethingTest";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
  }

  public void testMakeActionsWithSuiteButtonWhenNameStartsWithSuite() throws Exception {
    String pageName = "SuiteNothings";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
  }

  public void testMakeActionsWithSuiteButtonWhenNameEndsWithSuite() throws Exception {
    String pageName = "NothingsSuite";
    String html = getActionsHtml(pageName);
    verifyDefaultLinks(html, pageName);
    assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
  }

  @SuppressWarnings("serial")
  private String getActionsHtml(String pageName) throws Exception {
    root.addChildPage(pageName);
    PageData pageData = new PageData(root.getChildPage(pageName)) {
      public String getVariable(String name) throws Exception {
        return null;
      }
    };
    return HtmlUtil.makeActions(pageData, pageName, pageName, false).html();
  }

  private void verifyDefaultLinks(String html, String pageName) {
    assertSubString("<a href=\"" + pageName + "?edit\" accesskey=\"e\">Edit</a>", html);
    assertSubString("<a href=\"" + pageName + "?versions\" accesskey=\"v\">Versions</a>", html);
    assertSubString("<a href=\"" + pageName + "?properties\" accesskey=\"p\">Properties</a>", html);
    assertSubString("<a href=\"" + pageName + "?refactor\" accesskey=\"r\">Refactor</a>", html);
    assertSubString("<a href=\"" + pageName + "?whereUsed\" accesskey=\"w\">Where Used</a>", html);
    assertSubString("<a href=\"/files\" accesskey=\"f\">Files</a>", html);
    assertSubString("<a href=\"?searchForm\" accesskey=\"s\">Search</a>", html);
    assertSubString("<a href=\".FitNesse.UserGuide\" accesskey=\"\">User Guide</a>", html);
  }
}
