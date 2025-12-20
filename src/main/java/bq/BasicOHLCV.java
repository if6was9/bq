package bq;

import bx.util.Zones;
import com.google.common.base.MoreObjects;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public class BasicOHLCV implements OHLCV {

  Instant ts;
  BigDecimal open;
  BigDecimal high;
  BigDecimal low;
  BigDecimal close;
  BigDecimal volume;
  Long id;

  public static OHLCV of(
      LocalDate t, Double open, Double high, Double low, Double close, Double vol) {

    return of(t, open, high, low, close, vol, null);
  }

  public static OHLCV of(
      LocalDate t, Double open, Double high, Double low, Double close, Double vol, Long id) {
    return of(
        t.atStartOfDay(Zones.UTC).toInstant(),
        open != null ? new BigDecimal(open.toString()) : null,
        high != null ? new BigDecimal(high.toString()) : null,
        low != null ? new BigDecimal(low.toString()) : null,
        close != null ? new BigDecimal(close.toString()) : null,
        vol != null ? new BigDecimal(vol.toString()) : null,
        id);
  }

  public static OHLCV of(
      LocalDate t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal vol) {
    return of(t.atStartOfDay(Zones.UTC).toInstant(), open, high, low, close, vol, null);
  }

  public static OHLCV of(
      LocalDate t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal vol,
      Long id) {
    return of(t.atStartOfDay(Zones.UTC).toInstant(), open, high, low, close, vol, id);
  }

  public static OHLCV of(
      Instant t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal volume) {
    return of(t, open, high, low, close, volume, null);
  }

  public static OHLCV of(
      Instant t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal volume,
      Long id) {
    BasicOHLCV b = new BasicOHLCV();

    b.ts = t;
    b.open = open;
    b.high = high;
    b.low = low;
    b.close = close;
    b.volume = volume;
    b.id = id;

    return b;
  }

  public LocalDate getDate() {
    return getTimestamp().atZone(Zones.UTC).toLocalDate();
  }

  @Override
  public Instant getTimestamp() {
    return ts;
  }

  @Override
  public Optional<BigDecimal> getOpen() {
    return Optional.ofNullable(open);
  }

  @Override
  public Optional<BigDecimal> getHigh() {
    return Optional.ofNullable(high);
  }

  @Override
  public Optional<BigDecimal> getLow() {
    return Optional.ofNullable(low);
  }

  @Override
  public Optional<BigDecimal> getVolume() {
    return Optional.ofNullable(volume);
  }

  public Optional<BigDecimal> getClose() {
    return Optional.ofNullable(close);
  }

  public String toString() {

    String datePart = null;
    if (ts.atZone(Zones.UTC).toLocalDate().atStartOfDay(Zones.UTC).toEpochSecond()
        == ts.getEpochSecond()) {
      datePart = ts.atZone(Zones.UTC).toLocalDate().toString();
    } else {
      datePart = ts.toString();
    }
    var h =
        MoreObjects.toStringHelper("OLHCV")
            .add("date", datePart)
            .add("o", getOpen().orElse(null))
            .add("h", getHigh().orElse(null))
            .add("l", getLow().orElse(null))
            .add("c", getClose().orElse(null))
            .add("v", getVolume().orElse(null));

    getId()
        .ifPresent(
            id -> {
              h.add("id", id);
            });
    return h.toString();
  }

  public Optional<Long> getId() {
    return Optional.ofNullable(id);
  }

  @Override
  public int compareTo(Object o) {
    if (o == null || (!(o instanceof OHLCV))) {
      return 1;
    }

    OHLCV other = (OHLCV) o;
    return getDate().compareTo(other.getDate());
  }
}
