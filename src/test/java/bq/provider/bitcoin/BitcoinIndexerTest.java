package bq.provider.bitcoin;

import bq.BqTest;
import bx.util.Slogger;
import java.util.Optional;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;

public class BitcoinIndexerTest extends BqTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testIt() {

    BitcoinClient client = AmazonBitcoinClient.create();

    var info = client.getBlockChainInfo();

    final int lastBlock = info.path("blocks").asInt() - 1;

    BitcoinIndexer bi =
        new BitcoinIndexer().client(client).dataSource(getDataSource()).createTables();

    String hash = bi.getClient().getBlockHash(900000);

    bi.processBlock(hash);

    bi.getBlockTable().show();

    bi.getTxTable().show();

    Optional<String> next = bi.getNextBlockHash(hash);

    System.out.println(next);

    bi.getTxTable().prettyQuery().select("select * from tx where coinbase is not null");
  }

  @Test
  public void testFirstBlocks() {

    DataSource ds = getDataSource();
    BitcoinClient client = AmazonBitcoinClient.create();

    BitcoinIndexer bi = new BitcoinIndexer().client(client).dataSource(ds).createTables();

    for (int i = 0; i < 5; i++) {

      Assertions.assertThat(bi.getBlockHash(i)).isPresent();
      if (bi.hasBlockInDb(i)) {
        logger.atInfo().log("already have block: {}", i);

      } else {
        bi.processBlock(i);
      }
    }

    bi.getBlockTable().show();
    bi.getTxTable().show();
    bi.getTxInputTable().show();
  }

  @Test
  public void testRecentBlocks() {

    DataSource ds = getDataSource();
    BitcoinClient client = AmazonBitcoinClient.create();

    int startBlock = 900000;
    int endBlock = startBlock + 2;

    BitcoinIndexer bi = new BitcoinIndexer().client(client).dataSource(ds).createTables();

    for (int i = startBlock; i < endBlock; i++) {

      if (bi.hasBlockInDb(i)) {
        logger.atInfo().log("already have block: {}", i);

      } else {
        bi.processBlock(i);
      }
    }

    bi.getBlockTable().show();
    bi.getTxTable().show();
    bi.getTxInputTable().show();
  }

  @Test
  public void showLatest() {

    BitcoinClient client = AmazonBitcoinClient.create();

    JsonNode info = client.getBlockChainInfo();

    String hash = client.getBlockHash(info.path("blocks").asInt() - 1);

    JsonNode block = client.getBlock(hash, 2);

    System.out.println(block.toPrettyString());
  }

  @Test
  public void testLatestBlock() {

    BitcoinClient client = AmazonBitcoinClient.create();

    JsonNode info = client.getBlockChainInfo();
    int blockCount = info.path("blocks").asInt();
    BitcoinIndexer bi =
        new BitcoinIndexer().client(client).dataSource(getDataSource()).createTables();

    bi.processBlock(blockCount);

    bi.getBlockTable().show();
    bi.getTxTable().show();
    bi.getTxInputTable().show();
  }
}
