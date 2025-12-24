package bq.ta4j;

import bx.sql.duckdb.DuckTable;
import org.ta4j.core.BarSeries;

public class BarSeriesTable {

  DuckTable table;

  public BarSeriesTable(DuckTable t) {
    this.table = t;
  }

  BarSeries getBarSeries() {
    var bars =
        table
            .sql(String.format("select * from %s order by date", table.getTableName()))
            .query(new BarRowMapper())
            .list();

    System.out.println(bars);
    return Bars.toBarSeries(bars);
  }
}
