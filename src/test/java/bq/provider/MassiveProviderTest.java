package bq.provider;

import bq.BqTest;
import bx.sql.duckdb.DuckTable;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MassiveProviderTest extends BqTest {

  @Test
  public void testIt() {

    MassiveProvider p = new MassiveProvider().dataSource(getDataSource());
    DuckTable t = p.forSymbol("GOOG").fetchIntoTable();

    t.prettyQuery().select();

    t.prettyQuery().select(c -> c.sql("select * from " + t.getTableName()));
  }

  @Test
  public void testRange() {

    MassiveProvider p = new MassiveProvider().dataSource(getDataSource());

    LocalDate from = LocalDate.of(2025, 12, 1);
    LocalDate to = LocalDate.of(2025, 12, 10);
    var list = p.forSymbol("GOOG").from(from).to(to).fetchStream().toList();

    Assertions.assertThat(list).hasSize(8);
    Assertions.assertThat(list.get(0).getDate()).isEqualTo(from);
    Assertions.assertThat(list.get(7).getDate()).isEqualTo(to);
  }
}
