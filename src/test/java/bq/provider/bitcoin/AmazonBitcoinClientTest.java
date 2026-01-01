package bq.provider.bitcoin;

import bq.BqTest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

public class AmazonBitcoinClientTest extends BqTest {

  @Test
  public void testIt() {

    var client = AmazonBitcoinClient.create();

    var info = client.getBlockChainInfo();

    System.out.println(info.toPrettyString());
  }

  @Test
  public void testGenesisBlock() {

    var client = AmazonBitcoinClient.create();

    var info = client.getBlockChainInfo();

    var blockHash = client.getBlockHash(info.path("blocks").asInt());

    System.out.println(client.getBlock(blockHash, 2).toPrettyString());
  }

  @Test
  public void testTxLookup() {
    var txid = "941fd0bd66a4e93f94dc84e8e271c2141ea420c7329fd79c4fb1c16568b29b00";
    var client = AmazonBitcoinClient.create();

    JsonNode x = client.getRawTransaction(txid);

    System.out.println(x.toPrettyString());
  }
}
