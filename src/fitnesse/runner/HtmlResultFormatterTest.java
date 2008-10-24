// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fitnesse.html.HtmlPageFactory;
import fitnesse.testutil.RegexTestCase;
import fitnesse.util.StreamReader;
import fitnesse.responders.run.TestSummary;

import java.io.InputStream;

public class HtmlResultFormatterTest extends RegexTestCase
{
	private HtmlResultFormatter formatter;

	public void setUp() throws Exception
	{
		formatter = new HtmlResultFormatter(new HtmlPageFactory(), "somehost.com:8080", "FitNesse");
	}

	public void testIsValidHtml() throws Exception
	{
		String html = getHtml().trim();
		assertTrue(html.startsWith("<!DOCTYPE HTML"));
		assertTrue(html.endsWith("</html>"));

		assertSubString("<base href=\"http://somehost.com:8080/\"", html);
		assertSubString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>", html);
		assertSubString("href=\"/files/css/fitnesse_print.css\"", html);
		assertNotSubString("href=\"/files/css/fitnesse.css\"", html);
		assertSubString("Command Line Test Results", html);
		assertSubString(HtmlResultFormatter.scriptContent, html);
		assertSubString("<body onload=\"localizeInPageLinks()\"", html);
	}

	public void testUsage() throws Exception
	{
		formatter.acceptResult(new PageResult("PageOne", new TestSummary(1, 0, 0, 0), "page one"));
		formatter.acceptResult(new PageResult("PageTwo", new TestSummary(0, 1, 0, 0), "page two"));
		formatter.acceptFinalCount(new TestSummary(1, 1, 0, 0));

		String html = getHtml();

		assertSubString("PageOne", html);
		assertSubString("page one", html);
		assertSubString("pass", html);

		assertSubString("PageTwo", html);
		assertSubString("page two", html);
		assertSubString("fail", html);
	}

	private String getHtml() throws Exception
	{
		InputStream input = formatter.getResultStream();
		assertNotNull(input);

		int bytes = formatter.getByteCount();
		String html = new StreamReader(input).read(bytes);
		return html;
	}
}
