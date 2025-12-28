package bq.indicator.btc;

import bx.util.Zones;
import java.time.LocalDate;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

public class BtcDayOfEpochIndicator extends AbstractBtcEpochIndicator {

  public BtcDayOfEpochIndicator(BarSeries series) {
    super(series);
  }

  @Override
  public Num getValue(int index) {
    Bar bar = getBarSeries().getBar(index);
    if (bar == null) {
      return null;
    }
    LocalDate d = bar.getBeginTime().atZone(Zones.UTC).toLocalDate();

    long count = BtcUtil.getDayOfEpoch(d);
    return DoubleNum.valueOf(count);
  }

  @Override
  public int getCountOfUnstableBars() {
    return 0;
  }
}
