// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.RegexTestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;

public class NotFoundResponderTest extends RegexTestCase {
  public void testResponse() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("some page");

    Responder responder = new NotFoundResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(), request);

    assertEquals(404, response.getStatus());

    String body = response.getContent();

    assertHasRegexp("<html>", body);
    assertHasRegexp("<body", body);
    assertHasRegexp("some page", body);
    assertHasRegexp("Not Found", body);
  }

  public void testHasEditLinkForWikiWords() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("PageOne.PageTwo");
    WikiPage root = InMemoryPage.makeRoot("RooT");

    Responder responder = new NotFoundResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

    assertHasRegexp("\"PageOne[.]PageTwo[?]edit\"", response.getContent());
  }

}
