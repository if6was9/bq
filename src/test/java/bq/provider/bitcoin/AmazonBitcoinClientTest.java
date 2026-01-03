package bq.provider.bitcoin;

import bq.BqTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

public class AmazonBitcoinClientTest extends BqTest {

  static BitcoinClient client;
  static Boolean skip = null;

  @BeforeEach
  public void setup() {

    if (skip != null && skip == true) {
      Assumptions.assumeTrue(false);
      return;
    }
    if (client != null && skip == false) {
      return;
    }

    try {
      BitcoinClient c = AmazonBitcoinClient.create();
      JsonNode n = c.getBlockChainInfo();
      client = c;
      skip = false;
    } catch (Exception e) {

      client = null;
      skip = true;
    }
    Assumptions.assumeTrue(client != null);
  }

  @Test
  public void testIt() {

    var info = client.getBlockChainInfo();

    System.out.println(info.toPrettyString());
  }

  @Test
  public void testGenesisBlock() {

    var info = client.getBlockChainInfo();

    var blockHash = client.getBlockHash(info.path("blocks").asInt());

    System.out.println(client.getBlock(blockHash, 2).toPrettyString());
  }

  @Test
  public void testTxLookup() {
    var txid = "941fd0bd66a4e93f94dc84e8e271c2141ea420c7329fd79c4fb1c16568b29b00";

    // client = AmazonBitcoinClient.create();
    JsonNode x = client.getRawTransaction(txid);
    System.out.println(x.getNodeType());
    System.out.println(x.toPrettyString());
  }
}
