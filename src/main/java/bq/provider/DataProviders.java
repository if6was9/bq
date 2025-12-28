package bq.provider;

import bq.Ticker;
import java.util.Set;
import javax.sql.DataSource;

public class DataProviders {

  private static DataProviders instance = new DataProviders();
  private DataSource dataSource;

  private DataProviders() {}

  Set<String> cryptoSymbols =
      Set.of("BTC", "ETH", "USDT", "BNB", "XRP", "USDC", "SOL", "TRX", "DOGE", "XMR", "ADA");

  public boolean isCrypto(String s) {
    return cryptoSymbols.contains(s.toUpperCase().trim());
  }

  public static DataProviders get() {
    return instance;
  }

  public DataProviders dataSource(DataSource ds) {
    this.dataSource = ds;
    return this;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public DataProvider getDataProviderForSymbol(Ticker t) {
    if (t.isCrypto()) {
      return new CoinbaseDataProvider().dataSource(instance.dataSource);
    } else if (t.isStock()) {
      return new MassiveProvider().dataSource(instance.dataSource);
    }
    throw new IllegalArgumentException("unsupported " + t);
  }

  public DataProvider getDataProviderForSymbol(String symbol) {

    return getDataProviderForSymbol(Ticker.of(symbol));
  }

  public static DataProvider forSymbol(Ticker t) {
    return get().getDataProviderForSymbol(t);
  }

  public static DataProvider forSymbol(String symbol) {
    return forSymbol(Ticker.of(symbol));
  }

  public static DataProvider.Request newRequest(Ticker t) {
    return get().getDataProviderForSymbol(t).newRequest(t);
  }

  public static DataProvider.Request newRequest(String symbol) {
    return get().newRequestForSymbol(symbol);
  }

  public DataProvider.Request newRequestForSymbol(String symbol) {
    return getDataProviderForSymbol(symbol).newRequest(symbol);
  }
}
