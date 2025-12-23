package bq.ta4j;

import bq.PriceTable;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.util.Map;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

public class DuckColumnIndicator implements Indicator<Num> {

  PriceTable priceTable;
  String column;

  Map<Long, Num> cachedValues = null;

  public DuckColumnIndicator(PriceTable bs, String column) {
    this.priceTable = bs;
    this.column = column;
  }

  @Override
  public Num getValue(int index) {

    if (cachedValues == null) {
      fetchValues();
    }
    // 1. look up the bar in the driving BarSeries
    // 2. this will have the DuckDB rowId, which we'll use to get the data
    IndexedBar bar = (IndexedBar) priceTable.getBarSeries().getBar(index);
    if (bar == null) {
      return null;
    }

    Long rowId = bar.getId();
    Num num = cachedValues.get(rowId);
    return num;
  }

  private void fetchValues() {
    // IMPORTANT - The TA4J Indicator object model uses the bar index to get the indicator
    // value.
    // We need to load the appropriate column into memory and store the DuckDB rowid->Num
    // mappings.
    // This can then be used in a two-stage lookup operation in getValue().

    Map<Long, Num> rowIdMap = Maps.newHashMap();

    String sql =
        String.format(
            "select rowid, %s from %s", column, this.priceTable.getDuckTable().getTableName());

    this.priceTable
        .getDuckTable()
        .sql(sql)
        .query(
            rch -> {
              Long rowId = rch.getLong("rowid");
              if (rch.wasNull()) {
                rowId = null;
              }
              Double d = rch.getDouble(2);
              if (!rch.wasNull()) {
                rowIdMap.put(rowId, DoubleNum.valueOf(d));
              }
            });

    this.cachedValues = rowIdMap;
  }

  @Override
  public int getCountOfUnstableBars() {
    return 0;
  }

  @Override
  public BarSeries getBarSeries() {
    return priceTable.getBarSeries();
  }

  public String toString() {
    return MoreObjects.toStringHelper("ColumnIndicator").add("column", this.column).toString();
  }
}
