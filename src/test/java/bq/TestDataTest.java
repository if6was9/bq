package bq;

import org.junit.jupiter.api.Test;

public class TestDataTest extends BqTest {

  @Test
  public void testIt() {

    getTestData().createBTCTable("test");
  }
}
