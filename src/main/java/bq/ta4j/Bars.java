package bq.ta4j;

import bq.BasicOHLCV;
import bq.OHLCV;
import bx.util.Item;
import bx.util.Iterators;
import bx.util.Zones;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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

  public static String toString(BarSeries b) {

    if (b == null) {
      return Objects.toString(null);
    }
    Instant from = null;
    Instant to = null;
    if (b.getFirstBar() != null) {
      from = b.getFirstBar().getBeginTime();
      to = b.getLastBar().getEndTime();
    }

    int count = b.getBarCount();
    return MoreObjects.toStringHelper(BarSeries.class)
        .add("begin", b.getFirstBar().getBeginTime())
        .add("end", to)
        .add("count", b.getBarCount())
        .toString();
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
        ohlcv.getOpen().orElse(null),
        ohlcv.getHigh().orElse(null),
        ohlcv.getLow().orElse(null),
        ohlcv.getClose().orElse(null),
        ohlcv.getVolume().orElse(null),
        ohlcv.getId().orElse(null));
  }

  public static OHLCV toOHLCV(Bar b) {
    return BasicOHLCV.of(
        b.getBeginTime(),
        b.getOpenPrice() != null ? b.getOpenPrice().doubleValue() : null,
        b.getHighPrice() != null ? b.getHighPrice().doubleValue() : null,
        b.getLowPrice() != null ? b.getLowPrice().doubleValue() : null,
        b.getClosePrice() != null ? b.getClosePrice().doubleValue() : null,
        b.getVolume() != null ? b.getVolume().doubleValue() : null);
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

  /**
   * TA4J BarSeries indexes are unusual *AND* necessary to use indicators. Using
   * the Item<Bar> construct makes it easy to iterate across a bar series and, for
   * example, get Indicator values.
   *
   * @param barSeries
   * @return
   */
  public static List<Item<Bar>> toIndexedList(BarSeries barSeries) {

    List<Item<Bar>> items = Lists.newArrayList();
    for (int i = barSeries.getBeginIndex(); i <= barSeries.getEndIndex(); i++) {
      items.add(Item.of(barSeries.getBar(i), i));
    }
    return List.copyOf(items);
  }
}
