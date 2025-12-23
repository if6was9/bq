package bq;

import bq.ta4j.BarSeriesIterator;
import bq.ta4j.Bars;
import bq.ta4j.DuckColumnIndicator;
import bq.ta4j.IndexedBar;
import bq.ta4j.IndicatorBuilder;
import bx.sql.Results;
import bx.sql.duckdb.DuckTable;
import bx.util.Dates;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.springframework.jdbc.core.RowMapper;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

public class PriceTable {

  DuckTable table;

  AtomicReference<BarSeries> barSeries = new AtomicReference<BarSeries>();

  static class OHLCVRowMapper implements RowMapper<OHLCV> {

    @Override
    public OHLCV mapRow(ResultSet rs, int rowNum) throws SQLException {

      LocalDate date = Dates.asLocalDate(rs.getString("date")).orElse(null);
      Results r = Results.create(rs);

      OHLCV ohlcv =
          BasicOHLCV.of(
              date,
              r.getDouble("open").orElse(null),
              r.getDouble("high").orElse(null),
              r.getDouble("low").orElse(null),
              r.getDouble("close").orElse(null),
              r.getDouble("volume").orElse(null),
              r.getLong("rowid").orElse(null));

      return ohlcv;
    }
  }

  PriceTable() {
    super();
  }

  public static PriceTable from(DuckTable t) {
    PriceTable pt = new PriceTable();
    pt.table = t;
    return pt;
  }

  public DuckTable getDuckTable() {
    return this.table;
  }

  public BarSeries getBarSeries() {
    BarSeries bs = barSeries.get();
    if (bs != null) {
      return bs;
    }
    bs = loadBarSeries();
    this.barSeries.set(bs);

    return bs;
  }

  private BarSeries loadBarSeries() {

    String sql =
        String.format("select rowid, * from %s order by date asc", getDuckTable().getTableName());

    var rows = table.select(sql).query(new OHLCVRowMapper()).list();

    return Bars.toBarSeries(rows.stream());
  }

  IndicatorBuilder indicatorBuilder = new IndicatorBuilder();

  public void addIndicator(String col, Indicator<Num> ind) {
    BarSeriesIterator t = Bars.toIterator(ind.getBarSeries());

    table.addColumn(col + " double");
    while (t.hasNext()) {
      IndexedBar b = (IndexedBar) t.next();
      Num num = null;

      try {
        num = ind.getValue(t.getBarIndex());
      } catch (NullPointerException ignore) {
        num = null;
      }

      if (num == null || Double.isNaN(num.doubleValue())) {
        // Is recording NaN as NULL the right thing to do?
        table.update(b.getId(), col, null);
      } else {

        table.update(b.getId(), col, num.doubleValue());
      }
    }
  }

  public void addIndicator(String col, String expression) {
    addIndicator(
        col,
        bs -> {
          return indicatorBuilder.create(expression, bs);
        });
  }

  public Indicator<Num> getColumnIndicator(String col) {

    return new DuckColumnIndicator(this, col);
  }

  public void addIndicator(String col, Function<BarSeries, Indicator> fn) {
    BarSeries bs = loadBarSeries();
    Indicator<Num> ind = fn.apply(bs);

    addIndicator(col, ind);
  }
}
