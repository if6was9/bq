package bq.ta4j;

import bq.BqTest;
import bx.util.Json;
import bx.util.Slogger;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Sets;
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

  @Test
  public void testShortCodes() {

    Set<String> expected = Sets.newHashSet();

    expected.add("acceleration_deceleration");
    expected.add("accumulation_distribution");
    expected.add("adx");
    expected.add("amount");
    expected.add("aroon_down");
    expected.add("aroon_oscillator");
    expected.add("aroon_up");
    expected.add("arr");
    expected.add("atma");
    expected.add("atr");
    expected.add("average");
    expected.add("awesome_oscillator");
    expected.add("bearish_engulfing");
    expected.add("bearish_harami");
    expected.add("bearish_marubozu");
    expected.add("binary_operation");
    expected.add("bollinger_band_width");
    expected.add("bollinger_bands_lower");
    expected.add("bollinger_bands_middle");
    expected.add("bollinger_bands_upper");
    expected.add("boolean_transform");
    expected.add("btc_day_of_epoch");
    expected.add("btc_days_since_genesis");
    expected.add("btc_epoch");
    expected.add("btc_mayer_multiple");
    expected.add("btc_pi_multiple");
    expected.add("btc_power_law_price");
    expected.add("btc_power_law_quantile");
    expected.add("bullish_engulfing");
    expected.add("bullish_harami");
    expected.add("bullish_marubozu");
    expected.add("cash_flow");
    expected.add("cci");
    expected.add("chaikin_money_flow");
    expected.add("chaikin_oscillator");
    expected.add("chandelier_exit_long");
    expected.add("chandelier_exit_short");
    expected.add("channel");
    expected.add("chop");
    expected.add("close_location_value");
    expected.add("close_price");
    expected.add("close_price_difference");
    expected.add("close_price_ratio");
    expected.add("cmo");
    expected.add("combine");
    expected.add("connors_rsi");
    expected.add("constant");
    expected.add("convergence_divergence");
    expected.add("coppock_curve");
    expected.add("correlation_coefficient");
    expected.add("covariance");
    expected.add("cross");
    expected.add("cumulative_pn_l");
    expected.add("date_time");
    expected.add("de_mark_pivot_point");
    expected.add("de_mark_reversal");
    expected.add("difference");
    expected.add("difference_percentage");
    expected.add("distance_from_ma");
    expected.add("dma");
    expected.add("doji");
    expected.add("donchian_channel_lower");
    expected.add("donchian_channel_middle");
    expected.add("donchian_channel_upper");
    expected.add("double_ema");
    expected.add("down_trend");
    expected.add("dpo");
    expected.add("dx");
    expected.add("edma");
    expected.add("ema");
    expected.add("fibonacci_reversal");
    expected.add("fisher");
    expected.add("fixed");
    expected.add("fixed_boolean");
    expected.add("fixed_num");
    expected.add("gain");
    expected.add("hammer");
    expected.add("hanging_man");
    expected.add("high_price");
    expected.add("highest_value");
    expected.add("hma");
    expected.add("ichimoku_chikou_span");
    expected.add("ichimoku_kijun_sen");
    expected.add("ichimoku_line");
    expected.add("ichimoku_senkou_span_a");
    expected.add("ichimoku_senkou_span_b");
    expected.add("ichimoku_tenkan_sen");
    expected.add("iii");
    expected.add("intra_day_momentum_index");
    expected.add("inverted_hammer");
    expected.add("jma");
    expected.add("kalman_filter");
    expected.add("kama");
    expected.add("keltner_channel_lower");
    expected.add("keltner_channel_middle");
    expected.add("keltner_channel_upper");
    expected.add("ki_jun_v2");
    expected.add("kri");
    expected.add("kst");
    expected.add("loss");
    expected.add("low_price");
    expected.add("lower_shadow");
    expected.add("lowest_value");
    expected.add("lsma");
    expected.add("lwma");
    expected.add("macd");
    expected.add("macdv");
    expected.add("mass_index");
    expected.add("mc_ginley_ma");
    expected.add("mean_deviation");
    expected.add("median_price");
    expected.add("minus_di");
    expected.add("minus_dm");
    expected.add("mma");
    expected.add("money_flow_index");
    expected.add("mvwap");
    expected.add("net_momentum");
    expected.add("num");
    expected.add("numeric");
    expected.add("nvi");
    expected.add("on_balance_volume");
    expected.add("open_price");
    expected.add("parabolic_sar");
    expected.add("pearson_correlation");
    expected.add("percent_b");
    expected.add("percent_rank");
    expected.add("percentage_change");
    expected.add("periodical_growth_rate");
    expected.add("pivot_point");
    expected.add("plus_di");
    expected.add("plus_dm");
    expected.add("ppo");
    expected.add("previous_value");
    expected.add("pvi");
    expected.add("pvo");
    expected.add("ravi");
    expected.add("real_body");
    expected.add("recent_swing_high");
    expected.add("recent_swing_low");
    expected.add("relative_volume_standard_deviation");
    expected.add("renko_down");
    expected.add("renko_up");
    expected.add("renko_x");
    expected.add("returns");
    expected.add("roc");
    expected.add("rocv");
    expected.add("rsi");
    expected.add("running_total");
    expected.add("rwi_high");
    expected.add("rwi_low");
    expected.add("schaff_trend_cycle");
    expected.add("sgma");
    expected.add("shooting_star");
    expected.add("sigma");
    expected.add("simple_linear_regression");
    expected.add("sma");
    expected.add("smma");
    expected.add("squeeze_pro");
    expected.add("standard_deviation");
    expected.add("standard_error");
    expected.add("standard_reversal");
    expected.add("stochastic");
    expected.add("stochastic_oscillator_d");
    expected.add("stochastic_oscillator_k");
    expected.add("stochastic_rsi");
    expected.add("streak");
    expected.add("sum");
    expected.add("super_trend");
    expected.add("super_trend_lower_band");
    expected.add("super_trend_upper_band");
    expected.add("three_black_crows");
    expected.add("three_white_soldiers");
    expected.add("time_segmented_volume");
    expected.add("tma");
    expected.add("tr");
    expected.add("trade_count");
    expected.add("trendline");
    expected.add("triple_ema");
    expected.add("true_strength_index");
    expected.add("typical_price");
    expected.add("ulcer_index");
    expected.add("unary_operation");
    expected.add("unstable");
    expected.add("up_trend");
    expected.add("upper_shadow");
    expected.add("variance");
    expected.add("vidya");
    expected.add("volume");
    expected.add("vwap");
    expected.add("vwma");
    expected.add("wilders_ma");
    expected.add("williams_r");
    expected.add("wma");
    expected.add("zlema");

    Set<String> found = new IndicatorBuilder().getIndicatorNameMap().keySet();

    Assertions.assertThat(found).containsAll(expected);
  }
}
