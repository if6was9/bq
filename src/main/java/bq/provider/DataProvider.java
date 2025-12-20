package bq.provider;

import bq.DataManager;
import bq.OHLCV;
import bq.ta4j.Bars;
import bx.sql.duckdb.DuckTable;
import bx.util.Zones;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNumFactory;

public abstract class DataProvider {

  DataSource dataSource;

  public class Request {
    LocalDate from;
    LocalDate to;
    String symbol;

    public Optional<LocalDate> getFrom() {
      return Optional.of(from);
    }

    public Optional<LocalDate> getTo() {
      return Optional.of(to);
    }

    public Request fromDaysAgo(int count) {
      return from(LocalDate.now(Zones.UTC).minusDays(count));
    }

    public Request toDaysAgo(int count) {
      return to(LocalDate.now(Zones.UTC).minusDays(count));
    }

    public Request from(int year, int month, int day) {
      return from(LocalDate.of(year, month, day));
    }

    public Request to(int year, int month, int day) {
      return to(LocalDate.of(year, month, day));
    }

    public Request from(LocalDate date) {
      this.from = date;
      return this;
    }

    public Request to(LocalDate date) {

      this.to = date;
      return this;
    }

    Request symbol(String symbol) {
      this.symbol = symbol;
      return this;
    }

    public Stream<OHLCV> fetchStream() {
      return fetch(this);
    }

    public BarSeries fetchBarSeries() {
      BarSeries s =
          new BaseBarSeriesBuilder()
              .withName(symbol)
              .withNumFactory(DoubleNumFactory.getInstance())
              .build();

      List<Bar> bars = fetchStream().map(ohlcv -> Bars.toBar(ohlcv)).toList();
      return new BaseBarSeriesBuilder()
          .withName(symbol)
          .withNumFactory(DoubleNumFactory.getInstance())
          .withBars(bars)
          .build();
    }

    public DuckTable fetchIntoTable() {
      String tableName = String.format("temp_%s", System.currentTimeMillis());

      Preconditions.checkState(dataSource != null, "DataSource must be set");
      DataManager ddm = new DataManager().dataSource(dataSource);

      var table = ddm.createOHLCV(tableName, true);

      return fetchIntoTable(table.getTableName());
    }

    public DuckTable fetchIntoTable(String table) {
      Preconditions.checkState(DataProvider.this.dataSource != null, "dataSource must be set");
      DataManager ddm = new DataManager().dataSource(dataSource);
      ddm.insert(table, fetchStream().toList());
      return DuckTable.of(dataSource, table);
    }
  }

  public <T extends DataProvider> T dataSource(DataSource ds) {
    this.dataSource = ds;
    return (T) this;
  }

  public Request forSymbol(String symbol) {
    return newRequest().symbol(symbol);
  }

  private Request newRequest() {
    Request r = new Request();

    return r;
  }

  protected abstract Stream<OHLCV> fetch(Request request);
}
