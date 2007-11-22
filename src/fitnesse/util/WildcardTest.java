// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WildcardTest extends TestCase
{
	private File testDir;
	private static final String TEST_DIR = "testDir";

	public void setUp() throws Exception
	{
		makeSampleFiles();
		testDir = new File(TEST_DIR);
	}

	public void tearDown() throws Exception
	{
		deleteSampleFiles();
	}

	public void testJar() throws Exception
	{
		Wildcard wildcard = new Wildcard("*.jar");
		File[] files = testDir.listFiles(wildcard);
		List list = fileArrayToStringList(files);
		assertEquals(2, files.length);
		assertTrue(list.contains("one.jar"));
		assertTrue(list.contains("two.jar"));
	}

	public void testDll() throws Exception
	{
		Wildcard wildcard = new Wildcard("*.dll");
		File[] files = testDir.listFiles(wildcard);
		List list = fileArrayToStringList(files);
		assertEquals(2, files.length);
		assertTrue(list.contains("one.dll"));
		assertTrue(list.contains("two.dll"));
	}

	public void testOne() throws Exception
	{
		Wildcard wildcard = new Wildcard("one*");
		File[] files = testDir.listFiles(wildcard);
		List list = fileArrayToStringList(files);
		assertEquals(3, files.length);
		assertTrue(list.contains("oneA"));
		assertTrue(list.contains("one.jar"));
		assertTrue(list.contains("one.dll"));
	}

	public void testAll() throws Exception
	{
		Wildcard wildcard = new Wildcard("*");
		File[] files = testDir.listFiles(wildcard);
		assertEquals(6, files.length);
	}

	private List fileArrayToStringList(File[] files)
	{
		List list = new ArrayList();
		for(int i = 0; i < files.length; i++)
		{
			File file = files[i];
			list.add(file.getName());
		}
		return list;
	}

	public static void makeSampleFiles()
	{
		FileUtil.makeDir(TEST_DIR);
		FileUtil.createFile(TEST_DIR+"/one.jar", "");
		FileUtil.createFile(TEST_DIR+"/two.jar", "");
		FileUtil.createFile(TEST_DIR+"/one.dll", "");
		FileUtil.createFile(TEST_DIR+"/two.dll", "");
		FileUtil.createFile(TEST_DIR+"/oneA", "");
		FileUtil.createFile(TEST_DIR+"/twoA", "");
	}

	public static void deleteSampleFiles()
	{
		FileUtil.deleteFileSystemDirectory(TEST_DIR);
	}


}
