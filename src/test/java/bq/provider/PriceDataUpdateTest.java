package bq.provider;

import bq.BqTest;
import bq.DataManager;
import bq.PriceTable;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

public class PriceDataUpdateTest extends BqTest {

  @Test
  public void testIt() {

    DataSource dataSource = getDataSource();
    DataProviders.get().dataSource(dataSource);
    DataManager dm = new DataManager().dataSource(dataSource);

    PriceTable t = dm.createOHLCV("btc", false);
    t.show();

    var pdu = new PriceDataUpdate().symbol("BTC").table(t);

    pdu.refresh();

    pdu.refresh();

    t.show();
  }
}
