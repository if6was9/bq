package bq.provider;

import bq.PriceTable;
import bq.Ticker;

import bx.util.Config;

import javax.sql.DataSource;

public abstract class StorageProvider {


  private String bucket;

  private DataSource dataSource;


  public abstract PriceTable createTableFromStorage(Ticker ticker);

  public abstract PriceTable createTableFromStorage(Ticker ticker, String table);

  public abstract void writeTableToStorage(Ticker ticker, PriceTable table);


  public StorageProvider() {

    this.bucket = Config.get().get("BQ_BUCKET").orElse(null);
  }


  public StorageProvider(DataSource ds, String bucket) {
    this();
    dataSource(ds).bucket(bucket);
  }

  public final <T extends StorageProvider> T dataSource(DataSource ds) {
    this.dataSource = ds;
    return (T) this;
  }

  public final <T extends StorageProvider> T bucket(String bucket) {
    this.bucket = bucket;
    return (T) this;
  }


  public final String getBucket() {

    return bucket;
  }

  public final DataSource getDataSource() {
    return this.dataSource;
  }
}
