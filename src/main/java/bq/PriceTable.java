package bq;

import bq.ta4j.Bars;
import bx.sql.Results;
import bx.sql.duckdb.DuckTable;
import bx.util.Dates;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.springframework.jdbc.core.RowMapper;
import org.ta4j.core.BarSeries;

public class PriceTable {

  DuckTable table;

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

  public BarSeries loadBarSeries() {

    String sql =
        String.format("select rowid, * from %s order by date asc", getDuckTable().getTableName());

    var rows = table.select(sql).query(new OHLCVRowMapper()).list();

    return Bars.toBarSeries(rows.stream());
  }
}
