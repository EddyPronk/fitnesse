// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import static fitnesse.revisioncontrol.NullState.ADDED;
import static fitnesse.revisioncontrol.NullState.UNKNOWN;
import static fitnesse.revisioncontrol.RevisionControlOperation.STATE;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.File;
import java.util.HashSet;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.RegexTestCase;
import fitnesse.util.FileUtil;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.VirtualCouplingExtensionTest;
import fitnesse.wiki.WikiPage;

public class WikiPageResponderTest extends RegexTestCase {
    private WikiPage root;
    private PageCrawler crawler;

    @Override
    public void setUp() throws Exception {
        root = InMemoryPage.makeRoot("root");
        crawler = root.getPageCrawler();
    }

    public void testResponse() throws Exception {
        crawler.addPage(root, PathParser.parse("ChildPage"), "child content");
        MockRequest request = new MockRequest();
        request.setResource("ChildPage");

        Responder responder = new WikiPageResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

        assertEquals(200, response.getStatus());

        String body = response.getContent();

        assertSubString("<html>", body);
        assertSubString("<body", body);
        assertSubString("child content", body);
        assertSubString("href=\"ChildPage?whereUsed\"", body);
        assertSubString("ChildPage</span>", body);
        assertSubString("Cache-Control: max-age=0", response.makeHttpHeaders());
    }

    public void testAttributeButtons() throws Exception {
        crawler.addPage(root, PathParser.parse("NormalPage"));
        WikiPage noButtonsPage = crawler.addPage(root, PathParser.parse("NoButtonPage"));
        for (int i = 0; i < WikiPage.NON_SECURITY_ATTRIBUTES.length; i++) {
            String attribute = WikiPage.NON_SECURITY_ATTRIBUTES[i];
            PageData data = noButtonsPage.getData();
            data.removeAttribute(attribute);
            noButtonsPage.commit(data);
        }

        SimpleResponse response = requestPage("NormalPage");
        assertSubString("<!--Edit button-->", response.getContent());
        assertSubString("<!--Search button-->", response.getContent());
        assertSubString("<!--Versions button-->", response.getContent());
        assertNotSubString("<!--Suite button-->", response.getContent());
        assertNotSubString("<!--Test button-->", response.getContent());

        response = requestPage("NoButtonPage");
        assertNotSubString("<!--Edit button-->", response.getContent());
        assertNotSubString("<!--Search button-->", response.getContent());
        assertNotSubString("<!--Versions button-->", response.getContent());
        assertNotSubString("<!--Suite button-->", response.getContent());
        assertNotSubString("<!--Test button-->", response.getContent());
    }

    public void testHeadersAndFooters() throws Exception {
        crawler.addPage(root, PathParser.parse("NormalPage"), "normal");
        crawler.addPage(root, PathParser.parse("TestPage"), "test page");
        crawler.addPage(root, PathParser.parse("PageHeader"), "header");
        crawler.addPage(root, PathParser.parse("PageFooter"), "footer");
        crawler.addPage(root, PathParser.parse("SetUp"), "setup");
        crawler.addPage(root, PathParser.parse("TearDown"), "teardown");

        SimpleResponse response = requestPage("NormalPage");
        String content = response.getContent();
        assertHasRegexp("header", content);
        assertHasRegexp("normal", content);
        assertHasRegexp("footer", content);
        assertDoesntHaveRegexp("setup", content);
        assertDoesntHaveRegexp("teardown", content);

        response = requestPage("TestPage");
        content = response.getContent();
        assertHasRegexp("header", content);
        assertHasRegexp("test page", content);
        assertHasRegexp("footer", content);
        assertHasRegexp("setup", content);
        assertHasRegexp("teardown", content);
    }

