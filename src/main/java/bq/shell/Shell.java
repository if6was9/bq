package bq.shell;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Shell {

  private Shell() {
    // TODO Auto-generated constructor stub
  }

  // NOTE: the out-of-process entrypoint for running scripts is bqsh which invokes jshell

  public void test() {

    try {
      ScriptEngineManager manager = new ScriptEngineManager();
      ScriptEngine engine = manager.getEngineByName("jshell");

      String script =
          """
          var a =1;
          var b =2;

          """;

      // result is the return value or last variable set
      Object result = engine.eval(script);
      System.out.println("Result: " + result);

    } catch (ScriptException e) {
      e.printStackTrace();
    }
  }
}
