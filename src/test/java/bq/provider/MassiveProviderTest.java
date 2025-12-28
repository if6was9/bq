package bq.provider;

import bq.BqTest;
import bx.sql.duckdb.DuckTable;
import bx.util.Slogger;
import com.google.common.base.Stopwatch;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class MassiveProviderTest extends BqTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testRequestDefault() {
    var cb = new CoinbaseDataProvider();
    Assertions.assertThat(cb.newRequest().isUnclosedPeriodIncluded()).isTrue();

    Assertions.assertThat(cb.newRequest().getFrom().isPresent()).isFalse();
    Assertions.assertThat(cb.newRequest().getTo().isPresent()).isFalse();
  }

  @Test
  public void testIt() {

    MassiveProvider p = new MassiveProvider().dataSource(getDataSource());
    DuckTable t = p.newRequest("GOOG").fetchIntoTable();

    t.prettyQuery().select();

    t.prettyQuery().select(c -> c.sql("select * from " + t.getTableName()));
  }

  @Test
  public void testRange2() {

    MassiveProvider p = new MassiveProvider().dataSource(getDataSource());

    LocalDate from = null;
    LocalDate to = LocalDate.of(2025, 12, 10);
    var list = p.newRequest("ABNB").from(from).to(to).fetchStream().toList();

    System.out.println(list.getFirst());

    System.out.println(list.getLast());
  }

  @Test
  public void testRange() {

    MassiveProvider p = new MassiveProvider().dataSource(getDataSource());

    LocalDate from = LocalDate.of(2025, 12, 1);
    LocalDate to = LocalDate.of(2025, 12, 10);
    var list = p.newRequest("GOOG").from(from).to(to).fetchStream().toList();

    Assertions.assertThat(list).hasSize(8);
    Assertions.assertThat(list.get(0).getDate()).isEqualTo(from);
    Assertions.assertThat(list.get(7).getDate()).isEqualTo(to);
  }

  @Test
  public void testCache() {
    var massive = new MassiveProvider();
    massive.invalidateAll();

    Stopwatch sw = Stopwatch.createStarted();
    massive.newRequest("GOOG").from(2020, 1, 1).fetchStream().forEach(it -> {});
    long uncachedMs = sw.elapsed(TimeUnit.MILLISECONDS);

    logger.atInfo().log("uncached {}ms", uncachedMs);

    sw = Stopwatch.createStarted();
    massive.newRequest("GOOG").from(2020, 1, 1).fetchStream().forEach(it -> {});
    long cachedMs = sw.elapsed(TimeUnit.MILLISECONDS);

    logger.atInfo().log("cached {}ms", cachedMs);

    double speedup = ((double) uncachedMs) / ((cachedMs > 0) ? cachedMs : 1);

    logger.atInfo().log("cache speedup: {}x", speedup);

    // cache speedup is not as big for Massive as Coinbase because their page sizes are much bigger
    // so large date ranges can be fetched in one request
    Assertions.assertThat(speedup).withFailMessage("cache speedup should be >2x").isGreaterThan(2);
  }
}
