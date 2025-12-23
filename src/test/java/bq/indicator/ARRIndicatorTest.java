package bq.indicator;

import bq.PriceTable;
import bq.chart.Chart;
import bx.sql.duckdb.DuckTable;
import bx.util.Slogger;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class ARRIndicatorTest extends IndicatorTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void aaaFirst() {}

  void checkResult(LocalDate t0, double p0, LocalDate t1, double arr, double p1) {
    double years = Math.abs(ChronoUnit.DAYS.between(t0, t1)) / 365d;

    double r = (arr / 100) + 1d;

    double out = Math.pow(r, years) * p0;

    Assertions.assertThat(p1).isCloseTo(out, Percentage.withPercentage(.1));
  }

  @Test
  public void testX() {

    ARRIndicator ind = new ARRIndicator(null);

    LocalDate t0 = LocalDate.now().minusYears(4);
    double p0 = 100;
    LocalDate t1 = LocalDate.now();
    double p1 = 80;

    double arr = ARRIndicator.calculateARR(t0, p0, t1, p1);

    checkResult(t0, p0, t1, arr, p1);
  }

  @Test
  public void applyIndicator() {
    DuckTable dt = getTestData().loadBtcPriceData("btc");

    dt.sql("delete from btc where date<'2016-01-01'").update();

    PriceTable bst = PriceTable.from(dt);

    ARRIndicator indicator = new ARRIndicator(bst.getBarSeries(), 4);

    bst.addIndicator("arr", indicator);

    bst.addIndicator("sma", "sma(arr,100)");

    Chart.newChart()
        .trace(
            "btc",
            trace -> {
              trace.addData("sma", bst);
            })
        .title("BTC Annualized Rate of Return (ARR)")
        .view();
  }

  @Test
  public void testPositive() {

    ARRIndicator ind = new ARRIndicator(null);

    LocalDate t0 = LocalDate.now().minusYears(20);
    double p0 = 100;
    LocalDate t1 = LocalDate.now();
    double p1 = 100;

    double arr = ARRIndicator.calculateARR(t0, p0, t1, p1);

    checkResult(t0, p0, t1, arr, p1);
  }
}
