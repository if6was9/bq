package bq.command;

import bq.BqTest;
import bx.sql.duckdb.DuckS3Extension;
import org.junit.jupiter.api.Test;

public class RefreshCommandTest extends BqTest {

  @Test
  public void testIt() {

    DuckS3Extension.load(getDataSource()).useCredentialChain();
    RefreshCommand c =
        new RefreshCommand().dataSource(getDataSource()).bucket(getTestS3BucketName());
    // c.run();

  }
}
