package bq.indicator.btc;

import bx.util.Slogger;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class BtcUtilTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testXX() {
    Assertions.assertThat(BtcUtil.getStartOfEpoch(1)).isEqualTo(LocalDate.of(2009, 1, 3));
  }

  @Test
  public void testGetDaysSinceGenesis() {
    Assertions.assertThat(BtcUtil.getDaysSinceGenesis(BtcUtil.GENESIS_DATE)).isEqualTo(0);
    Assertions.assertThat(BtcUtil.getDaysSinceGenesis(BtcUtil.GENESIS_DATE.minusDays(1)))
        .isEqualTo(-1);
    Assertions.assertThat(BtcUtil.getDaysSinceGenesis(BtcUtil.GENESIS_DATE.plusDays(1)))
        .isEqualTo(1);

    for (int i = 0; i < 6000; i++) {
      LocalDate d = BtcUtil.GENESIS_DATE.plusDays(i);
      Assertions.assertThat(BtcUtil.getDaysSinceGenesis(d)).isEqualTo(i);
    }
    Assertions.assertThat(BtcUtil.getDaysSinceGenesis(LocalDate.of(2024, 1, 1))).isEqualTo(5476);
  }

  @Test
  public void testGetEpochDay() {
    for (int epoch = 1; epoch < 10; epoch++) {

      LocalDate startOfEpoch = BtcUtil.getStartOfEpoch(epoch);
      logger.atDebug().log("testing epoch={} start={}", epoch, startOfEpoch);

      Assertions.assertThat(BtcUtil.getDayOfEpoch(BtcUtil.GENESIS_DATE)).isEqualTo(0);
      for (int i = 0; i < 2000; i++) {
        Assertions.assertThat(BtcUtil.getDayOfEpoch(BtcUtil.GENESIS_DATE.minusDays(i)))
            .isEqualTo(0);
      }

      if (epoch > 1) {
        Assertions.assertThat(BtcUtil.getDayOfEpoch(startOfEpoch.minusDays(1))).isGreaterThan(1300);
      }
      Assertions.assertThat(BtcUtil.getDayOfEpoch(startOfEpoch)).isEqualTo(0);
      Assertions.assertThat(BtcUtil.getDayOfEpoch(startOfEpoch.plusDays(1))).isEqualTo(1);
      for (int i = 0; i < 1200; i++) {
        Assertions.assertThat(BtcUtil.getDayOfEpoch(startOfEpoch.plusDays(i))).isEqualTo(i);
      }
      Assertions.assertThat(BtcUtil.getDayOfEpoch(startOfEpoch.plusDays(1))).isEqualTo(1);
    }
  }

  @Test
  public void testStartOfEpoch() {

    Assertions.assertThat(BtcUtil.getStartOfEpoch(1)).isEqualTo(LocalDate.of(2009, 1, 3));
    Assertions.assertThat(BtcUtil.getStartOfEpoch(2)).isEqualTo(LocalDate.of(2012, 11, 28));

    for (int epoch = 1; epoch < BtcUtil.getEpochStartDates().size(); epoch++) {

      LocalDate d = BtcUtil.getStartOfEpoch(epoch);

      Assertions.assertThat(BtcUtil.getEpoch(d)).isEqualTo(epoch);
    }

    Assertions.assertThat(BtcUtil.getStartOfEpoch(-1)).isEqualTo(LocalDate.EPOCH);
    Assertions.assertThat(BtcUtil.getStartOfEpoch(0)).isEqualTo(LocalDate.EPOCH);
  }

  @Test
  public void testMethods() {

    // thre is no 0 epoch
    Assertions.assertThat(BtcUtil.getEpoch(LocalDate.of(2009, 1, 2))).isEqualTo(0);

    Assertions.assertThat(BtcUtil.getEpoch(LocalDate.of(2009, 1, 3))).isEqualTo(1);
    Assertions.assertThat(BtcUtil.getEpoch(LocalDate.of(2010, 1, 5))).isEqualTo(1);
    Assertions.assertThat(BtcUtil.getEpoch(LocalDate.of(2009, 1, 3))).isEqualTo(1);

    Assertions.assertThat(BtcUtil.getEpoch(LocalDate.of(2012, 11, 27))).isEqualTo(1);

    Assertions.assertThat(BtcUtil.getEpoch(LocalDate.of(2012, 11, 28))).isEqualTo(2);
    Assertions.assertThat(BtcUtil.getEpoch(LocalDate.of(2012, 11, 29))).isEqualTo(2);

    for (int i = 0; i < BtcUtil.getEpochStartDates().size(); i++) {
      int epoch = i + 1;
      Assertions.assertThat(BtcUtil.getEpoch(BtcUtil.getEpochStartDates().get(i))).isEqualTo(epoch);
      if (epoch > 1) {
        Assertions.assertThat(BtcUtil.getEpoch(BtcUtil.getEpochStartDates().get(i - 1)))
            .isEqualTo(epoch - 1);
      }
    }
  }

  @Test
  public void testEpochStartDate() {
    Assertions.assertThat(BtcUtil.getHalvingDates()).doesNotContain(BtcUtil.GENESIS_DATE);
    Assertions.assertThat(BtcUtil.getEpochStartDates()).contains(BtcUtil.GENESIS_DATE);
  }

  @Test
  public void testBlockReward() {

    // https://en.bitcoin.it/wiki/Controlled_supply
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(0)).isEqualByComparingTo("0");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(1)).isEqualByComparingTo("50");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(2)).isEqualByComparingTo("25");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(3)).isEqualByComparingTo("12.5");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(4)).isEqualByComparingTo("6.25");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(5)).isEqualByComparingTo("3.125");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(6)).isEqualByComparingTo("1.56250");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(7)).isEqualByComparingTo("0.781250");

    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(10)).isEqualByComparingTo("0.09765625");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(14)).isEqualByComparingTo("0.00610351");

    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(19)).isEqualByComparingTo("0.00019073");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(20)).isEqualByComparingTo("0.00009536");

    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(33)).isEqualByComparingTo("0.00000001");
    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(34)).isEqualByComparingTo("0.0");

    Assertions.assertThat(BtcUtil.getBlockRewardForEpoch(-1)).isEqualByComparingTo("0");

    Assertions.assertThat(BtcUtil.getBlockRewardForBlock(0)).isEqualByComparingTo("50");
    for (int i = 1; i < 15; i++) {
      logger.atInfo().log("epoch={} reward={}", i, BtcUtil.getBlockRewardForEpoch(i));
    }

    Assertions.assertThat(BtcUtil.getBlockReward(LocalDate.now())).isEqualByComparingTo("3.125");
    Assertions.assertThat(BtcUtil.getBlockReward(LocalDate.of(2009, 1, 3)))
        .isEqualByComparingTo("50");
    Assertions.assertThat(BtcUtil.getBlockReward(LocalDate.of(2009, 1, 2)))
        .isEqualByComparingTo("0");
    Assertions.assertThat(BtcUtil.getBlockReward(LocalDate.of(2000, 1, 2)))
        .isEqualByComparingTo("0");
  }

  @Test
  public void testIt() {

    Assertions.assertThat(BtcUtil.getEpoch(420000)).isEqualTo(3);
    Assertions.assertThat(BtcUtil.getEpoch(210000)).isEqualTo(2);
    Assertions.assertThat(BtcUtil.getEpoch(209999)).isEqualTo(1);
    Assertions.assertThat(BtcUtil.getEpoch(0)).isEqualTo(1);
    Assertions.assertThat(BtcUtil.getEpoch(1)).isEqualTo(1);
  }
}
