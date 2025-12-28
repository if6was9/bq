package bq.util;

import bq.BqTest;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ScriptTest extends BqTest {

  @Test
  public void testIt() {

    try {
      ScriptEngineManager manager = new ScriptEngineManager();
      ScriptEngine engine = manager.getEngineByName("java");

      String script =
          """
            public class Script {
                public String getMessage() {
                	return "Hello World";
                }
            }

          """;
      Object result = engine.eval(script);
      System.out.println("Result: " + result);
    } catch (ScriptException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testCompile() {

    try {
      ScriptEngineManager manager = new ScriptEngineManager();
      ScriptEngine engine = manager.getEngineByName("java");

      Compilable compiler = (Compilable) engine;

      String script =
          """
          package foo;
              public class Script {


          public String abc =  "abc";
                  public String foo() {
                  System.out.println(getClass());
                  abc="def";
                  	return "Hello World";
                  }
              }

          """;

      Bindings bindings = engine.createBindings();
      bindings.put("abc", "Hello world");

      CompiledScript compiledScript = compiler.compile(script);
      Object result = compiledScript.eval(bindings);

      Assertions.assertThat(bindings.get("abc")).isEqualTo("def");

      System.out.println("Result: " + result);
    } catch (ScriptException e) {
      e.printStackTrace();
    }
  }
}
