// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;

import java.util.regex.Pattern;

import fitnesse.html.HtmlElement;

public class HruleWidgetTest extends TestCase
{
	private static String endl = HtmlElement.endl;
	
	public void testRegexp() throws Exception
	{
		assertTrue("match1", Pattern.matches(HruleWidget.REGEXP, "----"));
		assertTrue("match2", Pattern.matches(HruleWidget.REGEXP, "------------------"));
		assertTrue("match3", !Pattern.matches(HruleWidget.REGEXP, "--- -"));
	}

	public void testGetSize() throws Exception
	{
		HruleWidget widget = new HruleWidget(new MockWidgetRoot(), "----");
		assertEquals(0, widget.getExtraDashes());
		widget = new HruleWidget(new MockWidgetRoot(), "-----");
		assertEquals(1, widget.getExtraDashes());
		widget = new HruleWidget(new MockWidgetRoot(), "--------------");
		assertEquals(10, widget.getExtraDashes());
	}

	public void testHtml() throws Exception
	{
		HruleWidget widget = new HruleWidget(new MockWidgetRoot(), "----");
		assertEquals("<hr/>" + endl, widget.render());
		widget = new HruleWidget(new MockWidgetRoot(), "------");
		assertEquals("<hr size=\"3\"/>" + endl, widget.render());
	}
}