package bq;

import bq.provider.DataProviders;
import org.junit.jupiter.api.Test;

public class TestDataTest extends BqTest {

  @Test
  public void testIt() {

    getTestData().createBTCTable("test");
  }

  @Test
  public void testX() {
    DataProviders.forSymbol("wgmi");
  }
}
