package bq.ta4j;

import bq.BasicOHLCV;
import bq.BqTest;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class IndexedBarTest extends BqTest {

  @Test
  public void testIt() {

    var ohlcv = BasicOHLCV.of(LocalDate.of(2025, 7, 1), 1.0d, null, null, null, null);

    Assertions.assertThat(ohlcv.toString()).contains("date=2025-07-01");

    var bar = Bars.toBar(ohlcv);

    Assertions.assertThat(bar.toString()).contains("date=2025-07-01");

    // Assertions.assertThat(bar.getBeginTime().toString()).isEqualTo("");
  }
}
