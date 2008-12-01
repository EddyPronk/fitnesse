// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.RegexTestCase;

public class DeleteConfirmationResponderTest extends RegexTestCase {
  MockRequest request;
  private FitNesseContext context;

  public void setUp() throws Exception {
    request = new MockRequest();
    context = new FitNesseContext();
  }

  public void testContentOfPage() throws Exception {
    request.setResource("files");
    request.addInput("filename", "MyFile.txt");
    Responder responder = new DeleteConfirmationResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String content = response.getContent();

    assertSubString("deleteFile", content);
    assertSubString("Delete File", content);
    assertSubString("MyFile.txt", content);
  }

}
