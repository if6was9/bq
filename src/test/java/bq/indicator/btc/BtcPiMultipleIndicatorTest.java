package bq.indicator.btc;

import bq.PriceTable;
import bq.chart.Chart;
import bq.indicator.IndicatorTest;
import org.junit.jupiter.api.Test;

public class BtcPiMultipleIndicatorTest extends IndicatorTest {

  @Test
  public void testDivZero() {
    double a = 23;
    double b = 0d;

    System.out.println(a / b);
  }

  @Test
  public void testIt() {

    PriceTable t = getTestData().loadBtcPriceTable("btc");

    t.addIndicator("pi", "btc_pi_multiple()");

    Chart.newChart()
        .trace(
            "pi",
            trace -> {
              trace.addData("pi", t);
              trace.yAxis(y -> {});
            })
        .trace(
            "btc",
            trace -> {
              trace.addData("close", t);
              trace.newYAxis(
                  y -> {
                    y.logScale();
                  });
            })
        .view();
  }
}
