package bq;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

public class PriceTableTest extends BqTest {

  @Test
  public void testIt() {

    PriceTable pt = PriceTable.from(getTestData().loadBtcPriceData("btc"));

    // pt.getDuckTable().getJdbcClient().sql("delete from btc where date<'2025-01-15'").update();
    Assertions.assertThat(pt.getDuckTable()).isNotNull();

    // ema(foo(close),3)

    var ema = new EMAIndicator(new ClosePriceIndicator(pt.getBarSeries()), 3);

    System.out.println(ema.getValue(0));

    pt.addIndicator(
        "ema",
        bs -> {
          return new EMAIndicator(new ClosePriceIndicator(bs), 3);
        });

    pt.getDuckTable().show();
  }
}
