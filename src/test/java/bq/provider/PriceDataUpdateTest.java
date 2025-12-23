package bq.provider;

import java.util.List;
import org.junit.jupiter.api.Test;

public class PriceDataUpdateTest {

  @Test
  public void testIt() {

    PriceDataUpdate.forExistingData("BTC", List.of()).findMissingData();
  }
}
