package bq;

import bx.sql.duckdb.DuckTable;
import bx.util.Slogger;
import java.io.File;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;

public class TestData {

  static Logger logger = Slogger.forEnclosingClass();
  private DataSource dataSource;

  public TestData(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DataManager getDataManager() {
    return new DataManager().dataSource(getDataSource());
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public JdbcClient getJdbcClient() {
    return JdbcClient.create(getDataSource());
  }

  public PriceTable loadBtcPriceTable(String table) {
    return PriceTable.from(loadBtcPriceData(table));
  }

  public PriceTable loadWGMIPriceTable(String table) {
    File csv = new File("./src/test/resources/WGMI.csv");

    DuckTable t = new DataManager().dataSource(getDataSource()).createOHLCV(table, true);
    String sql =
        String.format(
            "insert into %s (date, open, high,low,close,volume) (select"
                + " date,open,high,low,close,volume from '%s' order by date)",
            table, csv.toString());

    int count = getJdbcClient().sql(sql).update();

    logger.atDebug().log("inserted {} rows", count);

    return PriceTable.from(t);
  }

  public DuckTable loadBtcPriceData(String table) {

    DuckTable t = new DataManager().dataSource(getDataSource()).createOHLCV(table, true);
    String sql =
        String.format(
            "insert into %s (date, open, high,low,close,volume) (select"
                + " date,open,high,low,close,volume from '%s' order by date)",
            table, new File("src/test/resources/btc-price-data.csv").toString());

    int count = getJdbcClient().sql(sql).update();

    logger.atDebug().log("inserted {} rows", count);

    return t;
  }
}
