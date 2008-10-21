// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.components.ClassPathBuilder;
import fitnesse.components.FitClient;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.components.FitProtocol;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.wiki.*;

import java.net.Socket;
import java.util.List;

public class FitClientResponder implements Responder, ResponsePuppeteer, TestSystemListener
{
	private FitNesseContext context;
	private PageCrawler crawler;
	private String resource;
	private WikiPage page;
	private boolean shouldIncludePaths;
	private String suiteFilter;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		this.context = context;
		crawler = context.root.getPageCrawler();
		crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
		resource = request.getResource();
		shouldIncludePaths = request.hasInput("includePaths");
		suiteFilter = (String) request.getInput("suiteFilter");
		return new PuppetResponse(this);
	}

	public void readyToSend(ResponseSender sender) throws Exception
	{
		Socket socket = sender.getSocket();
		WikiPagePath pagePath = PathParser.parse(resource);
		if(!crawler.pageExists(context.root, pagePath))
			FitProtocol.writeData(notFoundMessage(), socket.getOutputStream());
		else
		{
			page = crawler.getPage(context.root, pagePath);
			PageData data = page.getData();

			if(data.hasAttribute("Suite"))
				handleSuitePage(socket, page, context.root);
			else if(data.hasAttribute("Test"))
				handleTestPage(socket, data);
			else
				FitProtocol.writeData(notATestMessage(), socket.getOutputStream());
		}
		sender.close();
	}

	private void handleTestPage(Socket socket, PageData data) throws Exception
	{
		FitClient client = startClient(socket);

		if(shouldIncludePaths)
		{
			String classpath = new ClassPathBuilder().getClasspath(page);
			client.send(classpath);
		}

		sendPage(data, client, true);
		closeClient(client);
	}

	private void handleSuitePage(Socket socket, WikiPage page, WikiPage root) throws Exception
	{
		FitClient client = startClient(socket);
		List<WikiPage> testPages = SuiteResponder.makePageList(page, root, suiteFilter);

		if(shouldIncludePaths)
		{
			String classpath = SuiteResponder.buildClassPath(testPages, page);
			client.send(classpath);
		}

    for (WikiPage testPage : testPages) {
      PageData testPageData = testPage.getData();
      sendPage(testPageData, client, false);
    }
    closeClient(client);
	}

	private void sendPage(PageData data, FitClient client, boolean includeSuiteSetup) throws Exception
	{
		String pageName = crawler.getRelativeName(page, data.getWikiPage());
    SetupTeardownIncluder.includeInto(data, includeSuiteSetup);
    String testableHtml = data.getHtml();
		String sendableHtml = pageName + "\n" + testableHtml;
		client.send(sendableHtml);
	}

	private void closeClient(FitClient client) throws Exception
	{
		client.done();
		client.join();
	}

	private FitClient startClient(Socket socket) throws Exception
	{
		FitClient client = new FitClient(this);
		client.acceptSocket(socket);
		return client;
	}

	private String notATestMessage()
	{
		return resource + " is neither a Test page nor a Suite page.";
	}

	private String notFoundMessage()
	{
		return "The page " + resource + " was not found.";
	}

	public void acceptOutput(String output) throws Exception
	{
	}

	public void acceptResults(TestSystem.TestSummary testSummary) throws Exception
	{
	}

	public void exceptionOccurred(Throwable e)
	{
	}
}
