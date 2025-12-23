package bq.indicator.btc;

import bq.PriceTable;
import bq.chart.Chart;
import bq.indicator.IndicatorTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class BtcMVRVZIndicatorTest extends IndicatorTest {

  @Test
  public void testB() {

    PriceTable t = getTestData().loadBtcPriceTable("btc");

    t.addIndicator("foo", "btc_mvrvz()");
    t.addIndicator("bar", "sma(foo,100)");

    Chart.newChart()
        .trace(
            "mvrv",
            trace -> {
              trace.addData("foo", t);
              trace.yAxis(
                  y -> {
                    //   y.logScale();
                  });
            })
        .view();
  }
}
