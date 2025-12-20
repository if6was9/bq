package bq;

import bq.ta4j.Bars;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PriceTableTest extends BqTest {

  @Test
  public void testIt() {

    PriceTable pt = PriceTable.from(loadBtcPriceData("btc"));

    pt.getDuckTable().getJdbcClient().sql("delete from btc where date<'2025-01-15'").update();
    Assertions.assertThat(pt.getDuckTable()).isNotNull();

    var bs = pt.loadBarSeries();

    System.out.println(bs);

    Bars.toStream(bs)
        .forEach(
            it -> {
              System.out.println(it);
            });
  }
}
