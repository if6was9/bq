package bq.provider;

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

  public DataProvider getDataProviderForSymbol(String symbol) {
    if (isCrypto(symbol)) {
      return new CoinbaseDataProvider().dataSource(instance.dataSource);
    }
    return new MassiveProvider().dataSource(instance.dataSource);
  }

  public static DataProvider forSymbol(String symbol) {

    return get().getDataProviderForSymbol(symbol);
  }

  public static DataProvider.Request newRequest(String symbol) {
    return get().newRequestForSymbol(symbol);
  }

  public DataProvider.Request newRequestForSymbol(String symbol) {
    return getDataProviderForSymbol(symbol).newRequest(symbol);
  }
}
