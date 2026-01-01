package bq.provider;

import bq.BqTest;
import org.junit.jupiter.api.Test;

public class AmazonBitcoinClientTest extends BqTest {

  @Test
  public void testIt() {

    var client = AmazonBitcoinClient.create();

    var info = client.getBlockChainInfo();

    System.out.println(info.toPrettyString());
  }
}