    private SimpleResponse requestPage(String name) throws Exception {
        MockRequest request = new MockRequest();
        request.setResource(name);
        Responder responder = new WikiPageResponder();
        return (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    }

    public void testShouldGetVirtualPage() throws Exception {
        WikiPage pageOne = crawler.addPage(root, PathParser.parse("TargetPage"), "some content");
        crawler.addPage(pageOne, PathParser.parse("ChildPage"), "child content");
        WikiPage linkerPage = crawler.addPage(root, PathParser.parse("LinkerPage"), "linker content");
        FitNesseUtil.bindVirtualLinkToPage(linkerPage, pageOne);
        SimpleResponse response = requestPage("LinkerPage.ChildPage");

        assertSubString("child content", response.getContent());
    }

    public void testVirtualPageIndication() throws Exception {
        WikiPage targetPage = crawler.addPage(root, PathParser.parse("TargetPage"));
        crawler.addPage(targetPage, PathParser.parse("ChildPage"));
        WikiPage linkPage = crawler.addPage(root, PathParser.parse("LinkPage"));
        VirtualCouplingExtensionTest.setVirtualWiki(linkPage, "http://localhost:" + FitNesseUtil.port + "/TargetPage");

        FitNesseUtil.startFitnesse(root);
        SimpleResponse response = null;
        try {
            response = requestPage("LinkPage.ChildPage");
        } finally {
            FitNesseUtil.stopFitnesse();
        }

        assertSubString("<body class=\"virtual\">", response.getContent());
    }

    public void testImportedPageIndication() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("SamplePage"));
        PageData data = page.getData();
        WikiImportProperty importProperty = new WikiImportProperty("blah");
        importProperty.addTo(data.getProperties());
        page.commit(data);

        String content = requestPage("SamplePage").getContent();

        assertSubString("<body class=\"imported\">", content);
    }

    public void testResponderIsSecureReadOperation() throws Exception {
        Responder responder = new WikiPageResponder();
        assertTrue(responder instanceof SecureResponder);
        SecureOperation operation = ((SecureResponder) responder).getSecureOperation();
        assertEquals(SecureReadOperation.class, operation.getClass());
    }

    public void testShouldDisplayRevisionControlMenuIfPageIsEditableOrImportedAndUnderRevisionControl() throws Exception {
        RevisionController revisionController = createMock(RevisionController.class);
        String rootDir = "testDir";
        String pageName = "RevisionControlledPage";

        expect(revisionController.history((FileSystemPage) anyObject())).andStubReturn(new HashSet<VersionInfo>());
        expect(revisionController.add((String) anyObject())).andStubReturn(ADDED);
        expect(revisionController.isExternalReversionControlEnabled()).andReturn(true);
        expect(revisionController.execute(STATE, contentAndPropertiesFilePath(rootDir + "/ExternalRoot/" + pageName))).andReturn(UNKNOWN);
        replay(revisionController);
        try {
            FileUtil.createDir(rootDir);
            root = FileSystemPage.makeRoot(rootDir, "ExternalRoot", revisionController);
            root.addChildPage(pageName);
            SimpleResponse response = requestPage(pageName);
            assertSubString("<div class=\"main\">Revision Control</div>", response.getContent());
            assertSubString("<a href=\"" + pageName + "?addToRevisionControl\" accesskey=\"a\">Add</a>", response.getContent());
        } finally {
            FileUtil.deleteFileSystemDirectory(rootDir);
            verify(revisionController);
        }
    }

    public void testShouldOnlyShowRevisionControlMenuForFileSystemPage() throws Exception {
        crawler.addPage(root, PathParser.parse("NormalPage"), "normal");

        SimpleResponse response = requestPage("NormalPage");
        String content = response.getContent();
        assertHasRegexp("header", content);
        assertHasRegexp("normal", content);
        assertDoesntHaveRegexp("Revision Control", content);
    }

    private String[] contentAndPropertiesFilePath(String basePath) {
        return new String[] { new File(basePath + FileSystemPage.contentFilename).getAbsolutePath(),
                new File(basePath + FileSystemPage.propertiesFilename).getAbsolutePath() };
    }
}
