package fitnesse.slim.test;

import java.util.List;

public class TestSlim {
  private boolean niladWasCalled = false;
  private String stringArg;
  private int intArg;
  private double doubleArg;
  private Integer integerObjectArg;
  private Double doubleObjectArg;
  private char charArg;
  private List<Object> listArg;
  private int constructorArg;
  private String[] stringArray;
  private Integer[] integerArray;
  private Boolean[] booleanArray;
  private Double[] doubleArray;

  public TestSlim() {

  }

  public TestSlim(int constructorArg) {
    this.constructorArg = constructorArg;
  }

  public void nilad() {
    niladWasCalled = true;
  }

  public int returnConstructorArg() {
    return constructorArg;
  }

  public void voidFunction() {

  }

  public boolean niladWasCalled() {
    return niladWasCalled;
  }

  public String returnString() {
    return "string";
  }

  public int returnInt() {
    return 7;
  }

  public void setString(String arg) {
    stringArg = arg;
  }

  public void oneString(String arg) {
    stringArg = arg;
  }

  public void oneList(List<Object> l) {
    listArg = l;
  }

  public List<Object> getListArg() {
    return listArg;
  }

  public String getStringArg() {
    return stringArg;
  }

  public void oneInt(int arg) {
    intArg = arg;
  }

  public int getIntArg() {
    return intArg;
  }

  public void oneDouble(double arg) {
    doubleArg = arg;
  }

  public double getDoubleArg() {
    return doubleArg;
  }

  public void manyArgs(Integer i, Double d, char c) {
    integerObjectArg = i;
    doubleObjectArg = d;
    charArg = c;
  }

  public Integer getIntegerObjectArg() {
    return integerObjectArg;
  }

  public double getDoubleObjectArg() {
    return doubleObjectArg;
  }

  public char getCharArg() {
    return charArg;
  }

  public int addTo(int a, int b) {
    return a + b;
  }

  public int echoInt(int i) {
    return i;
  }

  public String echoString(String s) {
    return s;
  }

  public List<Object> echoList(List<Object> l) {
    return l;
  }

  public boolean echoBoolean(boolean b) {
    return b;
  }

  public double echoDouble(double d) {
    return d;
  }

  public void execute() {

  }

  public void die() {
    throw new Error("blah");
  }

  public void setNoSuchConverter(NoSuchConverter x) {

  }

  public NoSuchConverter noSuchConverter() {
    return new NoSuchConverter();
  }

  public void setStringArray(String array[]) {
    stringArray = array;
  }

  public String[] getStringArray() {
    return stringArray;
  }

  public void setIntegerArray(Integer array[]) {
    integerArray = array;
  }

  public Integer[] getIntegerArray() {
    return integerArray;
  }

  public Boolean[] getBooleanArray() {
    return booleanArray;
  }

  public void setBooleanArray(Boolean[] booleanArray) {
    this.booleanArray = booleanArray;
  }

  public Double[] getDoubleArray() {
    return doubleArray;
  }

  public void setDoubleArray(Double[] doubleArray) {
    this.doubleArray = doubleArray;
  }

  public String nullString() {
    return null;
  }

  class NoSuchConverter {
  }

  ;
}
