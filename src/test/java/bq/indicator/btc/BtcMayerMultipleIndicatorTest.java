package bq.indicator.btc;

import bq.PriceTable;
import bq.chart.Chart;
import bq.indicator.IndicatorTest;
import org.junit.jupiter.api.Test;

public class BtcMayerMultipleIndicatorTest extends IndicatorTest {

  @Test
  public void testIt() {

    PriceTable t = getTestData().loadBtcPriceTable("btc");

    t.getDuckTable().sql("delete from btc where date <'2014-12-01'").update();

    t.addIndicator("mm", "btc_mayer_multiple()");
    t.addIndicator("sma", "sma(350)");

    Chart.newChart()
        .trace(
            "price",
            t2 -> {
              t2.lineWidth(.5);
              t2.addData("close", t);
              t2.yAxis(
                  y -> {
                    y.logScale();
                  });
            })
        .trace(
            "sma",
            t2 -> {
              t2.lineWidth(.5);
              t2.addData("sma", t);
            })
        .view();

    /*  .trace("mayer", trace -> {
      trace.lineColor("red");
      trace.lineWidth(1);
      trace.data(t, "mm");

    })*/

  }
}
