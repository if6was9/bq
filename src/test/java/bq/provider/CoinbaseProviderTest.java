package bq.provider;

import bq.BqTest;
import bq.ta4j.Bars;
import bx.util.Slogger;
import bx.util.Zones;
import com.google.common.base.Stopwatch;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class CoinbaseProviderTest extends BqTest {

  Logger logger = Slogger.forEnclosingClass();

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
  public void testNullTo() {
    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    var candles =
        cb.newRequest("btc")
            .from(LocalDate.now(Zones.UTC).minusDays(3))
            .to(null)
            .fetchStream()
            .toList();

    candles.forEach(
        it -> {
          System.out.println(it);
        });
    Assertions.assertThat(candles).hasSize(4);
    Assertions.assertThat(candles.getLast().getDate()).isEqualTo(LocalDate.now(Zones.UTC));
    Assertions.assertThat(candles.get(candles.size() - 1).getDate())
        .isEqualTo(LocalDate.now(Zones.UTC));
    Assertions.assertThat(candles.get(candles.size() - 2).getDate())
        .isEqualTo(cb.getLastClosedTradingDay());
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
