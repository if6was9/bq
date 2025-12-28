package bq.ta4j;

import bx.util.Zones;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.ta4j.core.BaseBar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

public class IndexedBar extends BaseBar {

  Long id;

  public IndexedBar(
      Duration timePeriod,
      Instant beginTime,
      Instant endTime,
      Num openPrice,
      Num highPrice,
      Num lowPrice,
      Num closePrice,
      Num volume,
      Long id) {
    super(
        timePeriod,
        beginTime,
        endTime,
        openPrice,
        highPrice,
        lowPrice,
        closePrice,
        volume,
        null,
        0);
    this.id = id;
  }

  public static IndexedBar create(
      LocalDate d, Double open, Double high, Double low, Double close, Double volume, Long index) {

    return new IndexedBar(
        Duration.ofDays(1),
        d.atStartOfDay(Zones.UTC).toInstant(),
        null,
        open != null ? DoubleNum.valueOf(open) : null,
        high != null ? DoubleNum.valueOf(high) : null,
        low != null ? DoubleNum.valueOf(low) : null,
        close != null ? DoubleNum.valueOf(close) : null,
        volume != null ? DoubleNum.valueOf(volume) : null,
        index);
  }

  public static IndexedBar create(
      Duration timePeriod,
      Instant beginTime,
      Instant endTime,
      Double open,
      Double high,
      Double low,
      Double close,
      Double volume,
      Long index) {

    return new IndexedBar(
        timePeriod,
        beginTime,
        endTime,
        open != null ? DoubleNum.valueOf(open) : null,
        high != null ? DoubleNum.valueOf(high) : null,
        low != null ? DoubleNum.valueOf(low) : null,
        close != null ? DoubleNum.valueOf(close) : null,
        volume != null ? DoubleNum.valueOf(volume) : null,
        index);
  }

  public Long getId() {
    return id;
  }

  public LocalDate getDate() {
    return getBeginTime().atZone(Zones.UTC).toLocalDate();
  }

  public String toString() {
    ToStringHelper h =
        MoreObjects.toStringHelper("Bar")
            .add("date", getDate())
            .add("open", getOpenPrice())
            .add("high", getHighPrice())
            .add("low", getLowPrice())
            .add("close", getClosePrice())
            .add("volume", getVolume());
    if (id != null) {
      h.add("id", id);
    }
    return h.toString();
  }
}
