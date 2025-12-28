package bq.provider;

import bq.DataManager;
import bq.OHLCV;
import bq.PriceTable;
import bq.ta4j.Bars;
import bx.util.Zones;
import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNumFactory;

public abstract class DataProvider {

  DataSource dataSource;

  public class Request {
    LocalDate from = null;
    LocalDate to = null;
    String symbol = null;
    boolean includeUnclosedPeriod = true;

    public Optional<LocalDate> getFrom() {
      return java.util.Optional.ofNullable(from);
    }

    public Request includeUnclosedPeriod(boolean b) {
      this.includeUnclosedPeriod = b;
      return this;
    }

    public DataSource getDataSource() {
      return DataProvider.this.getDataSource();
    }

    public boolean isUnclosedPeriodIncluded() {
      return includeUnclosedPeriod;
    }

    public Optional<LocalDate> getTo() {
      return Optional.ofNullable(to);
    }

    public Request fromDaysAgo(int count) {
      return from(LocalDate.now(Zones.UTC).minusDays(count));
    }

    public Request toDaysAgo(int count) {
      return to(LocalDate.now(Zones.UTC).minusDays(count));
    }

    public Request notBefore(LocalDate d) {
      return from(d);
    }

    public Request notBefore(int y, int m, int d) {
      return from(y, m, d);
    }

    public Request notAfter(LocalDate d) {
      return from(d);
    }

    public Request notAfter(int y, int m, int d) {
      return from(y, m, d);
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

    public Request symbol(String symbol) {
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

    public PriceTable fetchIntoTable() {
      String tableName = String.format("temp_%s", System.currentTimeMillis());

      Preconditions.checkState(dataSource != null, "DataSource must be set");

      return fetchIntoTable(tableName);
    }

    public PriceTable fetchIntoTable(String table) {
      return fetchIntoTable(table, true);
    }

    public PriceTable fetchIntoTable(String table, boolean create) {
      Preconditions.checkState(DataProvider.this.dataSource != null, "dataSource must be set");
      DataManager ddm = new DataManager().dataSource(dataSource);

      var t = PriceTable.from(dataSource, table);
      if (create) {
        if (!t.exists()) {
          ddm.createOHLCV(table, true);
        }
      }
      ddm.insert(table, fetchStream().toList());
      return PriceTable.from(dataSource, table);
    }
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public <T extends DataProvider> T dataSource(DataSource ds) {
    this.dataSource = ds;
    return (T) this;
  }

  public LocalDate getDefaultNotBefore() {
    return LocalDate.now(Zones.UTC).minusYears(2);
  }

  public Request newRequest(String symbol) {
    return newRequest().symbol(symbol);
  }

  public Request newRequest() {
    Request r = new Request();
    return r;
  }

  protected abstract Stream<OHLCV> fetch(Request request);
}
