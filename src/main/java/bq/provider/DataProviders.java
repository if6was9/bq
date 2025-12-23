package bq.provider;

import java.util.Set;

public class DataProviders {

  DataProvider stockProvider = new MassiveProvider();
  DataProvider cryptoProvider = new CoinbaseDataProvider();

  private static DataProviders instance = new DataProviders();

  private DataProviders() {}

  Set<String> cryptoSymbols =
      Set.of("BTC", "ETH", "USDT", "BNB", "XRP", "USDC", "SOL", "TRX", "DOGE", "XMR", "ADA");

  public boolean isCrypto(String s) {
    return cryptoSymbols.contains(s.toUpperCase().trim());
  }

  public static DataProvider.Request forSymbol(String symbol) {
    return findProviderForSymbol(symbol).newRequest(symbol);
  }

  public static DataProvider findProviderForSymbol(String symbol) {

    if (instance.isCrypto(symbol)) {
      return instance.cryptoProvider;
    } else {
      return instance.stockProvider;
    }
  }
}
