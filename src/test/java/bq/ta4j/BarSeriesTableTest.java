package bq.ta4j;

import bq.BqTest;
import bx.sql.duckdb.DuckTable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class BarSeriesTableTest extends BqTest {

  @Test
  public void tesIt() {

    var testData = getTestData();

    Assertions.assertThat(testData.getDataSource()).isNotNull();

    DuckTable dt = testData.createBTCTable("btc");

    dt.show();
  }
}
