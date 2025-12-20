package bq.ta4j;

import bq.BqTest;
import org.junit.jupiter.api.Test;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

public class IndicatorsTest extends BqTest {

  @Test
  public void testIt() {
    var bs = Bars.toBarSeries(getSampleGOOG().stream());

    var sma = new SMAIndicator(new ClosePriceIndicator(bs), 10);

    var atr = new ATRIndicator(bs, 5);

    for (int i = bs.getBeginIndex(); i < bs.getEndIndex(); i++) {
      System.out.println(i + " " + atr.getValue(i) + " " + sma.isStable());
    }
  }
}
