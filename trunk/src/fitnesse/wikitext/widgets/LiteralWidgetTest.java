// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;


public class LiteralWidgetTest extends WidgetTestCase
{
	public void testMatches() throws Exception
	{  //Paren Literal: () -> ??
      assertMatch("!lit?0?" );
      assertMatch("!lit?99?");
      assertNoMatch("!lit?-1?");
      assertNoMatch("!lit?a?" );
	}

	protected String getRegexp()
	{
		return LiteralWidget.REGEXP;
	}

	public void testWikiWordIsNotParsed() throws Exception
	{
		ParentWidget root = new MockWidgetRoot();
		root.defineLiteral("Bob");
  		//Paren Literal: () -> ??
  		LiteralWidget w = new LiteralWidget(root, "!lit?0?");
		String html = w.render();
		assertEquals("Bob", html);
	}
}
