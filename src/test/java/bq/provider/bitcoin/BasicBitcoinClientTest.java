package bq.provider.bitcoin;

import bq.BqTest;
import bx.util.Slogger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class BasicBitcoinClientTest extends BqTest {

  static Logger logger = Slogger.forEnclosingClass();
  static BitcoinClient client;
  static Exception skipReason;

  @BeforeEach
  public void setup() {

    Assumptions.assumeTrue(skipReason == null);
    if (skipReason != null) {
      return;
    }
    try {
      client = BasicBitcoinClient.create();
      client.getBlockChainInfo();
    } catch (Exception e) {
      logger.atWarn().setCause(e).log();
      skipReason = e;
      client = null;
    }

    Assumptions.assumeTrue(client != null);
  }

  @Test
  public void testIt() {

    System.out.println(client.getBlockChainInfo().toPrettyString());
  }

  @Test
  public void testInvalidMethod() {

    try {
      client.invoke("invalid_method");
    } catch (BitcoinClientException e) {
      Assertions.assertThat(e.getHttpStatus()).isEqualTo(200);
      Assertions.assertThat(e.getCode()).isEqualTo(-32601);
    }
  }

  @Test
  public void testOutOfRangeHigh() {

    try {

      client.getBlockHash(2000000);
      Assertions.failBecauseExceptionWasNotThrown(BitcoinClientException.class);
    } catch (BitcoinClientException e) {
      Assertions.assertThat(e.getCode()).isEqualTo(-8);
    }
  }

  @Test
  public void testOutOfRangeLow() {

    try {

      client.getBlockHash(-1);
      Assertions.failBecauseExceptionWasNotThrown(BitcoinClientException.class);
    } catch (BitcoinClientException e) {
      Assertions.assertThat(e.getCode()).isEqualTo(-8);
    }
  }

  @Test
  public void testTip() {
    int blocks = client.getBlockChainInfo().path("blocks").asInt();

    String hash = client.getBlockHash(blocks);

    Assertions.assertThat(hash).isNotBlank().hasSize(64);
  }

  @Test
  public void testIt2() {

    Assertions.assertThat(client.getBlockHash(0))
        .isEqualTo("000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f");
    Assertions.assertThat(client.getBlockHash(1))
        .isEqualTo("00000000839a8e6886ab5951d76f411475428afc90947ee320161bbf18eb6048");
  }
}
