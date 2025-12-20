package bq.ta4j;

import bq.BasicOHLCV;
import bq.BqTest;
import bq.OHLCV;
import bq.provider.MassiveProvider;
import bx.util.Zones;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;

public class BarsTest extends BqTest {

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

  public static String createAddBarCode(OHLCV it) {
    LocalDate d = it.getDate();

    String s =
        String.format(
            "list.add( BasicOHLCV.of( %s, %s, %s, %s, %s, %s));",
            String.format(
                "LocalDate.of(%s,%s,%s)", d.getYear(), d.getMonthValue(), d.getDayOfMonth()),
            it.getOpenAsDouble().get(),
            it.getHighAsDouble().get(),
            it.getLowAsDouble().get(),
            it.getCloseAsDouble().get(),
            it.getVolumeAsDouble().get());

    return s;
  }

  @Test
  @Disabled
  public void generateTestData() {

    // This will fetch some sample data that can be pasted back into test method

    new MassiveProvider()
        .forSymbol("GOOG")
        .from(LocalDate.of(2025, 1, 1))
        .to(LocalDate.of(2025, 3, 30))
        .fetchStream()
        .map(BarsTest::createAddBarCode)
        .forEach(
            it -> {
              System.out.println(it);
            });
  }

  @Test
  public void testToBar() {
    getSampleGOOG()
        .forEach(
            ohlcv -> {
              Bar bar = Bars.toBar(ohlcv);

              // TA4J uses half-open for periods [begin time,end time)
              Assertions.assertThat(bar.getBeginTime().toString())
                  .startsWith(ohlcv.getDate().toString());
              Assertions.assertThat(bar.getEndTime().toString())
                  .startsWith(ohlcv.getDate().plusDays(1).toString());
              Assertions.assertThat(bar.getOpenPrice().doubleValue())
                  .isEqualTo(ohlcv.getOpenAsDouble().get());
              Assertions.assertThat(bar.getHighPrice().doubleValue())
                  .isEqualTo(ohlcv.getHighAsDouble().get());
              Assertions.assertThat(bar.getLowPrice().doubleValue())
                  .isEqualTo(ohlcv.getLowAsDouble().get());
              Assertions.assertThat(bar.getClosePrice().doubleValue())
                  .isEqualTo(ohlcv.getCloseAsDouble().get());
              Assertions.assertThat(bar.getVolume().doubleValue())
                  .isEqualTo(ohlcv.getVolumeAsDouble().get());
            });
  }

  @Test
  public void testFindBarIndex() {

    var goog = getSampleGOOG();

    var bs = Bars.toBarSeries(goog.stream());

    Bars.checkOrder(bs);

    BarSeriesIterator t = Bars.toIterator(bs);

    t.forEachRemaining(
        b -> {
          int i = t.getBarIndex();

          System.out.println(i + " " + b);
          Bar bar = bs.getBar(i);

          Optional<Integer> x =
              Bars.findBarIndexByDate(bs, bar.getBeginTime().atZone(Zones.UTC).toLocalDate());

          Assertions.assertThat(x).isPresent();
          Assertions.assertThat(x.get()).isEqualTo(i);
        });
  }
}
