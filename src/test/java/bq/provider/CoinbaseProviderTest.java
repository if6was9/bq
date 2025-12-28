package bq.provider;

import bq.BqTest;
import bq.OHLCV;
import bq.ta4j.Bars;
import bx.util.Slogger;
import bx.util.Zones;
import com.google.common.base.Stopwatch;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class CoinbaseProviderTest extends BqTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testX() {
    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    var t =
        cb.dataSource(getDataSource())
            .newRequest("btc")
            .from(LocalDate.of(2025, 12, 1))
            .to(LocalDate.of(2025, 12, 7));

    t.fetchStream()
        .forEach(
            it -> {
              System.out.println(it);
            });
    Bars.toIterator(t.fetchBarSeries())
        .forEachRemaining(
            it -> {
              System.out.println(it);
            });
  }

  @Test
  void testId() {

    CoinbaseDataProvider cb = new CoinbaseDataProvider();
    var t =
        cb.dataSource(getDataSource())
            .newRequest("btc")
            .from(LocalDate.of(2017, 12, 1))
            .to(LocalDate.of(2025, 12, 7))
            .fetchIntoTable();

    t.prettyQuery().select();
  }

  @Test
  void testFromToDefaults() {

    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    // if not from is set, a value of 2 years is set.
    var t = cb.dataSource(getDataSource()).newRequest("btc").fetchStream().toList();

    checkOrdering(t);
    Assertions.assertThat(t.getFirst().getDate()).isEqualTo(LocalDate.now(Zones.UTC).minusYears(2));
  }

  @Test
  void testFetchBarSeries() {

    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    var t =
        cb.dataSource(getDataSource())
            .newRequest("btc")
            .from(LocalDate.of(2017, 12, 1))
            .to(LocalDate.of(2025, 12, 7))
            .fetchIntoTable();

    t.prettyQuery().select();
  }

  @Test
  public void testRange() {

    getDuckDataManager().createOHLCV("test", false);
    var t =
        new CoinbaseDataProvider()
            .dataSource(getDataSource())
            .newRequest("btc")
            .from(LocalDate.of(2025, 12, 1))
            .fetchIntoTable("test");

    t.prettyQuery().select();
  }

  void checkOrdering(List<OHLCV> bars) {
    if (bars == null) {
      bars = List.of();
    }
    Instant lastTs = null;
    for (OHLCV bar : bars) {
      Instant ts = bar.getTimestamp();

      if (lastTs != null) {

        Assertions.assertThat(ts).isAfter(lastTs);
      }
      lastTs = ts;
    }
  }

  @Test
  public void testDefaultFrom() {

    getDuckDataManager().createOHLCV("test", false);
    var t =
        new CoinbaseDataProvider()
            .dataSource(getDataSource())
            .newRequest("BTC")
            .fetchStream()
            .toList();

    checkOrdering(t);

    Assertions.assertThat(t.getFirst().getDate()).isAfterOrEqualTo(LocalDate.now().minusYears(2));
    Assertions.assertThat(t.size()).isGreaterThanOrEqualTo(730).isLessThanOrEqualTo(732);
  }

  @Test
  public void testFetchIntoTable() {

    getDuckDataManager().createOHLCV("test", false);
    var t =
        new CoinbaseDataProvider()
            .dataSource(getDataSource())
            .newRequest("btc")
            .from(LocalDate.of(2025, 12, 1))
            .fetchIntoTable();

    t.prettyQuery().select();
  }

  @Test
  public void testNullToWithExcludeUnclosed() {
    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    var candles =
        cb.newRequest("btc")
            .from(LocalDate.now(Zones.UTC).minusDays(3))
            .to(null)
            .includeUnclosedPeriod(false)
            .fetchStream()
            .toList();

    candles.forEach(
        it -> {
          System.out.println(it);
        });
    Assertions.assertThat(candles).hasSize(3);

    Assertions.assertThat(candles.get(0).getDate())
        .isEqualTo(cb.getLastClosedTradingDay().minusDays(2));
    Assertions.assertThat(candles.get(1).getDate())
        .isEqualTo(cb.getLastClosedTradingDay().minusDays(1));

    Assertions.assertThat(candles.get(2).getDate())
        .isEqualTo(cb.getLastClosedTradingDay().minusDays(0));
  }

  @Test
  public void testNullToWithIncludeUnclosed() {
    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    var candles =
        cb.newRequest("btc")
            .from(LocalDate.now(Zones.UTC).minusDays(3))
            .includeUnclosedPeriod(true) // explicitly set
            .fetchStream()
            .toList();

    candles.forEach(
        it -> {
          System.out.println(it);
        });

    Assertions.assertThat(candles.size()).isEqualTo(4);
    Assertions.assertThat(candles.get(0).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(3));
    Assertions.assertThat(candles.get(1).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(2));
    Assertions.assertThat(candles.get(2).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(1));
    Assertions.assertThat(candles.get(3).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(0));

    // now do it again and see that the result is the same
    candles =
        cb.newRequest("btc").from(LocalDate.now(Zones.UTC).minusDays(3)).fetchStream().toList();

    candles.forEach(
        it -> {
          System.out.println(it);
        });

    Assertions.assertThat(candles.size()).isEqualTo(4);
    Assertions.assertThat(candles.get(0).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(3));
    Assertions.assertThat(candles.get(1).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(2));
    Assertions.assertThat(candles.get(2).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(1));
    Assertions.assertThat(candles.get(3).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC).minusDays(0));
  }

  @Test
  void testSingle() {

    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    var list =
        cb.newRequest("btc")
            .from(LocalDate.of(2025, 12, 1))
            .to(LocalDate.of(2025, 12, 1))
            .fetchStream()
            .toList();
    Assertions.assertThat(list).hasSize(1);
    Assertions.assertThat(list.getFirst().getDate()).hasDayOfMonth(1);
    Assertions.assertThat(list.getFirst().getDate()).hasMonthValue(12);

    Assertions.assertThat(list.getFirst().getDate()).hasYear(2025);
  }

  @Test
  public void testSymbol() {
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("BTC")).isEqualTo("BTC-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("btc")).isEqualTo("BTC-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("BTC_USD")).isEqualTo("BTC-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("BTC/USD")).isEqualTo("BTC-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("X:BTC")).isEqualTo("BTC-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("BTC/EUR")).isEqualTo("BTC-EUR");

    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("ETH")).isEqualTo("ETH-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("ETH")).isEqualTo("ETH-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("ETH_USD")).isEqualTo("ETH-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("ETH/USD")).isEqualTo("ETH-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("X:ETH")).isEqualTo("ETH-USD");
    Assertions.assertThat(CoinbaseDataProvider.toCoinbaseSymbol("ETH/EUR")).isEqualTo("ETH-EUR");
  }

  @Test
  public void testCache() {
    var cb = new CoinbaseDataProvider();
    cb.invalidateAll();

    Stopwatch sw = Stopwatch.createStarted();
    cb.newRequest("btc").from(2020, 1, 1).fetchStream().forEach(it -> {});
    long uncachedMs = sw.elapsed(TimeUnit.MILLISECONDS);

    logger.atInfo().log("uncached {}ms", uncachedMs);

    sw = Stopwatch.createStarted();
    cb.newRequest("btc").from(2020, 1, 1).fetchStream().forEach(it -> {});
    long cachedMs = sw.elapsed(TimeUnit.MILLISECONDS);

    logger.atInfo().log("cached {}ms", cachedMs);

    double speedup = ((double) uncachedMs) / ((cachedMs > 0) ? cachedMs : 1);

    logger.atInfo().log("cache speedup: {}x", speedup);

    Assertions.assertThat(speedup)
        .withFailMessage("cache speedup should be >10x")
        .isGreaterThan(10);
  }

  @Test
  public void testRequestDefault() {
    var cb = new CoinbaseDataProvider();
    Assertions.assertThat(cb.newRequest().isUnclosedPeriodIncluded()).isTrue();

    Assertions.assertThat(cb.newRequest().getFrom().isPresent()).isFalse();
    Assertions.assertThat(cb.newRequest().getTo().isPresent()).isFalse();
  }

  @Test
  public void testDaysAgo() {
    new CoinbaseDataProvider()
        .newRequest("btc")
        .fromDaysAgo(3)
        .fetchStream()
        .forEach(
            it -> {
              System.out.println(it);
            });
  }
}
