// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fitnesse.testutil.RegexTestCase;
import fitnesse.util.StreamReader;
import fitnesse.responders.run.TestSystemBase;

public class CachingResultFormatterTest extends RegexTestCase
{
	public void testAddResult() throws Exception
	{
		CachingResultFormatter formatter = new CachingResultFormatter();
		PageResult result = new PageResult("PageTitle", new TestSystemBase.TestSummary(1, 2, 3, 4), "content");
		formatter.acceptResult(result);
		formatter.acceptFinalCount(new TestSystemBase.TestSummary(1, 2, 3, 4));

		String content = new StreamReader(formatter.getResultStream()).read(formatter.getByteCount());
		assertSubString("0000000060", content);
		assertSubString(result.toString(), content);
		assertSubString("0000000001", content);
		assertSubString("0000000002", content);
		assertSubString("0000000003", content);
		assertSubString("0000000004", content);
	}

	public void testIsComposit() throws Exception
	{
		CachingResultFormatter formatter = new CachingResultFormatter();
		MockResultFormatter mockFormatter = new MockResultFormatter();
		formatter.addHandler(mockFormatter);

		PageResult result = new PageResult("PageTitle", new TestSystemBase.TestSummary(1, 2, 3, 4), "content");
		formatter.acceptResult(result);
		TestSystemBase.TestSummary testSummary = new TestSystemBase.TestSummary(1, 2, 3, 4);
		formatter.acceptFinalCount(testSummary);

		assertEquals(1, mockFormatter.results.size());
		assertEquals(result.toString(), mockFormatter.results.get(0).toString());
		assertEquals(testSummary, mockFormatter.finalSummary);
	}
}
