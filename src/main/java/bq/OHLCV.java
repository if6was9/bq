package bq;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public interface OHLCV extends Comparable {

  LocalDate getDate();

  Instant getTimestamp();

  public Optional<BigDecimal> getOpenAsDecimal();

  public Optional<BigDecimal> getHighAsDecimal();

  public Optional<BigDecimal> getLowAsDecimal();

  public Optional<BigDecimal> getCloseAsDecimal();

  public Optional<BigDecimal> getVolumeAsDecimal();

  Optional<Long> getId();

  public Optional<Double> getOpen();

  public Optional<Double> getHigh();

  public Optional<Double> getLow();

  public Optional<Double> getClose();

  public Optional<Double> getVolume();
}
