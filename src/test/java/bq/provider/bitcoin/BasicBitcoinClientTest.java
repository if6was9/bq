package bq.provider.bitcoin;

import bq.BqTest;
import org.junit.jupiter.api.Test;

public class BasicBitcoinClientTest extends BqTest {

  @Test
  public void testIt() {

    BitcoinClient c = BasicBitcoinClient.create();

    System.out.println(c.getBlockChainInfo().toPrettyString());
  }
}
