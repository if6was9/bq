package bq.provider;

import bq.PriceTable;
import bx.util.Slogger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public class PriceDataUpdate {
  static Logger logger = Slogger.forEnclosingClass();

  PriceTable table;
  String symbol;

  class DateMapper implements RowMapper<LocalDate> {

    @Override
    public LocalDate mapRow(ResultSet rs, int rowNum) throws SQLException {
      // TODO Auto-generated method stub
      return null;
    }
  }

  public PriceDataUpdate() {}

  public PriceDataUpdate table(PriceTable table) {
    this.table = table;
    return this;
  }

  public PriceDataUpdate symbol(String s) {
    this.symbol = s;
    return this;
  }

  public DataProvider getDataProvider() {

    return DataProviders.forSymbol(symbol);
  }

  void refresh() {

    List<LocalDate> dates =
        table
            .sql("select date from " + table.getName() + " order by date desc")
            .query(new DateMapper())
            .list();
    if (dates.isEmpty()) {
      logger.atInfo().log("full load");
      PriceTable pt = getDataProvider().newRequest(symbol).fetchIntoTable();

      int count = this.table.insertMissing(pt);
      logger.atInfo().log("added {} record", count);
      pt.show();
    } else {
      logger.atInfo().log("incremental load");
      PriceTable pt = getDataProvider().newRequest(symbol).fetchIntoTable();

      int count = this.table.insertMissing(pt);

      logger.atInfo().log("added {} record", count);
    }
  }
}
