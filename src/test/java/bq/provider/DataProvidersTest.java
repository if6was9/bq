package bq.provider;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataProvidersTest {

  @Test
  public void testIt() {

    Assertions.assertThat(DataProviders.findProviderForSymbol("btc"))
        .isInstanceOf(CoinbaseDataProvider.class);

    DataProviders.findProviderForSymbol("btc");

    DataProviders.forSymbol("btc")
        .from(2025, 12, 1)
        .to(2025, 12, 10)
        .fetchStream()
        .forEach(
            it -> {
              System.out.println(it);
            });

    Assertions.assertThat(DataProviders.findProviderForSymbol("GOOG"))
        .isInstanceOf(MassiveProvider.class);

    DataProviders.forSymbol("GOOG")
        .from(2025, 12, 1)
        .to(2025, 12, 10)
        .fetchStream()
        .forEach(
            it -> {
              System.out.println(it);
            });
  }
}
