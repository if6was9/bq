package bq.provider;

import java.util.Set;
import javax.sql.DataSource;

public class DataProviders {

  DataProvider stockProvider = new MassiveProvider();
  DataProvider cryptoProvider = new CoinbaseDataProvider();

  private static DataProviders instance = new DataProviders();
  private DataSource dataSource;

  private DataProviders() {}

  Set<String> cryptoSymbols =
      Set.of("BTC", "ETH", "USDT", "BNB", "XRP", "USDC", "SOL", "TRX", "DOGE", "XMR", "ADA");

  public boolean isCrypto(String s) {
    return cryptoSymbols.contains(s.toUpperCase().trim());
  }

  public static void setDataSource(DataSource dataSource) {
    instance.dataSource = dataSource;
  }

  public static DataProvider.Request forSymbol(String symbol) {
    return findProviderForSymbol(symbol).newRequest(symbol);
  }

  public static DataProvider findProviderForSymbol(String symbol) {

    DataProvider p = null;
    if (instance.isCrypto(symbol)) {
      p = instance.cryptoProvider;
    } else {
      p = instance.stockProvider;
    }
    if (p != null) {
      if (p.getDataSource() == null) {
        p.dataSource = instance.dataSource;
      }
    }
    return p;
  }
}
