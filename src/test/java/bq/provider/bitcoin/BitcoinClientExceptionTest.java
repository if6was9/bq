package bq.provider.bitcoin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class BitcoinClientExceptionTest {

  @Test
  public void testIt() {
    Assertions.assertThat(new BitcoinClientException(401).getMessage())
        .isEqualTo("(httpStatus=401)");
    Assertions.assertThat(new BitcoinClientException(401, " a problem ").getMessage())
        .isEqualTo("a problem (httpStatus=401)");
    Assertions.assertThat(new BitcoinClientException(401, " a problem ", 123).getMessage())
        .isEqualTo("a problem (code=123) (httpStatus=401)");

    Assertions.assertThat(new BitcoinClientException(401, null, 123).getMessage())
        .isEqualTo("(code=123) (httpStatus=401)");
  }
}
