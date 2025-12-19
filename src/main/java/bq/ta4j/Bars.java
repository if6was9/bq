package bq.ta4j;

import bq.OHLCV;
import bx.util.Iterators;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesBuilder;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNumFactory;

public class Bars {

  static BarSeries barFactory =
      new BaseBarSeriesBuilder()
          .withName(null)
          .withNumFactory(DoubleNumFactory.getInstance())
          .build();

  public static BarSeriesBuilder newBarSeriesBuilder(String name) {
    return new BaseBarSeriesBuilder().withName(name).withNumFactory(DoubleNumFactory.getInstance());
  }

  public static class DateComparator implements Comparator<Bar> {
    @Override
    public int compare(Bar o1, Bar o2) {
      if (o1 == null) {
        return 1;
      }
      if (o2 == null) {
        return -1;
      }

      return o1.getBeginTime().compareTo(o2.getBeginTime());
    }
  }

  public static BarSeries toBarSeries(Stream<OHLCV> stream) {
    return toBarSeries(stream, null);
  }

  public static BarSeries toBarSeries(Stream<OHLCV> stream, String name) {
    if (stream == null) {
      stream = Stream.of();
    }
    return toBarSeries(stream.map(Bars::toBar).toList());
  }

  public static BarSeries toBarSeries(List<Bar> bars) {
    return toBarSeries(bars, null);
  }

  public static BarSeries toBarSeries(List<Bar> bars, String name) {

    BarSeries bs = newBarSeriesBuilder(name).build();
    if (bars != null) {
      bars.forEach(
          bar -> {
            bs.addBar(bar);
          });
    }
    return bs;
  }

  public static Bar toBar(OHLCV ohlcv, BarSeries bs) {
    var b = bs.barBuilder();

    int count = bs.getBarCount();

    try {
      b.timePeriod(Duration.ofDays(1));
      b.beginTime(ohlcv.getTimestamp());
      ohlcv
          .getOpenAsDouble()
          .ifPresent(
              p -> {
                b.openPrice(p);
              });
      ohlcv
          .getHighAsDouble()
          .ifPresent(
              p -> {
                b.highPrice(p);
              });
      ohlcv
          .getLowAsDouble()
          .ifPresent(
              p -> {
                b.lowPrice(p);
              });
      ohlcv
          .getCloseAsDouble()
          .ifPresent(
              p -> {
                b.closePrice(p);
              });
      ohlcv
          .getVolumeAsDouble()
          .ifPresent(
              p -> {
                b.volume(p);
              });

      return b.build();

    } finally {
      Preconditions.checkState(bs.getBarCount() == count);
    }
  }

  public static class BarSeriesIterator implements Iterator<Bar> {

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
  }

  public static Iterator<Bar> toIterator(BarSeries bs) {
    return new BarSeriesIterator(bs);
  }

  public static Stream<Bar> toStream(BarSeries series) {

    return Iterators.toStream(toIterator(series));
  }

  public static Bar toBar(OHLCV ohlcv) {
    Preconditions.checkState(barFactory.getBarCount() == 0);
    return toBar(ohlcv, barFactory);
  }
}
