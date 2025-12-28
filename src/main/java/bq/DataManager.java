package bq;

import bx.sql.DbException;
import bx.sql.Results;
import bx.sql.duckdb.DuckTable;
import bx.util.BxException;
import bx.util.Slogger;
import bx.util.Zones;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DataManager {

  static Logger logger = Slogger.forEnclosingClass();
  DataSource dataSource;

  public DataManager dataSource(DataSource ds) {
    this.dataSource = ds;
    return this;
  }

  public JdbcClient getJdbcClient() {

    return JdbcClient.create(this.dataSource);
  }

  public PriceTable createOHLCV(String table, boolean withConstraint) {
    String sql =
        """
CREATE TABLE %s (
    		date date not null,
    		open double null,
    		high double null,
    		low double null,
    		close double null,
    		volume double null)
""";

    sql = String.format(sql, table);

    getJdbcClient().sql(sql).update();

    if (withConstraint) {
      addOHLCVPrimaryKey(table);
    }

    return new PriceTable(getDataSource(), table);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void addOHLCVPrimaryKey(String table) {
    String sql = String.format("ALTER TABLE %s ADD PRIMARY KEY(%s)", table, "date");

    getJdbcClient().sql(sql).update();
  }

  static class OHLCVRowMapper implements RowMapper<OHLCV> {

    @Override
    public OHLCV mapRow(ResultSet rs, int rowNum) throws SQLException {

      Results r = Results.create(rs);

      LocalDate date = r.getLocalDate("date").get();
      r.getDouble("open");
      r.getDouble("high");
      r.getDouble("low");
      r.getDouble("close");
      r.getDouble("volume");

      var ohlcv =
          BasicOHLCV.ofDecimal(
              date.atStartOfDay(Zones.UTC).toInstant(),
              r.getBigDecimal("open").orElse(null),
              r.getBigDecimal("high").orElse(null),
              r.getBigDecimal("low").orElse(null),
              r.getBigDecimal("close").orElse(null),
              r.getBigDecimal("volume").orElse(null));

      return ohlcv;
    }
  }

  public List<OHLCV> selectAll(String table) {

    return getJdbcClient()
        .sql(
            String.format(
                "select date,open,high,low,close,volume from %s order by date asc", table))
        .query(new OHLCVRowMapper())
        .list();
  }

  String getColumnType(String table, String column) {

    String sql =
        String.format("select column_type from (describe %s) where column_name=:col", table);
    return (String) getJdbcClient().sql(sql).param("col", column).query().singleValue();
  }

  public int insertMissing(String table, Iterable<OHLCV> data) {

    String tempTableName = String.format("temp_%s", System.currentTimeMillis());
    DuckTable tempTable = createOHLCV(tempTableName, false);
    try {
      insert(tempTable.getTableName(), data);
      String sql =
          String.format(
              "insert into %s (select * from %s where date not in (select date from %s))",
              table, tempTableName, table);
      return getJdbcClient().sql(sql).update();

    } finally {
      tempTable.drop();
    }
  }

  public void insert(String table, Iterable<OHLCV> data) {

    DuckTable t = DuckTable.of(dataSource, table);

    // appender seems to be broken for "DATE" types
    String type = getColumnType(table, "date");

    if ("DATE".equalsIgnoreCase(type)) {

      try {
        var c = t.getDataSource().getConnection();

        String sql =
            "insert into " + table + " (date,open,high,low,close,volume) values (?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
          data.forEach(
              it -> {
                try {

                  ps.setString(1, it.getDate().toString()); // do not use date type
                  ps.setDouble(2, it.getOpen().orElse(null));
                  ps.setDouble(3, it.getHigh().orElse(null));
                  ps.setDouble(4, it.getLow().orElse(null));
                  ps.setDouble(5, it.getClose().orElse(null));
                  ps.setDouble(6, it.getVolume().orElse(null));
                  ps.executeUpdate();
                } catch (SQLException e) {
                  throw new DbException(e);
                }
              });
        }

      } catch (SQLException e) {
        throw new DbException(e);
      }

    } else {
      int count = 0;
      var appender = t.createAppender();
      try {

        for (OHLCV candle : data) {

          appender.beginRow();

          Date d = new java.sql.Date(System.currentTimeMillis());

          Timestamp ts = Timestamp.from(Instant.now());

          appender.append(ts);
          appender.append(candle.getOpen().orElse(null));
          appender.append(
              (Double)
                  ((candle.getHighAsDecimal().isPresent())
                      ? candle.getHighAsDecimal().get().doubleValue()
                      : null));
          appender.append(
              (Double)
                  ((candle.getLowAsDecimal().isPresent())
                      ? candle.getLowAsDecimal().get().doubleValue()
                      : null));
          appender.append(
              (Double)
                  ((candle.getCloseAsDecimal().isPresent())
                      ? candle.getCloseAsDecimal().get().doubleValue()
                      : null));
          appender.append(
              (Double)
                  ((candle.getVolumeAsDecimal().isPresent())
                      ? candle.getVolumeAsDecimal().get().doubleValue()
                      : null));

          appender.endRow();
          count++;
        }

      } catch (SQLException e) {
        throw new DbException(e);
      } finally {
        try {
          appender.close();
        } catch (SQLException e) {
          throw new BxException(e);
        }
      }

      logger.atInfo().log("added {} rows", count);
    }
  }

  public int mergeMissing(String table, String toBeAdded) {
    String sql =
        String.format(
            "insert into %s (select * from %s where date not in (select date from %s))",
            table, toBeAdded, table);
    return getJdbcClient().sql(sql).update();
  }
}
