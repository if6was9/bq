package bq.ta4j;

import bx.sql.Results;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.ta4j.core.Bar;

public class BarRowMapper implements RowMapper<Bar> {

  public BarRowMapper() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Bar mapRow(ResultSet rs, int rowNum) throws SQLException {
    Results r = Results.create(rs);

    r.getZonedDateTime("date");

    r.getDouble("open");
    r.getDouble("high");
    r.getDouble("low");
    r.getDouble("close");
    r.getDouble("volume");

    return IndexedBar.create(
        r.getZonedDateTime("date").get().toLocalDate(),
        r.getDouble("open").orElse(null),
        r.getDouble("high").orElse(null),
        r.getDouble("low").orElse(null),
        r.getDouble("close").orElse(null),
        r.getDouble("volume").orElse(null),
        null);
  }
}
