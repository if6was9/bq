package bq;

import bq.provider.CoinbaseDataProvider;
import bx.sql.duckdb.DuckTable;
import bx.util.Slogger;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class DataManagerTest extends BqTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testIt() {
    getDuckDataManager().createOHLCV("test", false);

    DuckTable.of(getDataSource(), "test").prettyQuery().select();

    var ohlcv = BasicOHLCV.of(LocalDate.of(2025, 11, 11), 10.0, 11.0, 6.0, 9.0, 100.0);
    getDuckDataManager().insert("test", List.of(ohlcv));

    DuckTable.of(getDataSource(), "test").prettyQuery().select();
  }

  @Test
  public void testIt2() {

    var table = getTestData().getDataManager().createOHLCV("btc", true);

    var list =
        new CoinbaseDataProvider()
            .newRequest("btc")
            .from(LocalDate.of(2025, 1, 28))
            .fetchStream()
            .toList();

    logger.atInfo().log("Adding {}", list.size());

    getDuckDataManager().insert(table.getTableName(), list);
  }

  @Test
  public void testX() {
    var dm = new DataManager().dataSource(getDataSource());

    var table = dm.createOHLCV("test", true);

    var c1 = BasicOHLCV.of(LocalDate.of(2025, 12, 1), 1.0, 1.0, 1.0, 1.0, 1.0);
    var c2 = BasicOHLCV.of(LocalDate.of(2025, 12, 2), 1.0, 1.0, 1.0, 1.0, 1.0);
    var c3 = BasicOHLCV.of(LocalDate.of(2025, 12, 3), 1.0, 1.0, 1.0, 1.0, 1.0);

    dm.insert(table.getTableName(), List.of(c1, c2));

    Assertions.assertThat(table.rowCount()).isEqualTo(2);
    dm.insertMissing(table.getTableName(), List.of(c1, c2, c3));

    Assertions.assertThat(table.rowCount()).isEqualTo(3);

    table.prettyQuery().select();
  }

  @Test
  public void testInlineParse() {
    String csv =
        """
        a,b,c
        1,2,3


        4,5,6
        7,8,9
        """;

    var t = DuckTable.of(getDataSource(), "test").csv().fromString(csv).load();

    t.show();
  }
}
