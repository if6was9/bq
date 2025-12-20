package bq;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public interface OHLCV extends Comparable {

  LocalDate getDate();

  Instant getTimestamp();

  Optional<BigDecimal> getOpen();

  Optional<BigDecimal> getHigh();

  Optional<BigDecimal> getLow();

  Optional<BigDecimal> getClose();

  Optional<BigDecimal> getVolume();

  Optional<Long> getId();

  default Optional<Double> optDouble(Optional<BigDecimal> bd) {
    if (bd == null) {
      return Optional.empty();
    }
    if (bd.isPresent()) {
      return Optional.of(bd.get().doubleValue());
    }
    return Optional.empty();
  }

  public default Optional<Double> getOpenAsDouble() {

    return optDouble(getOpen());
  }

  public default Optional<Double> getHighAsDouble() {

    return optDouble(getHigh());
  }

  public default Optional<Double> getLowAsDouble() {

    return optDouble(getLow());
  }

  public default Optional<Double> getCloseAsDouble() {

    return optDouble(getClose());
  }

  public default Optional<Double> getVolumeAsDouble() {

    return optDouble(getVolume());
  }
}
