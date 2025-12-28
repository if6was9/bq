package bq;

import bx.util.Zones;
import com.google.common.base.MoreObjects;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public class BasicOHLCV implements OHLCV {

  Instant ts;
  Double open;
  Double high;
  Double low;
  Double close;
  Double volume;
  Long id;

  public static OHLCV of(
      LocalDate t, Double open, Double high, Double low, Double close, Double vol, Long id) {
    return of(t.atStartOfDay(Zones.UTC).toInstant(), open, high, low, close, vol, id);
  }

  public static OHLCV of(
      LocalDate t, Double open, Double high, Double low, Double close, Double vol) {

    return of(t, open, high, low, close, vol, null);
  }

  public static OHLCV of(
      Instant t, Double open, Double high, Double low, Double close, Double vol) {
    return of(t, open, high, low, close, vol, null);
  }

  public static OHLCV of(
      Instant t, Double open, Double high, Double low, Double close, Double vol, Long id) {

    var bar = new BasicOHLCV();
    bar.ts = t;
    bar.open = open;
    bar.high = high;
    bar.low = low;
    bar.close = close;
    bar.volume = vol;
    bar.id = id;
    return bar;
  }

  public static OHLCV ofDecimal(
      LocalDate t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal vol) {
    return ofDecimal(t.atStartOfDay(Zones.UTC).toInstant(), open, high, low, close, vol, null);
  }

  public static OHLCV ofDecimal(
      LocalDate t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal vol,
      Long id) {
    return ofDecimal(t.atStartOfDay(Zones.UTC).toInstant(), open, high, low, close, vol, id);
  }

  public static OHLCV ofDecimal(
      Instant t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal volume) {
    return ofDecimal(t, open, high, low, close, volume, null);
  }

  public static OHLCV ofDecimal(
      Instant t,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal volume,
      Long id) {

    return BasicOHLCV.of(
        t,
        open != null ? open.doubleValue() : null,
        high != null ? high.doubleValue() : null,
        low != null ? low.doubleValue() : null,
        close != null ? close.doubleValue() : null,
        volume != null ? volume.doubleValue() : null,
        id);
  }

  public Optional<BigDecimal> getOpenAsDecimal() {
    return optDecimal(getOpen());
  }

  public Optional<BigDecimal> getHighAsDecimal() {
    return optDecimal(getHigh());
  }

  public Optional<BigDecimal> getLowAsDecimal() {
    return optDecimal(getLow());
  }

  public Optional<BigDecimal> getCloseAsDecimal() {
    return optDecimal(getClose());
  }

  public Optional<BigDecimal> getVolumeAsDecimal() {
    return optDecimal(getVolume());
  }

  public LocalDate getDate() {
    return getTimestamp().atZone(Zones.UTC).toLocalDate();
  }

  @Override
  public Instant getTimestamp() {
    return ts;
  }

  @Override
  public Optional<Double> getOpen() {
    return Optional.ofNullable(open);
  }

  @Override
  public Optional<Double> getHigh() {
    return Optional.ofNullable(high);
  }

  @Override
  public Optional<Double> getLow() {
    return Optional.ofNullable(low);
  }

  @Override
  public Optional<Double> getVolume() {
    return Optional.ofNullable(volume);
  }

  public Optional<Double> getClose() {
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
            .add("open", getOpenAsDecimal().orElse(null))
            .add("high", getHighAsDecimal().orElse(null))
            .add("low", getLowAsDecimal().orElse(null))
            .add("close", getCloseAsDecimal().orElse(null))
            .add("volume", getVolumeAsDecimal().orElse(null));

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
    return ts.compareTo(other.getTimestamp());
  }

  Optional<BigDecimal> optDecimal(Optional<Double> d) {
    if (d == null) {
      return Optional.empty();
    }
    if (d.isPresent()) {
      return Optional.of(BigDecimal.valueOf(d.get()));
    }
    return Optional.empty();
  }
}
