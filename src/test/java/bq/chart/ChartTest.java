package bq.chart;

import bq.BqTest;
import bq.ta4j.Bars;
import org.junit.jupiter.api.Test;

public class ChartTest extends BqTest {

  @Test
  public void testIt() {

    var bs = Bars.toBarSeries(getSampleGOOG().stream());

    Chart.newChart()
        .title("foo")
        .trace(
            "test",
            c -> {
              c.addData(bs);
            })
        .view();
  }
}
