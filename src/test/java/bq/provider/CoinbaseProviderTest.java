package bq.provider;

import bq.BqTest;
import bq.ta4j.Bars;
import bx.util.Zones;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class CoinbaseProviderTest extends BqTest {

  @Test
  public void testX() {
    CoinbaseDataProvider cb = new CoinbaseDataProvider();
    var t =
        cb.dataSource(getDataSource())
            .forSymbol("btc")
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
            .forSymbol("btc")
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
            .forSymbol("btc")
            .from(LocalDate.of(2025, 12, 1))
            .to(LocalDate.of(2025, 12, 7))
            .fetchBarSeries();

    System.out.println(t);
  }

  @Test
  public void testRange() {

    getDuckDataManager().createOHLCV("test", false);
    var t =
        new CoinbaseDataProvider()
            .dataSource(getDataSource())
            .forSymbol("btc")
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
            .forSymbol("btc")
            .from(LocalDate.of(2025, 12, 1))
            .fetchIntoTable();

    t.prettyQuery().select();
  }

  @Test
  public void testNullTo() {
    CoinbaseDataProvider cb = new CoinbaseDataProvider();

    var candles =
        cb.forSymbol("btc")
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
        cb.forSymbol("btc")
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
  public void testDaysAgo() {
    new CoinbaseDataProvider()
        .forSymbol("btc")
        .fromDaysAgo(3)
        .fetchStream()
        .forEach(
            it -> {
              System.out.println(it);
            });
  }
}
