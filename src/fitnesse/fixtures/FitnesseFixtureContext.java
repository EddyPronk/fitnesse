// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.responders.ResponderFactory;
import fitnesse.wiki.WikiPage;

public class FitnesseFixtureContext {
  public static WikiPage root;
  public static WikiPage page;
  public static Response response;
  public static MockResponseSender sender;
  public static ResponderFactory responderFactory;
  public static String baseDir = "temp";
  public static FitNesseContext context;
  public static FitNesse fitnesse;
}
