// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import java.util.regex.*;

import fitnesse.responders.run.TestSystemBase;

public class PageResult
{
	private static final Pattern countsPattern = Pattern.compile("(\\d+)[^,]*, (\\d+)[^,]*, (\\d+)[^,]*, (\\d+)[^,]*");

	private StringBuffer contentBuffer = new StringBuffer();
	private TestSystemBase.TestSummary testSummary;
	private String title;

	public PageResult(String title)
	{
		this.title = title;
	}

	public PageResult(String title, TestSystemBase.TestSummary testSummary, String startingContent) throws Exception
	{
		this(title);
		this.testSummary = testSummary;
		append(startingContent);
	}

	public String content()
	{
		return contentBuffer.toString();
	}

	public void append(String data) throws Exception
	{
		contentBuffer.append(data);
	}

	public String title()
	{
		return title;
	}

	public TestSystemBase.TestSummary testSummary()
	{
		return testSummary;
	}

	public void setTestSummary(TestSystemBase.TestSummary testSummary)
	{
		this.testSummary = testSummary;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(title).append("\n");
		buffer.append(testSummary.toString()).append("\n");
		buffer.append(contentBuffer);
		return buffer.toString();
	}

	public static PageResult parse(String resultString) throws Exception
	{
		int firstEndlIndex = resultString.indexOf('\n');
		int secondEndlIndex = resultString.indexOf('\n', firstEndlIndex + 1);

		String title = resultString.substring(0, firstEndlIndex);
		TestSystemBase.TestSummary testSummary = parseCounts(resultString.substring(firstEndlIndex + 1, secondEndlIndex));
		String content = resultString.substring(secondEndlIndex + 1);

		return new PageResult(title, testSummary, content);
	}

	private static TestSystemBase.TestSummary parseCounts(String countString)
	{
		Matcher matcher = countsPattern.matcher(countString);
		if(matcher.find())
		{
			int right = Integer.parseInt(matcher.group(1));
			int wrong = Integer.parseInt(matcher.group(2));
			int ignores = Integer.parseInt(matcher.group(3));
			int exceptions = Integer.parseInt(matcher.group(4));
			return new TestSystemBase.TestSummary(right, wrong, ignores, exceptions);
		}
		else
			return null;
	}
}
