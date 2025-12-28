package bq.provider;

import bq.BqTest;
import bq.PriceTable;
import bq.Ticker;
import org.junit.jupiter.api.Test;

public class S3StorageProviderTest extends BqTest {

  @Test
  public void testX() {

    var s3 = new S3StorageProvider().bucket("data.bitquant.cloud").dataSource(getDataSource());

    Ticker ticker = Ticker.of("GOOG");

    PriceTable t = s3.createTableFromStorage(ticker);

    PriceTable newData = DataProviders.newRequest(ticker).fetchIntoTable();

    t.insertMissing(newData);
    t.show();

    //	s3.writeTableToStorage(ticker, t);

  }
}
