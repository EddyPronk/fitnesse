package fitnesse.responders.run.slimResponder;

import fitnesse.slim.SlimClient;
import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import static fitnesse.util.ListUtility.list;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.Utils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScriptTableTest {
  private WikiPage root;
  private List<Object> instructions;
  private final String scriptTableHeader = "|Script|\n";
  public ScriptTable st;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private ScriptTable makeScriptTableAndBuildInstructions(String pageContents) throws Exception {
    st = makeScriptTable(pageContents);
    st.appendInstructions(instructions);
    return st;
  }

  private ScriptTable makeScriptTable(String tableText) throws Exception {
    WikiPageUtil.setPageContents(root, tableText);
    TableScanner ts = new TableScanner(root.getData());
    Table t = ts.getTable(0);
    return new ScriptTable(t, "id");
  }

  private void assertScriptResults(String sriptStatements, List<Object> scriptResults, String table) throws Exception {
    buildInstructionsFor(sriptStatements);
    List<Object> resultList = list(list("scriptTable_id_0", "OK"));
    resultList.addAll(scriptResults);
    Map<String, Object> pseudoResults = SlimClient.resultToMap(resultList);
    st.evaluateExpectations(pseudoResults);
    assertEquals(table, Utils.unescapeWiki(st.getTable().toString()));
  }

  private void buildInstructionsFor(String scriptStatements) throws Exception {
    makeScriptTableAndBuildInstructions(scriptTableHeader + scriptStatements);
  }

  @Test
  public void instructionsForScriptTable() throws Exception {
    buildInstructionsFor("||\n");
    assertEquals(0, instructions.size());
  }

  @Test
  public void startStatement() throws Exception {
    buildInstructionsFor("|start|Bob|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "make", "scriptTableActor", "Bob")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void startStatementWithArguments() throws Exception {
    buildInstructionsFor("|start|Bob martin|x|y|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "make", "scriptTableActor", "BobMartin", "x", "y")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void simpleFunctionCall() throws Exception {
    buildInstructionsFor("|function|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void functionCallWithOneArgument() throws Exception {
    buildInstructionsFor("|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void functionCallWithOneArgumentAndTrailingName() throws Exception {
    buildInstructionsFor("|function|arg|trail|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "functionTrail", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void complexFunctionCallWithManyArguments() throws Exception {
    buildInstructionsFor("|eat|3|meals with|12|grams protein|3|grams fat |\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "eatMealsWithGramsProteinGramsFat", "3", "12", "3")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void checkWithFunction() throws Exception {
    buildInstructionsFor("|check|function|arg|result|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void checkWithFunctionAndTrailingName() throws Exception {
    buildInstructionsFor("|check|function|arg|trail|result|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "functionTrail", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void rejectWithFunctionCall() throws Exception {
    buildInstructionsFor("|reject|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void ensureWithFunctionCall() throws Exception {
    buildInstructionsFor("|ensure|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void showWithFunctionCall() throws Exception {
    buildInstructionsFor("|show|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void setSymbol() throws Exception {
    buildInstructionsFor("|$V=|function|arg|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "callAndAssign", "V", "scriptTableActor", "function", "arg")
      );
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void useSymbol() throws Exception {
    buildInstructionsFor("|function|$V|\n");
    List<Object> expectedInstructions =
      list(
        list("scriptTable_id_0", "call", "scriptTableActor", "function", "$V")
      );
    assertEquals(expectedInstructions, instructions);
  }


  @Test
  public void noteDoesNothing() throws Exception {
    buildInstructionsFor("|note|blah|blah|\n");
    List<Object> expectedInstructions = list();
    assertEquals(expectedInstructions, instructions);
  }

  @Test
  public void voidActionHasNoEffectOnColor() throws Exception {
    assertScriptResults("|func|\n",
      list(
        list("scriptTable_id_0", VoidConverter.VOID_TAG)
      ),
      "|!<Script>!|\n" +
        "|!<func>!|\n"
    );
  }

  @Test
  public void trueActionPasses() throws Exception {
    assertScriptResults("|func|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.TRUE)
      ),
      "|!<Script>!|\n" +
        "|!style_pass(!<func>!)|\n"
    );
  }

  @Test
  public void falseActionFails() throws Exception {
    assertScriptResults("|func|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.FALSE)
      ),
      "|!<Script>!|\n" +
        "|!style_fail(!<func>!)|\n"
    );
  }

  @Test
  public void checkPasses() throws Exception {
    assertScriptResults("|check|func|3|\n",
      list(
        list("scriptTable_id_0", "3")
      ),
      "|!<Script>!|\n" +
        "|!<check>!|!<func>!|!style_pass(!<3>!)|\n"
    );
  }

  @Test
  public void checkFails() throws Exception {
    assertScriptResults("|check|func|3|\n",
      list(
        list("scriptTable_id_0", "4")
      ),
      "|!<Script>!|\n" +
        "|!<check>!|!<func>!|[!<4>!] !style_fail(expected [!<3>!])|\n"
    );
  }

  @Test
  public void ensurePasses() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.TRUE)
      ),
      "|!<Script>!|\n" +
        "|!style_pass(!<ensure>!)|!<func>!|!<3>!|\n"
    );
  }

  @Test
  public void ensureFails() throws Exception {
    assertScriptResults("|ensure|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.FALSE)
      ),
      "|!<Script>!|\n" +
        "|!style_fail(!<ensure>!)|!<func>!|!<3>!|\n"
    );
  }

  @Test
  public void rejectPasses() throws Exception {
    assertScriptResults("|reject|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.FALSE)
      ),
      "|!<Script>!|\n" +
        "|!style_pass(!<reject>!)|!<func>!|!<3>!|\n"
    );
  }

  @Test
  public void rejectFails() throws Exception {
    assertScriptResults("|reject|func|3|\n",
      list(
        list("scriptTable_id_0", BooleanConverter.TRUE)
      ),
      "|!<Script>!|\n" +
        "|!style_fail(!<reject>!)|!<func>!|!<3>!|\n"
    );
  }

  @Test
  public void show() throws Exception {
    assertScriptResults("|show|func|3|\n",
      list(
        list("scriptTable_id_0", "kawabunga")
      ),
      "|!<Script>!|\n" +
        "|show|!<func>!|!<3>!|!style_ignore(!<kawabunga>!)|\n"
    );
  }

  @Test
  public void symbolReplacement() throws Exception {
    assertScriptResults(
      "|$V=|function|\n" +
        "|check|funcion|$V|$V|\n",
      list(
        list("scriptTable_id_0", "3"),
        list("scriptTable_id_1", "3")
      ),
      "|!<Script>!|\n" +
        "|$V<-[!<3>!]|!<function>!|\n" +
        "|!<check>!|!<funcion>!|!<$V->[3]>!|!style_pass(!<$V->[3]>!)|\n"
    );
  }
}
