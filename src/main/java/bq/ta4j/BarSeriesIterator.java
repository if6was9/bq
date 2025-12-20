package bq.ta4j;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

public class BarSeriesIterator implements Iterator<Bar> {

  BarSeries series;
  int index = -1;

  public BarSeriesIterator(BarSeries b) {
    this.series = b;
  }

  @Override
  public boolean hasNext() {
    if (index == -1) {}

    if (index < series.getEndIndex()) {
      return true;
    }
    return false;
  }

  @Override
  public Bar next() {

    if (hasNext()) {

      if (index < 0) {
        index = series.getBeginIndex();
      } else {
        index++;
      }
      return series.getBar(index);
    } else {
      throw new NoSuchElementException();
    }
  }

  public int getBarIndex() {
    return index;
  }
}
