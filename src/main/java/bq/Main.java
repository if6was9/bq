package bq;

import bx.util.Slogger;
import java.util.Map;
import org.slf4j.Logger;

public class Main {

  static Logger logger = Slogger.forEnclosingClass();

  static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>();

  public static void main(String[] args) throws Exception {}
}
