package fitnesse.responders.run.slimResponder;

import fitnesse.slim.converters.BooleanConverter;
import fitnesse.slim.converters.VoidConverter;
import fitnesse.wikitext.widgets.TableRowWidget;
import fitnesse.wikitext.widgets.TextWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class ScriptTable extends SlimTable {
  private Matcher symbolAssignmentMatcher;

  public ScriptTable(Table table, String tableId, SlimTestContext context) {
    super(table, tableId, context);
  }

  public ScriptTable(Table table, String tableId) {
    super(table, tableId);
  }

  protected String getTableType() {
    return "scriptTable";
  }

  public void appendInstructions() {
    int rows = table.getRowCount();
    if (rows < 2)
      throw new SlimTable.SyntaxError("Script tables must at least one statement.");
    for (int row = 1; row < rows; row++)
      appendInstructionForRow(row);

  }

  private void appendInstructionForRow(int row) {
    String firstCell = table.getCellContents(0, row);
    if (firstCell.equalsIgnoreCase("start"))
      startActor(row);
    else if (firstCell.equalsIgnoreCase("check"))
      checkAction(row);
    else if (firstCell.equalsIgnoreCase("reject"))
      reject(row);
    else if (firstCell.equalsIgnoreCase("ensure"))
      ensure(row);
    else if (firstCell.equalsIgnoreCase("show"))
      show(row);
    else if (firstCell.equalsIgnoreCase("note"))
      note(row);
    else if (isSymbolAssignment(firstCell))
      actionAndAssign(row);
    else {
      action(row);
    }
  }

  private void actionAndAssign(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    String symbolName = symbolAssignmentMatcher.group(1);
    addExpectation(new SymbolAssignmentExpectation(symbolName, getInstructionNumber(), 0, row));    
    String actionName = getActionNameStartingAt(1, lastCol, row);
    if (!actionName.equals("")) {
      String[] args = getArgumentsStartingAt(1 + 1, lastCol, row);
      callAndAssign(symbolName, "scriptTableActor", actionName, args);
    }
  }

  private boolean isSymbolAssignment(String firstCell) {
    symbolAssignmentMatcher = symbolAssignmentPattern.matcher(firstCell);
    return symbolAssignmentMatcher.matches();
  }

  private void action(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    addExpectation(new ScriptActionExpectation(getInstructionNumber(), 0, row));
    invokeAction(0, lastCol, row);
  }

  private void note(int row) {
  }

  private void show(int row) {
    int lastCol = table.getColumnCountInRow(row) - 1;
    addExpectation(new ShowActionExpectation(getInstructionNumber(), 0, row));
    invokeAction(1, lastCol, row);
  }

  private void ensure(int row) {
    addExpectation(new EnsureActionExpectation(getInstructionNumber(), 0, row));
    int lastCol = table.getColumnCountInRow(row) - 1;
    invokeAction(1, lastCol, row);
  }

  private void reject(int row) {
    addExpectation(new RejectActionExpectation(getInstructionNumber(), 0, row));
    int lastCol = table.getColumnCountInRow(row) - 1;
    invokeAction(1, lastCol, row);
  }

  private void checkAction(int row) {
    int lastColInAction = table.getColumnCountInRow(row) - 1;
    String expected = table.getCellContents(lastColInAction, row);
    addExpectation(new ReturnedValueExpectation(expected, getInstructionNumber(), lastColInAction, row));
    invokeAction(1, lastColInAction - 1, row);
  }

  private void invokeAction(int startingCol, int endingCol, int row) {
    String actionName = getActionNameStartingAt(startingCol, endingCol, row);
    if (!actionName.equals("")) {
      String[] args = getArgumentsStartingAt(startingCol + 1, endingCol, row);
      callFunction("scriptTableActor", actionName, args);
    }
  }

  private String getActionNameStartingAt(int startingCol, int endingCol, int row) {
    StringBuffer actionName = new StringBuffer();
    actionName.append(table.getCellContents(startingCol, row));
    int secondFuncNameCol = startingCol + 2;
    for (int actionNameCol = secondFuncNameCol; actionNameCol <= endingCol; actionNameCol += 2)
      actionName.append(" ").append(table.getCellContents(actionNameCol, row));
    return actionName.toString();
  }

  private String[] getArgumentsStartingAt(int startingCol, int endingCol, int row) {
    List<String> arguments = new ArrayList<String>();
    for (int argumentColumn = startingCol; argumentColumn <= endingCol; argumentColumn += 2){
      arguments.add(table.getCellContents(argumentColumn, row));
      addExpectation(new ArgumentExpectation(getInstructionNumber(), argumentColumn, row));
    }
    return arguments.toArray(new String[arguments.size()]);
  }

  private void startActor(int row) {
    int classNameColumn = 1;
    String className = Disgracer.disgraceClassName(table.getCellContents(classNameColumn, row));
    constructInstance("scriptTableActor", className, classNameColumn, row);
  }

  protected void evaluateReturnValues(Map<String, Object> returnValues) throws Exception {
  }

  private class ScriptActionExpectation extends Expectation {
    private ScriptActionExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
      if (value.equals(VoidConverter.VOID_TAG))
        return originalValue;
      else if (value.equals(BooleanConverter.FALSE))
        return fail(originalValue);
      else if (value.equals(BooleanConverter.TRUE))
        return pass(originalValue);
      else
      return failMessage(originalValue, String.format(" returned unexpected value: [%s]", value));
    }
  }

  private class EnsureActionExpectation extends Expectation {
    public EnsureActionExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
      return value.equals(BooleanConverter.TRUE) ? pass(originalValue) : fail(originalValue);
    }
  }

  private class RejectActionExpectation extends Expectation {
    public RejectActionExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
      return value.equals(BooleanConverter.FALSE) ? pass(originalValue) : fail(originalValue);
    }
  }

  private class ShowActionExpectation extends Expectation {
    public ShowActionExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
      int lastCol = table.getColumnCountInRow(row) - 1;
      TextWidget textWidget = table.getCell(lastCol, row);
      TableRowWidget rowWidget = (TableRowWidget) textWidget.getParent().getParent();
      try {
        rowWidget.addCells(String.format("|!style_ignore(%s)", value));
      } catch (Throwable e) {
        return failMessage(value, SlimResponder.exceptionToString(e));
      }
      return originalValue;
    }
  }

  private class ArgumentExpectation extends Expectation {
    private ArgumentExpectation(int instructionNumber, int col, int row) {
      super(null, instructionNumber, col, row);
    }

    protected String createEvaluationMessage(String value, String literalizedValue, String originalValue) {
      return replaceSymbolsWithFullExpansion(originalValue);
    }
  }
}
