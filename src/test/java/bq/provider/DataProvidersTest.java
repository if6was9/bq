package bq.provider;

import bq.BqTest;
import bq.ta4j.Bars;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;

public class DataProvidersTest extends BqTest {

  @Test
  public void testIt() {

    Assertions.assertThat(DataProviders.forSymbol("btc")).isInstanceOf(CoinbaseDataProvider.class);

    DataProviders.forSymbol("btc");

    DataProviders.newRequest("btc")
        .from(2025, 12, 1)
        .to(2025, 12, 10)
        .fetchStream()
        .forEach(
            it -> {
              System.out.println(it);
            });

    Assertions.assertThat(DataProviders.forSymbol("GOOG")).isInstanceOf(MassiveProvider.class);

    DataProviders.newRequest("GOOG")
        .from(2025, 12, 1)
        .to(2025, 12, 10)
        .fetchStream()
        .forEach(
            it -> {
              System.out.println(it);
            });
  }

  @Test
  public void testDataSource() {
    Assertions.assertThat(DataProviders.get().getDataSource()).isSameAs(getDataSource());
    Assertions.assertThat(DataProviders.newRequest("GOOG").getDataSource())
        .isSameAs(getDataSource());
  }

  @Test
  public void testItx() {
    var t = DataProviders.newRequest("btc").fetchIntoTable("test");
    t.show();

    BarSeries b = t.getBarSeries();

    System.out.println(Bars.toString(b));

    System.out.println(b.getFirstBar());
    System.out.println();
  }
}
