package bq.provider;

import bq.OHLCV;
import com.google.common.collect.Lists;
import java.util.List;

public class PriceDataUpdate {

  List<OHLCV> data = Lists.newArrayList();
  DataProvider provider;
  String symbol;

  public PriceDataUpdate() {}

  public static PriceDataUpdate forExistingData(String symbol, List<OHLCV> data) {
    if (data == null) {
      data = List.of();
    }
    PriceDataUpdate pdu = new PriceDataUpdate();

    pdu.provider = DataProviders.findProviderForSymbol(symbol);
    pdu.symbol = symbol;

    return pdu;
  }

  public void findMissingData() {
    if (data == null || data.isEmpty()) {
      provider
          .newRequest()
          .symbol(symbol)
          .fetchStream()
          .forEach(
              it -> {
                System.out.println(it);
              });
    }
  }
}
