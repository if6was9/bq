package bq;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TickerTest {

  @Test
  public void testIt() {
    Assertions.assertThat(Ticker.of("s:goog").isStock()).isTrue();
    Assertions.assertThat(Ticker.of("s:goog").getSymbol()).isEqualTo("GOOG");
    Assertions.assertThat(Ticker.of("x:btc").isCrypto()).isTrue();
    Assertions.assertThat(Ticker.of("x:btc").getSymbol()).isEqualTo("BTC");

    Assertions.assertThat(Ticker.of("I:SPX").isIndex()).isTrue();
    Assertions.assertThat(Ticker.of("I:SPX").getSymbol()).isEqualTo("SPX");

    Assertions.assertThat(Ticker.of("s_goog").isStock()).isTrue();
    Assertions.assertThat(Ticker.of("s_goog").getSymbol()).isEqualTo("GOOG");
    Assertions.assertThat(Ticker.of("x_btc").isCrypto()).isTrue();
    Assertions.assertThat(Ticker.of("x_btc").getSymbol()).isEqualTo("BTC");

    Assertions.assertThat(Ticker.of("I_SPX").isIndex()).isTrue();
    Assertions.assertThat(Ticker.of("I_SPX").getSymbol()).isEqualTo("SPX");
  }

  @Test
  public void testGuess() {

    Assertions.assertThat(Ticker.of("SOL").isCrypto()).isTrue();
    Assertions.assertThat(Ticker.of("WIF").isCrypto()).isTrue();
    Assertions.assertThat(Ticker.of("RUT").isIndex()).isTrue();
    Assertions.assertThat(Ticker.of("SPX").isIndex()).isTrue();
    Assertions.assertThat(Ticker.of("NQ").isIndex()).isTrue();
    Assertions.assertThat(Ticker.of("DJIA").isIndex()).isTrue();
    Assertions.assertThat(Ticker.of("RENDER").isCrypto()).isTrue();
    Assertions.assertThat(Ticker.of("render").getSymbol()).isEqualTo("RENDER");

    Assertions.assertThat(Ticker.of("S:SOL").isStock()).isTrue();
  }

  @Test
  public void testNullEmpty() {
    try {
      Ticker.of("");
      Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException expected) {
    }

    try {
      Ticker.of("");
      Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException expected) {
    }

    try {
      Ticker.of(" ");
      Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException expected) {
    }

    try {
      Ticker.of(":ADA");
      Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException expected) {
    }
    try {
      Ticker.of("X:");
      Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException expected) {
    }
  }
}
