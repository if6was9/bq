package bq;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class BasicOHLCVTest extends BqTest {

  @Test
  public void testIt() {

    var candle = BasicOHLCV.of(LocalDate.of(2025, 12, 10), 10.0, 11.2, 9.8, 9.9, 1234.5);

    Assertions.assertThat(candle.getOpenAsDecimal().get()).isEqualByComparingTo("10");
    Assertions.assertThat(candle.getHighAsDecimal().get()).isEqualByComparingTo("11.2");
    Assertions.assertThat(candle.getLowAsDecimal().get()).isEqualByComparingTo("9.8");
    Assertions.assertThat(candle.getCloseAsDecimal().get()).isEqualByComparingTo("9.9");
    Assertions.assertThat(candle.getVolumeAsDecimal().get()).isEqualByComparingTo("1234.5");

    Assertions.assertThat(candle.getOpen().get()).isEqualByComparingTo(10d);
    Assertions.assertThat(candle.getHigh().get()).isEqualByComparingTo(11.2d);
    Assertions.assertThat(candle.getLow().get()).isEqualByComparingTo(9.8d);
    Assertions.assertThat(candle.getClose().get()).isEqualByComparingTo(9.9d);
    Assertions.assertThat(candle.getVolume().get()).isEqualByComparingTo(1234.5d);

    candle = BasicOHLCV.of(LocalDate.of(2025, 12, 10), null, 11.2, 9.8, 9.9, 1234.5);
    Assertions.assertThat(candle.getOpenAsDecimal()).isEmpty();
    Assertions.assertThat(candle.getOpen()).isEmpty();

    candle =
        BasicOHLCV.ofDecimal(LocalDate.of(2025, 12, 10), (BigDecimal) null, null, null, null, null);
    Assertions.assertThat(candle.getOpenAsDecimal()).isEmpty();
    Assertions.assertThat(candle.getOpen()).isEmpty();
    Assertions.assertThat(candle.getHighAsDecimal()).isEmpty();
    Assertions.assertThat(candle.getHigh()).isEmpty();
    Assertions.assertThat(candle.getLowAsDecimal()).isEmpty();
    Assertions.assertThat(candle.getLow()).isEmpty();
    Assertions.assertThat(candle.getCloseAsDecimal()).isEmpty();
    Assertions.assertThat(candle.getClose()).isEmpty();
    Assertions.assertThat(candle.getVolumeAsDecimal()).isEmpty();
    Assertions.assertThat(candle.getVolume()).isEmpty();
  }

  @Test
  public void testSort() {
    var list = getSampleGOOG();

    var shuffled = Lists.newArrayList(list);
    Collections.shuffle(shuffled);

    Assertions.assertThat(shuffled).containsExactlyInAnyOrderElementsOf(list);

    var sorted = list.stream().sorted().toList();

    Assertions.assertThat(list).containsExactlyElementsOf(sorted);
  }

  @Test
  public void testLeap() {
    Assertions.assertThat(LocalDate.of(2020, 2, 29).minusYears(1))
        .isEqualTo(LocalDate.of(2019, 2, 28));
  }
}
