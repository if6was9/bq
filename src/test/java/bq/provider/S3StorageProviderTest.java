package bq.provider;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import bq.BqTest;
import bq.PriceTable;
import bq.Ticker;
import bx.util.Slogger;


public class S3StorageProviderTest extends BqTest {

  Logger logger = Slogger.forEnclosingClass();


  @Test
  public void testX() {

    var s3 = new S3StorageProvider().bucket(getTestS3BucketName()).dataSource(getDataSource());

    Ticker ticker = Ticker.of("GOOG");

    PriceTable t = s3.createTableFromStorage(ticker);

    PriceTable newData = DataProviders.newRequest(ticker).fetchIntoTable();

    t.insertMissing(newData);
    t.show();

    //	s3.writeTableToStorage(ticker, t);

  }


  @Test
  public void testY() {

    String bucket = getTestS3BucketName();
    logger.atInfo().log("bucket: {}", bucket);
    S3StorageProvider s3 = new S3StorageProvider().dataSource(getDataSource()).bucket(bucket);

    s3.listStocks()
        .forEach(
            it -> {
              //	  System.out.println(getTicker(it));
            });
    s3.listCrypto()
        .forEach(
            it -> {
              System.out.println(it.getTicker());
              //	  System.out.println(getTicker(it));
              //	  System.out.println(getTimeSinceLastModified(it).get(ChronoUnit.SECONDS)/24);
            });
  }

}
