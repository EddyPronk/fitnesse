package fitnesse.slim;

import static fitnesse.util.ListUtility.list;
import fitnesse.components.CommandRunner;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlimServiceTest {
  private List<Object> statements;
  private SlimClient slimClient = new SlimClient("localhost", 8099);
  private CommandRunner runner;
  @Before
  public void setUp() throws Exception {
    slimClient = new SlimClient("localhost", 8099);
    createSlimService();
    statements = new ArrayList<Object>();
	System.err.println("try to connect");
    slimClient.connect();
	System.err.println("connected?");
  }

  protected void createSlimService() throws Exception {
    while (!tryCreateSlimService())
      Thread.sleep(10);
  }

  private boolean tryCreateSlimService() throws Exception {
    try {
	//runner = new CommandRunner("java -cp classes:fitnesse.jar:fitlibrary.jar fitnesse.slim.SlimService 8099", "");
	runner = new CommandRunner("python /home/epronk/stuff/pyfit/fitnesse/slim/SlimService.py classes:fitnesse.jar:fitlibrary.jar fitnesse.slim.SlimService 8099", "");
	System.err.println("starting service");
	runner.start();
	Thread.sleep(300);
	System.err.println("started");
	return true;
    } catch (Exception e) {
      return false;
    }
  }

  @After
  public void after() throws Exception {
    teardown();
  }

  protected void teardown() throws Exception {
    slimClient.sendBye();
    slimClient.close();
	runner.join();
	Thread.sleep(300);
  }

	///@Test
  public void emptySession() throws Exception {
    assertTrue("Connected", slimClient.isConnected());
  }

	///@Test
		public void callOneMethod() throws Exception {
    addImportAndMake();
    addEchoInt("id", "1");
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("1", result.get("id"));
  }

  private void addEchoInt(String id, String number) {
    statements.add(list(id, "call", "testSlim", "echoInt", number));
  }

  private void addImportAndMake() {
<<<<<<< HEAD:src/fitnesse/slim/SlimServiceTest.java
    statements.add(list("i1", "import", getImport()));
    statements.add(list("m1", "make", "testSlim", "TestSlim"));
  }

  protected String getImport() {
    return "fitnesse.slim.test";
  }

  @Test
=======
    statements.add(list("i1", "import", "test.TestSlim"));
    statements.add(list("m1", "make", "testSlim", "TestSlim"));
  }

	//@Test
>>>>>>> hack to test python slim:src/fitnesse/slim/SlimServiceTest.java
  public void makeManyCallsInOrderToTestLongSequencesOfInstructions() throws Exception {
    addImportAndMake();
    for (int i = 0; i < 1000; i++)
      addEchoInt(String.format("id_%d", i), Integer.toString(i));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    for (int i = 0; i < 1000; i++)
      assertEquals(i, Integer.parseInt((String) result.get(String.format("id_%d", i))));
  }

	///@Test
  public void callWithLineBreakInStringArgument() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "testSlim", "echoString", "hello\nworld\n"));
    Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
    assertEquals("hello\nworld\n", result.get("id"));
  }

	@Test
  public void makeManyIndividualCalls() throws Exception {
    addImportAndMake();
    slimClient.invokeAndGetResponse(statements);
    for (int i = 0; i < 1; i++) {
      statements.clear();
      addEchoInt("id", "42");
      Map<String, Object> result = slimClient.invokeAndGetResponse(statements);
      assertEquals(1, result.size());
      assertEquals("42", result.get("id"));
    }
  }

	///@Test
  public void callFunctionThatDoesntExist() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "testSlim", "noSuchFunction"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<NO_METHOD_IN_CLASS", "id", results);
  }

  private void assertContainsException(String message, String id, Map<String, Object> results) {
    String result = (String) results.get(id);
    assertTrue(result, result.indexOf(SlimServer.EXCEPTION_TAG) != -1 && result.indexOf(message) != -1);
  }

	///@Test
  public void makeClassThatDoesntExist() throws Exception {
    statements.add(list("m1", "make", "me", "NoSuchClass"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR", "m1", results);
  }

	///@Test
  public void useInstanceThatDoesntExist() throws Exception {
    addImportAndMake();
    statements.add(list("id", "call", "noInstance", "f"));
    Map<String, Object> results = slimClient.invokeAndGetResponse(statements);
    assertContainsException("message:<<NO_INSTANCE", "id", results);
  }

	///@Test
  public void verboseArgument() throws Exception {
    String args[] = {"-v", "99"};
    assertTrue(SlimService.parseCommandLine(args));
    assertTrue(SlimService.verbose);
  }


}

