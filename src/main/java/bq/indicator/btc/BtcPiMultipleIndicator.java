package bq.indicator.btc;

import bx.util.Slogger;
import org.slf4j.Logger;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.AbstractIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.NaN;
import org.ta4j.core.num.Num;

public class BtcPiMultipleIndicator extends AbstractIndicator<Num> {

  static Logger logger = Slogger.forEnclosingClass();

  SMAIndicator sma350;
  SMAIndicator sma111;

  public BtcPiMultipleIndicator(BarSeries series) {
    super(series);
    ClosePriceIndicator closeIndicator = new ClosePriceIndicator(series);
    sma350 = new org.ta4j.core.indicators.averages.SMAIndicator(closeIndicator, 350);
    sma111 = new org.ta4j.core.indicators.averages.SMAIndicator(closeIndicator, 111);
  }

  @Override
  public Num getValue(int index) {

    try {
      Num v350 = sma350.getValue(index);
      Num v111 = sma111.getValue(index);

      if (v350 == null || v111 == null) {
        return DoubleNum.valueOf(Double.NaN);
      }

      double piMultiple = 1 - ((2.0 * v350.doubleValue()) / v111.doubleValue());

      return DoubleNum.valueOf(piMultiple);
    } catch (RuntimeException e) {
      logger.atDebug().setCause(e).log();
    }
    return NaN.NaN;
  }

  @Override
  public int getCountOfUnstableBars() {

    return 0;
  }
}
