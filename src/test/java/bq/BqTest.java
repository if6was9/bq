package bq;

import bx.sql.PrettyQuery;
import bx.sql.duckdb.DuckDataSource;
import bx.sql.duckdb.DuckTable;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Preconditions;
import java.io.File;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.jdbc.core.simple.JdbcClient;

public abstract class BqTest {

  static Logger logger = Slogger.forEnclosingClass();

  private DuckDataSource dataSource;
  DataManager ddm;

  public DataManager getDuckDataManager() {
    return ddm;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public JdbcClient getJdbcClient() {
    return JdbcClient.create(getDataSource());
  }

  public PrettyQuery prettyQuery() {
    return PrettyQuery.with(getDataSource()).out(logger, Level.INFO);
  }

  public DuckTable loadBtcPriceData(String table) {

    getDuckDataManager().createOHLCV(table, true);
    String sql =
        String.format(
            "insert into %s (date, open, high,low,close,volume) (select"
                + " date,open,high,low,close,volume from '%s' order by date)",
            table, new File("src/test/resources/btc-price-data.csv").toString());

    int count = getJdbcClient().sql(sql).update();

    logger.atDebug().log("inserted {} rows", count);

    return DuckTable.of(getDataSource(), table);
  }

  @BeforeEach
  public void setup() {

    String name = System.getProperty("app.name");
    if (!S.notBlank(name).orElse("").equals("bq")) {
      System.setProperty("app.name", "bq");
      name = System.getProperty("app.name");
      Preconditions.checkState(S.notBlank(name).orElse("").equals("bq"));
    }
    logger.atTrace().log("setup");
    dataSource = DuckDataSource.createInMemory();

    ddm = new DataManager();
    ddm.dataSource(dataSource);
  }

  @AfterEach
  public void tearDown() {
    logger.atTrace().log("closing: " + dataSource);
    dataSource.close();
  }
}
