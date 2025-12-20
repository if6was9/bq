package bq;

import bx.sql.PrettyQuery;
import bx.sql.duckdb.DuckDataSource;
import bx.sql.duckdb.DuckTable;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Preconditions;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.jdbc.core.simple.JdbcClient;

public abstract class BqTest {

  static Logger logger = Slogger.forEnclosingClass();

  private DuckDataSource dataSource;
  DataManager ddm;

  public DataManager getDuckDataManager() {
    return ddm;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public JdbcClient getJdbcClient() {
    return JdbcClient.create(getDataSource());
  }

  public PrettyQuery prettyQuery() {
    return PrettyQuery.with(getDataSource()).out(logger, Level.INFO);
  }

  public DuckTable loadBtcPriceData(String table) {

    getDuckDataManager().createOHLCV(table, true);
    String sql =
        String.format(
            "insert into %s (date, open, high,low,close,volume) (select"
                + " date,open,high,low,close,volume from '%s' order by date)",
            table, new File("src/test/resources/btc-price-data.csv").toString());

    int count = getJdbcClient().sql(sql).update();

    logger.atDebug().log("inserted {} rows", count);

    return DuckTable.of(getDataSource(), table);
  }

  @BeforeEach
  public void setup() {

    String name = System.getProperty("app.name");
    if (!S.notBlank(name).orElse("").equals("bq")) {
      System.setProperty("app.name", "bq");
      name = System.getProperty("app.name");
      Preconditions.checkState(S.notBlank(name).orElse("").equals("bq"));
    }
    logger.atTrace().log("setup");
    dataSource = DuckDataSource.createInMemory();

    ddm = new DataManager();
    ddm.dataSource(dataSource);
  }

  public List<OHLCV> getSampleGOOG() {

    List<OHLCV> list = Lists.newArrayList();

    // this seems silly, but it helps

    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 2), 191.485, 193.2, 188.71, 190.63, 1.7494462E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 3), 192.725, 194.5, 191.35, 193.13, 1.2874957E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 6), 195.15, 199.56, 195.06, 197.96, 1.9483323E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 7), 198.27, 202.14, 195.94, 196.71, 1.696676E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 8), 193.95, 197.64, 193.75, 195.39, 1.4335341E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 10), 195.42, 197.62, 191.6, 193.17, 2.0753778E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 13), 191.35, 192.49, 188.66, 192.29, 1.316906E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 14), 192.5, 193.27, 189.64, 191.05, 1.3651183E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 15), 194.35, 197.8, 193.33, 196.98, 1.2894875E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 1, 16), 195.825, 196.9799, 194.3, 194.41, 1.3449581E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 17), 198.05, 198.81, 195.31, 197.55, 2.2109129E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 21), 200.51, 203.84, 199.44, 199.63, 1.9005232E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 22), 200.55, 202.12, 199.2, 200.03, 1.5477376E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 23), 199.98, 201.94, 196.82, 199.58, 1.5170838E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 1, 24), 199.85, 202.57, 199.7842, 201.9, 1.2732376E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 27), 194.19, 198.67, 192.7, 193.77, 2.4970173E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 28), 194.65, 197.23, 192.61, 197.07, 1.5939161E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 1, 29), 197.37, 198.4595, 195.19, 197.18, 1.2282999E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 1, 30), 199.76, 203.2384, 199.4717, 202.63, 1.4571479E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 1, 31), 203.715, 207.08, 203.58, 205.6, 1.7087335E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 3), 202.215, 205.22, 201.66, 202.64, 1.6719469E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 4), 204.5, 208.7, 204.26, 207.71, 2.8282736E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 5), 193.1, 194.5499, 189.91, 193.3, 4.3719589E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 6), 190.99, 193.83, 190.49, 193.31, 2.0816593E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 7), 192.74, 193.015, 185.1, 187.14, 2.9565724E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 10), 189.06, 189.99, 187.61, 188.2, 1.660602E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 11), 186.835, 188.8, 186.08, 187.07, 1.30281E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 12), 185.23, 186.83, 183.63, 185.43, 1.7632314E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 13), 185.93, 187.99, 184.88, 187.88, 1.2729334E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 14), 186.83, 188.15, 186.11, 186.87, 1.2714154E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 18), 187.44, 187.78, 183.58, 185.8, 1.9796028E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 19), 186.185, 187.36, 185.5, 187.13, 1.3120465E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 20), 186.5, 187.12, 184.6, 186.64, 1.2063807E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 21), 187.29, 187.47, 181.13, 181.58, 1.9520782E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 24), 183.8, 185.09, 180.88, 181.19, 1.8734014E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 2, 25), 180.155, 180.76, 176.77, 177.37, 2.0832485E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 26), 176.945, 178.08, 173.59, 174.7, 2.3687651E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 27), 175.94, 176.59, 169.752, 170.21, 2.593053E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 2, 28), 170.3, 172.5, 168.39, 172.22, 3.0049812E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 3), 173.73, 175.0, 167.64, 168.66, 2.4121991E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 4), 167.94, 175.165, 167.54, 172.61, 3.0711408E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 5), 172.32, 175.75, 170.93, 174.99, 1.8748036E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 6), 172.55, 176.73, 172.508, 174.21, 1.9082404E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 7), 173.242, 176.9, 172.25, 175.75, 1.6195287E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 10), 170.16, 170.45, 165.565, 167.81, 2.8990724E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 11), 166.68, 168.655, 163.24, 165.98, 2.3705899E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 12), 168.47, 169.53, 165.48, 169.0, 1.9880062E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 13), 167.98, 168.12, 164.07, 164.73, 1.5206165E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 14), 165.315, 168.25, 164.51, 167.62, 1.8611094E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 17), 167.325, 168.46, 165.81, 166.57, 1.7839139E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 18), 165.96, 166.44, 158.8, 162.67, 2.4616784E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 19), 163.915, 168.1334, 163.05, 166.28, 2.4922917E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 20), 163.825, 167.03, 163.14, 165.05, 1.9981512E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 21), 163.38, 166.47, 163.03, 166.25, 2.9841314E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 24), 169.265, 170.5, 167.44, 169.93, 1.8742848E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 25), 171.18, 172.91, 170.55, 172.79, 1.3841592E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 26), 171.3, 171.9396, 166.8606, 167.14, 2.2554236E7));
    list.add(BasicOHLCV.of(LocalDate.of(2025, 3, 27), 166.71, 167.44, 163.85, 164.08, 2.1571174E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2025, 3, 28), 162.36, 163.81, 155.3394, 156.06, 3.4844462E7));

    return list;
  }

  @AfterEach
  public void tearDown() {
    logger.atTrace().log("closing: " + dataSource);
    dataSource.close();
  }
}
