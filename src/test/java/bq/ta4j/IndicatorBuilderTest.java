package bq.ta4j;

import bq.BqTest;
import bx.util.Json;
import bx.util.Slogger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.averages.EMAIndicator;
import org.ta4j.core.indicators.averages.SMAIndicator;
import org.ta4j.core.indicators.helpers.CloseLocationValueIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceRatioIndicator;
import org.ta4j.core.indicators.helpers.ConstantIndicator;
import org.ta4j.core.indicators.helpers.ConvergenceDivergenceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.GainIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

public class IndicatorBuilderTest extends BqTest {

  static org.slf4j.Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testIt() {
    IndicatorBuilder b = new IndicatorBuilder();

    var j = b.parseTree("123");

    Assertions.assertThat(j.isArray()).isTrue();
    Assertions.assertThat(j.size()).isEqualTo(1);
    Assertions.assertThat(j.path(0).asInt()).isEqualTo(123);

    Assertions.assertThat(b.parseTree("").isArray()).isTrue();
    Assertions.assertThat(b.parseTree("").size()).isEqualTo(0);
    Assertions.assertThat(b.parseTree(" ").isArray()).isTrue();
    Assertions.assertThat(b.parseTree(" ").size()).isEqualTo(0);

    Assertions.assertThat(b.parseTree("()").isArray()).isTrue();
    Assertions.assertThat(b.parseTree("()").size()).isEqualTo(0);
    Assertions.assertThat(b.parseTree(" ( ) ").isArray()).isTrue();
    Assertions.assertThat(b.parseTree(" ( ) ").size()).isEqualTo(0);

    j = b.parseTree("(1)");
    Assertions.assertThat(j.size()).isEqualTo(1);
    Assertions.assertThat(j.path(0).asInt()).isEqualTo(1);

    j = b.parseTree("( 1 )");
    Assertions.assertThat(j.size()).isEqualTo(1);
    Assertions.assertThat(j.path(0).asInt()).isEqualTo(1);

    j = b.parseTree("( 1,2 )");
    Assertions.assertThat(j.size()).isEqualTo(2);
    Assertions.assertThat(j.path(0).asInt()).isEqualTo(1);
    Assertions.assertThat(j.path(1).asInt()).isEqualTo(2);

    j = b.parseTree("( 1,2.2 )");
    Assertions.assertThat(j.size()).isEqualTo(2);
    Assertions.assertThat(j.path(0).asInt()).isEqualTo(1);
    Assertions.assertThat(j.path(1).asDouble()).isEqualTo(2.2);

    j = b.parseTree("close");

    Assertions.assertThat(j.path(0).path("fn").asString("")).isEqualTo("close");

    j = b.parseTree("close()");

    Assertions.assertThat(j.path(0).path("fn").asString("")).isEqualTo("close");

    j = b.parseTree("sma(close,20)");

    Assertions.assertThat(j.path(0).path("fn").asString("")).isEqualTo("sma");
    Assertions.assertThat(j.path(0).path("args").get(0).path("fn").asString("")).isEqualTo("close");
    Assertions.assertThat(j.path(0).path("args").get(1).asInt()).isEqualTo(20);
  }

  @Test
  public void testClosePrice() {

    BarSeries series = Bars.toBarSeries(getSampleGOOG().stream());

    String input =
        """
        		[ {
        "fn" : "close_price"} ]
        """;

    var json = Json.readTree(input);

    Indicator<Num> indicator = new IndicatorBuilder().create(json, series);
    Assertions.assertThat(indicator).isExactlyInstanceOf(ClosePriceIndicator.class);
    Assertions.assertThat(indicator.getBarSeries()).isEqualTo(series);

    for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
      Assertions.assertThat(indicator.getValue(i).doubleValue())
          .isEqualByComparingTo(series.getBar(i).getClosePrice().doubleValue());
    }
  }

  void checkSameOutput(String expression, Indicator<Num> refIndicator) {
    BarSeries barSeries = refIndicator.getBarSeries();

    IndicatorBuilder b = new IndicatorBuilder();
    Indicator<Num> testIndicator = b.create(expression, barSeries);

    for (int i = barSeries.getBeginIndex(); i <= barSeries.getEndIndex(); i++) {
      Num refNum = refIndicator.getValue(i);
      Num testNum = testIndicator.getValue(i);
      Assertions.assertThat(testNum.doubleValue())
          .withFailMessage("expression=%s indicator=%s index=%d", expression, testIndicator, i)
          .isEqualByComparingTo(refNum.doubleValue());
    }
  }

  @Test
  public void testSMA() {

    BarSeries series = Bars.toBarSeries(getSampleGOOG().stream());

    String input =
        """
        		[ {
        "fn" : "sma", "args":[5]} ]
        """;

    var json = Json.readTree(input);

    Indicator<Num> refIndicator = new SMAIndicator(new ClosePriceIndicator(series), 5);
    Indicator<Num> indicator = new IndicatorBuilder().create(json, series);
    Assertions.assertThat(indicator).isExactlyInstanceOf(SMAIndicator.class);
    Assertions.assertThat(indicator.getBarSeries()).isEqualTo(series);

    for (int i = series.getBeginIndex(); i <= series.getEndIndex(); i++) {
      Assertions.assertThat(indicator.getValue(i).doubleValue())
          .isEqualByComparingTo(refIndicator.getValue(i).doubleValue());
    }
  }

  @Test
  public void testSame() {

    IndicatorBuilder b = new IndicatorBuilder();

    BarSeries series = Bars.toBarSeries(getSampleGOOG().stream());

    checkSameOutput("open_price", new OpenPriceIndicator(series));
    checkSameOutput("high_price", new HighPriceIndicator(series));
    checkSameOutput("low_price", new LowPriceIndicator(series));
    checkSameOutput("close_price", new ClosePriceIndicator(series));
    checkSameOutput("sma(20)", new SMAIndicator(new ClosePriceIndicator(series), 20));
    checkSameOutput("ema(close_price,20)", new EMAIndicator(new ClosePriceIndicator(series), 20));
    checkSameOutput("constant(10)", new ConstantIndicator(series, DoubleNum.valueOf(10)));
    checkSameOutput("close_location_value", new CloseLocationValueIndicator(series));
    checkSameOutput(
        "difference(close_price)", new DifferenceIndicator(new ClosePriceIndicator(series)));
    checkSameOutput("difference", new DifferenceIndicator(new ClosePriceIndicator(series)));
    checkSameOutput(
        "difference(high_price)", new DifferenceIndicator(new HighPriceIndicator(series)));

    checkSameOutput("close_price_ratio", new ClosePriceRatioIndicator(series));
    checkSameOutput("gain", new GainIndicator(new ClosePriceIndicator(series)));
    checkSameOutput("gain(close_price)", new GainIndicator(new ClosePriceIndicator(series)));

    // not supported yet

    ConvergenceDivergenceIndicator cdi;
  }
}
