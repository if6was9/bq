package bq.chart;

import bx.util.Json;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class JSUtilTest {

  @Test
  public void testIt() {

    ObjectNode n = Json.createObjectNode();

    n.put("name", "Homer");
    n.put("age", 7);

    ArrayNode arr = Json.createArrayNode();

    n.set("arr", arr);

    arr.add("bark");
    arr.add("bark");

    n.set("activity", arr);

    String expected =
        """
{
    name: "Homer",
    age: 7,
    arr: [ "bark", "bark" ],
    activity: [ "bark", "bark" ],
}\
""";
    Assertions.assertThat(JSUtil.toObject(n)).isEqualTo(expected);
  }
}
