// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;
import junit.framework.TestCase;
import fitnesse.responders.run.TestSystem;

public class PageResultTest extends TestCase
{
	public void testToString() throws Exception
	{
		PageResult result = new PageResult("PageTitle", new TestSystem.TestSummary(1, 2, 3, 4), "content");
		assertEquals("PageTitle\n1 right, 2 wrong, 3 ignored, 4 exceptions\ncontent", result.toString());
	}

	public void testParse() throws Exception
	{
		TestSystem.TestSummary testSummary = new TestSystem.TestSummary(1, 2, 3, 4);
		PageResult result = new PageResult("PageTitle", testSummary, "content");
		PageResult parsedResult = PageResult.parse(result.toString());
		assertEquals("PageTitle", parsedResult.title());
		assertEquals(testSummary, parsedResult.testSummary());
		assertEquals("content", parsedResult.content());
	}
}
