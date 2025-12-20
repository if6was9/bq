package bq.ta4j;

import bq.OHLCV;
import bx.util.Iterators;
import bx.util.Zones;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BarSeriesBuilder;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNumFactory;

public class Bars {

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

  public static Optional<Integer> findBarIndexByDate(BarSeries series, Bar b) {
    return findBarIndexByDate(series, b.getBeginTime().atZone(Zones.UTC).toLocalDate());
  }

  public static Optional<Integer> findBarIndexByDate(BarSeries series, LocalDate d) {

    for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
      Bar b = series.getBar(i);
      if ((b.getBeginTime().atZone(Zones.UTC)).toLocalDate().isEqual(d)) {
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }

  public static Bar toBar(OHLCV ohlcv) {

    return IndexedBar.create(
        Duration.ofDays(1),
        ohlcv.getTimestamp(),
        null,
        ohlcv.getOpenAsDouble().orElse(null),
        ohlcv.getHighAsDouble().orElse(null),
        ohlcv.getLowAsDouble().orElse(null),
        ohlcv.getCloseAsDouble().orElse(null),
        ohlcv.getVolumeAsDouble().orElse(null),
        ohlcv.getId().orElse(null));
  }

  public static void checkOrder(BarSeries bs) {

    Instant last = null;
    Iterator<Bar> t = Bars.toIterator(bs);
    while (t.hasNext()) {
      Bar b = t.next();
      if (last != null) {
        if (!b.getBeginTime().isAfter(last)) {
          throw new IllegalStateException(
              String.format("%s is not after %s", b.getBeginTime(), last));
        }
      }
    }
  }

  public static BarSeriesIterator toIterator(BarSeries bs) {
    return new BarSeriesIterator(bs);
  }

  public static Stream<Bar> toStream(BarSeries series) {

    return Iterators.toStream(toIterator(series));
  }
}
