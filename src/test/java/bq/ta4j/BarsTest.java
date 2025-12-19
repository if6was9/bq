package bq.ta4j;

import bq.BasicOHLCV;
import bq.BqTest;
import bq.OHLCV;
import bq.provider.MassiveProvider;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNumFactory;

public class BarsTest extends BqTest {

  public Bar toDailyBar(OHLCV ohlcv) {
    var series =
        new BaseBarSeriesBuilder()
            .withName("mySeries")
            .withNumFactory(DoubleNumFactory.getInstance())
            .build();
    var b = series.barBuilder();
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
  }

  @Test
  public void testCompare() {

    Bar a =
        Bars.toBar(
            BasicOHLCV.of(LocalDate.of(2025, 12, 10), (Double) null, null, null, null, null));
    Bar b =
        Bars.toBar(
            BasicOHLCV.of(LocalDate.of(2025, 12, 11), (Double) null, null, null, null, null));

    var list = List.of(b, a).stream().sorted(new Bars.DateComparator()).toList();
    Assertions.assertThat(list).containsExactly(a, b);
  }

  @Test
  public void testIterator() {
    Bar a =
        Bars.toBar(
            BasicOHLCV.of(LocalDate.of(2025, 12, 10), (Double) null, null, null, null, null));
    Bar b =
        Bars.toBar(
            BasicOHLCV.of(LocalDate.of(2025, 12, 11), (Double) null, null, null, null, null));

    var bs = Bars.toBarSeries(List.of(a, b));

    Iterator<Bar> t = Bars.toIterator(bs);
    Assertions.assertThat(t.hasNext()).isTrue();

    Assertions.assertThat(t.hasNext()).isTrue();
    Assertions.assertThat(t.next()).isSameAs(a);
    Assertions.assertThat(t.hasNext()).isTrue();
    Assertions.assertThat(t.next()).isSameAs(b);
    Assertions.assertThat(t.hasNext()).isFalse();

    try {
      t.next();
      Assertions.failBecauseExceptionWasNotThrown(NoSuchElementException.class);
    } catch (NoSuchElementException expected) {
    }
  }

  @Test
  public void testStream() {
    Bar a =
        Bars.toBar(
            BasicOHLCV.of(LocalDate.of(2025, 12, 10), (Double) null, null, null, null, null));
    Bar b =
        Bars.toBar(
            BasicOHLCV.of(LocalDate.of(2025, 12, 11), (Double) null, null, null, null, null));

    var bs = Bars.toBarSeries(List.of(a, b));

    Assertions.assertThat(Bars.toStream(bs).toList()).hasSize(2).containsExactly(a, b);
  }

  @Test
  public void testIt() {

    new MassiveProvider()
        .forSymbol("GOOG")
        .fromDaysAgo(5)
        .fetchStream()
        .map(this::toDailyBar)
        .forEach(
            it -> {
              System.out.println(it);
            });
  }
}
