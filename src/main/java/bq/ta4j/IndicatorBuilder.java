package bq.ta4j;

import bx.util.BxException;
import bx.util.Json;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class IndicatorBuilder {

  static org.slf4j.Logger logger = Slogger.forEnclosingClass();

  String input;

  public static String toShortName(ClassInfo ci) {
    String simpleName = ci.getSimpleName();

    String shortName = simpleName;
    if (simpleName.endsWith("Indicator")) {
      shortName = shortName.substring(0, shortName.length() - "Indicator".length());
    }

    shortName = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(shortName);

    shortName = collapse(shortName);

    return shortName;
  }

  private static String collapse(String input) {

    List<String> collapsed = com.google.common.collect.Lists.newArrayList();
    String combined = "";
    for (String p : Splitter.on("_").splitToList(input)) {

      if (p.length() > 1) {
        if (S.isNotBlank(combined)) {
          collapsed.add(combined);
          collapsed.add(p);
          combined = "";
        } else {
          collapsed.add(p);
        }
      } else if (p.length() == 1) {
        combined += p;
      }
    }
    if (S.isNotBlank(combined)) {
      collapsed.add(combined);
    }
    return Joiner.on("_").join(collapsed);
  }

  private static Supplier<Map<String, Class<Indicator<?>>>> mapSupplier =
      Suppliers.memoize(IndicatorBuilder::buildMap);

  protected Map<String, Class<Indicator<?>>> getIndicatorNameMap() {
    return mapSupplier.get();
  }

  private static Map<String, Class<Indicator<?>>> buildMap() {

    Map<String, Class<Indicator<?>>> map = Maps.newHashMap();

    try (ScanResult scanResult =
        new ClassGraph()
            .enableClassInfo()
            .enableMethodInfo()
            .acceptPackages("org.ta4j", "bq.indicator", "bq.indicator.bq")
            .scan()) { // Start the scan
      for (ClassInfo classInfo : scanResult.getClassesImplementing(Indicator.class)) {

        try {
          String className = classInfo.getName();

          String shortName = toShortName(classInfo);
          map.put(shortName, (Class<Indicator<?>>) Class.forName(className));

        } catch (ClassNotFoundException e) {
          logger.atWarn().setCause(e).log();
        }
      }
    }

    return map;
  }

  List<String> tokenize(String s) {

    if (s == null) {
      return List.of();
    }
    List<String> result = Lists.newArrayList();

    String part = "";
    for (char c : s.toCharArray()) {
      if (c == '(' || c == ')' || c == ',') {
        if (part.length() > 0) {
          result.add(part);

          part = "";
        }
        result.add(Character.toString(c));
      } else {
        part += Character.toString(c);
      }
    }

    if (S.isNotBlank(part)) {
      result.add(part);
    }
    return Lists.newArrayList(
        result.stream().map(it -> it.trim()).filter(it -> S.isNotBlank(it)).toList());
  }

  enum TokenType {
    IDENTIFIER,
    INTEGER,
    DOUBLE,
    OPEN_PAREN,
    CLOSE_PAREN,
    COMMA,
    WHITESPACE
  }

  TokenType getTokenType(String token) {

    if (S.isBlank(token)) {
      return TokenType.WHITESPACE;
    }
    if (token.equals("(")) {
      return TokenType.OPEN_PAREN;
    } else if (token.equals(")")) {
      return TokenType.CLOSE_PAREN;
    } else if (token.equals(",")) {
      return TokenType.COMMA;
    }
    try {
      Integer.parseInt(token);
      return TokenType.INTEGER;
    } catch (Exception e) {
    }

    try {
      Double.parseDouble(token);
      return TokenType.DOUBLE;
    } catch (Exception e) {
    }

    if (token.chars().anyMatch(c -> Character.isAlphabetic(c))) {
      return TokenType.IDENTIFIER;
    }
    throw new BxException("unparsable token: " + token);
  }

  protected JsonNode parseTree(String s) {

    List<String> tokens = tokenize(s);
    return parse(tokens);
  }

  protected ArrayNode parse(final List<String> tokens) {
    ArrayNode tree = Json.createArrayNode();

    while (!tokens.isEmpty()) {
      TokenType tokenType = getTokenType(tokens.getFirst());

      if (tokenType == TokenType.IDENTIFIER) {
        String token = tokens.removeFirst();

        ObjectNode fnNode = Json.createObjectNode();
        fnNode.put("fn", token);
        tree.add(fnNode);
        if (!tokens.isEmpty()) {
          if (tokens.getFirst().equals("(")) {
            JsonNode argTree = parse(tokens);
            fnNode.set("args", argTree);
          }
        }
      } else if (getTokenType(tokens.getFirst()) == TokenType.OPEN_PAREN) {
        List<String> listTokens = Lists.newArrayList();
        int level = 1;
        tokens.removeFirst();
        while (level > 0 && !tokens.isEmpty()) {
          String next = tokens.removeFirst();
          if (next.equals("(")) {
            level++;
          } else if (next.equals(")")) {
            level--;
          } else {
            listTokens.add(next);
          }
        }

        ArrayNode an = parse(listTokens);

        for (JsonNode n : an) {
          tree.add(n);
        }

      } else if (getTokenType(tokens.getFirst()) == TokenType.INTEGER) {
        tree.add(Long.valueOf(tokens.removeFirst()));
      } else if (getTokenType(tokens.getFirst()) == TokenType.DOUBLE) {
        tree.add(Double.valueOf(tokens.removeFirst()));
      } else if (getTokenType(tokens.getFirst()) == TokenType.COMMA) {
        tokens.removeFirst();
      } else if (getTokenType(tokens.getFirst()) == TokenType.WHITESPACE) {
        tokens.removeFirst();
      } else {
        throw new IllegalArgumentException("unknown token: " + tokens.getFirst());
      }
    }
    return tree;
  }

  private List<Object> buildArgs(JsonNode functionNode, BarSeries series) {
    if (functionNode.path("args").isEmpty()) {
      return List.of();
    }
    List<Object> vals = Lists.newArrayList();

    functionNode
        .path("args")
        .forEach(
            arg -> {
              if (arg.isLong()) {
                vals.add(arg.asLong());
              } else if (arg.isDouble()) {
                vals.add(arg.asDouble());
              } else if (arg.isIntegralNumber()) {
                vals.add(arg.asInt());
              } else if (arg.isObject() && arg.has("fn")) {
                Indicator ind = create(arg, series);
                vals.add(ind);
              } else {
                throw new IllegalArgumentException(
                    "unsupported arg: " + arg + " " + arg.getNodeType());
              }
            });

    return vals;
  }

  public Indicator<Num> create(String expression, BarSeries series) {
    return create(parseTree(expression), series);
  }

  protected Indicator<Num> create(JsonNode input, BarSeries series) {

    Preconditions.checkArgument(input != null);

    if (input.isObject()) {
      ArrayNode wrapper = Json.createArrayNode();
      wrapper.add(input);
      input = wrapper;
    }
    Preconditions.checkArgument(input.size() > 0);

    JsonNode v = input.get(0);

    if (v.isNumber()) {
      ConstantIndicator<Num> indicator =
          new ConstantIndicator<Num>(series, DoubleNum.valueOf(v.asDouble()));
      return indicator;
    }

    String shortName = v.path("fn").asString();

    Map<String, Class<Indicator<?>>> m = getIndicatorNameMap();

    Class<Indicator<?>> clazz = m.get(shortName);

    if (clazz == null) {
      throw new BxException("unknown indicator: " + shortName);
    }

    List args = buildArgs(v, series);

    return invokeBestConstructor(clazz, args, series);
  }

  private Indicator<Num> invokeBestConstructor(
      Class<Indicator<?>> clazz, List<Object> args, BarSeries bs) {

    var ctors = List.of(clazz.getDeclaredConstructors());
    ctors =
        List.of(clazz.getDeclaredConstructors()).stream()
            .filter(
                ctor -> {
                  int arity = ctor.getParameterCount();

                  return (arity == args.size());

                  // return true;
                })
            .toList();

    for (Constructor ctor : ctors) {
      Indicator<Num> indicator = tryInvoke(ctor, args, bs);
      if (indicator != null) {
        return indicator;
      }
    }

    if (args.size() > 0 && (args.get(0) instanceof Indicator)) {
      // the user selected an indicator as the first arg.  Implicitly adding the close price doesn't
      // make sense
      throw new UnsupportedOperationException("unsupported: " + clazz);
    }

    List<Object> implicitArgs = Lists.newArrayList();
    implicitArgs.clear();
    implicitArgs.add(new ClosePriceIndicator(bs));
    implicitArgs.addAll(args);

    ctors =
        List.of(clazz.getDeclaredConstructors()).stream()
            .filter(
                ctor -> {
                  int arity = ctor.getParameterCount();
                  if (arity == implicitArgs.size()
                      && Indicator.class.isAssignableFrom(ctor.getParameterTypes()[0])) {
                    return true;
                  }
                  return false;
                })
            .toList();

    for (Constructor ctor : ctors) {
      Indicator<Num> indicator = tryInvoke(ctor, implicitArgs, bs);
      if (indicator != null) {
        return indicator;
      }
    }

    ////////
    ///
    ///

    implicitArgs.clear();
    implicitArgs.add(bs);
    implicitArgs.addAll(args);

    ctors =
        List.of(clazz.getDeclaredConstructors()).stream()
            .filter(
                ctor -> {
                  int arity = ctor.getParameterCount();
                  if (arity == implicitArgs.size()
                      && BarSeries.class.isAssignableFrom(ctor.getParameterTypes()[0])) {
                    return true;
                  }
                  return false;
                })
            .toList();

    for (Constructor ctor : ctors) {
      Indicator<Num> indicator = tryInvoke(ctor, implicitArgs, bs);
      if (indicator != null) {
        return indicator;
      }
    }

    throw new UnsupportedOperationException("unsupported: " + clazz);
  }

  private Object convertType(Object from, Class to) {

    if (from == null) {
      return from;
    } else if (to.isAssignableFrom(Num.class)) {
      return DoubleNum.valueOf(from.toString());
    } else if (to.equals(int.class) || to.equals(Integer.class)) {

      if (from instanceof Long) {
        Long v = (Long) from;

        return v.intValue();
      }
    }

    if (Num.class.isAssignableFrom(to)) {
      return DoubleNum.valueOf(10);
    }
    return from;
  }

  Indicator<Num> tryInvoke(Constructor ctor, List<Object> args, BarSeries bs) {
    try {
      if (ctor.getParameterCount() != args.size()) {
        return null;
      }

      List<Object> castedArgs = Lists.newArrayList();
      for (int i = 0; i < ctor.getParameterCount(); i++) {
        Parameter p = ctor.getParameters()[i];

        Object castedArg = convertType(args.get(i), p.getType());
        castedArgs.add(castedArg);
      }

      Object[] arr = castedArgs.toArray(new Object[0]);
      return (Indicator<Num>) ctor.newInstance(arr);
    } catch (IllegalArgumentException
        | InstantiationException
        | InvocationTargetException
        | IllegalAccessException e) {
      throw new BxException(e);
    }
  }
}
