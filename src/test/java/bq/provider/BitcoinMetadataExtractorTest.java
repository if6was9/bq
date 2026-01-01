package bq.provider;

import bq.BqTest;
import bx.sql.duckdb.DuckDataSource;
import org.junit.jupiter.api.Test;

public class BitcoinMetadataExtractorTest extends BqTest {

  @Test
  public void testIt() {

    BitcoinClient client = AmazonBitcoinClient.create();

    var info = client.getBlockChainInfo();

    final int lastBlock = info.path("blocks").asInt() - 1;

    BitcoinMetadataExtractor bme =
        new BitcoinMetadataExtractor()
            .client(client)
            .dataSource(DuckDataSource.create("jdbc:duckdb:./blockchain.duckdb"))
            .table("block");

    int count = 0;
    bme.processBlock(lastBlock);

    while (bme.hasPrev() && count++ < 5) {
      bme.processPrev();
    }

    bme.table.show();
  }
}
