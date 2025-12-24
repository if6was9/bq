package bq;

import bq.provider.CoinbaseDataProvider;
import bq.ta4j.Bars;
import bx.sql.duckdb.DuckTable;
import bx.util.Slogger;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javax.sql.DataSource;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.ta4j.core.BarSeries;

public class TestData {

  static Logger logger = Slogger.forEnclosingClass();
  private DataSource dataSource;

  public TestData(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public DataManager getDataManager() {
    return new DataManager().dataSource(getDataSource());
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public JdbcClient getJdbcClient() {
    return JdbcClient.create(getDataSource());
  }

  public PriceTable createWGMITable(String table) {
    File csv = new File("./src/test/resources/WGMI.csv");

    DuckTable t = new DataManager().dataSource(getDataSource()).createOHLCV(table, true);
    String sql =
        String.format(
            "insert into %s (date, open, high,low,close,volume) (select"
                + " date,open,high,low,close,volume from '%s' order by date)",
            table, csv.toString());

    int count = getJdbcClient().sql(sql).update();

    logger.atDebug().log("inserted {} rows", count);

    return PriceTable.from(t);
  }

  public PriceTable createBTCTable(String table) {

    if (!DuckTable.of(getDataSource(), table).exists()) {
      getDataManager().createOHLCV(table, true);
    }
    return new CoinbaseDataProvider()
        .dataSource(getDataSource())
        .newRequest()
        .symbol("BTC")
        .from(2016, 1, 1)
        .toDaysAgo(0)
        .fetchIntoTable(table);
  }

  public static final List<OHLCV> GOOG_2024_OHLCV = createGOOG2024();
  public static final BarSeries GOOG_2024_BAR_SERIES = Bars.toBarSeries(GOOG_2024_OHLCV.stream());

  public List<OHLCV> getGOOG2024() {
    return createGOOG2024();
  }

  public BarSeries getGOOG2024BarSeries() {
    return Bars.toBarSeries(getGOOG2024().stream());
  }

  private static List<OHLCV> createGOOG2024() {

    List<OHLCV> list = Lists.newArrayList();
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 2), 139.6, 140.6147, 137.74, 139.56, 2.0071885E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 3), 138.6, 141.09, 138.43, 140.36, 1.8974308E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 4), 139.85, 140.635, 138.01, 138.04, 1.8253331E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 5), 138.352, 138.81, 136.85, 137.39, 1.5439475E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 8), 138.0, 140.64, 137.88, 140.53, 1.6545293E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 1, 9), 140.06, 142.7998, 139.79, 142.56, 1.9579667E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 10), 142.52, 144.525, 142.46, 143.8, 1.6641881E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 1, 11), 144.895, 146.66, 142.215, 143.67, 1.747113E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 12), 144.34, 144.74, 143.36, 144.24, 1.3998729E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 1, 16), 143.43, 145.84, 143.0564, 144.08, 1.9188189E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 17), 142.91, 143.41, 140.51, 142.89, 1.7884548E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 18), 143.44, 145.585, 143.35, 144.99, 1.88768E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 19), 146.305, 148.04, 145.8, 147.97, 2.7181032E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 1, 22), 148.71, 150.015, 147.58, 147.71, 2.1779232E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 23), 147.72, 148.86, 147.19, 148.68, 1.4113649E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 24), 150.29, 151.57, 149.84, 150.35, 1.9245031E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 25), 151.74, 154.76, 151.22, 153.64, 2.149512E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 26), 152.87, 154.11, 152.8, 153.79, 1.9494488E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 29), 153.64, 155.2, 152.92, 154.84, 2.0909258E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 1, 30), 154.01, 155.04, 152.775, 153.05, 2.6537134E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 1, 31), 145.39, 145.59, 141.55, 141.8, 4.3908584E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 1), 143.69, 144.62, 142.26, 142.71, 2.5526855E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 2), 140.89, 143.88, 138.17, 143.54, 4.2106127E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 5), 144.04, 146.67, 143.91, 144.93, 2.9254444E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 6), 145.96, 146.74, 144.52, 145.41, 2.1517655E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 7), 146.12, 147.0, 145.2103, 146.68, 2.1436126E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 8), 146.97, 147.61, 146.42, 147.22, 1.8241319E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 9), 147.95, 150.695, 147.43, 150.22, 2.1877693E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 12), 149.54, 150.59, 148.56, 148.73, 1.7236108E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 13), 146.07, 148.04, 145.11, 146.37, 1.8138482E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 2, 14), 147.37, 147.83, 145.555, 147.14, 1.6651824E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 15), 144.46, 144.76, 141.88, 143.94, 2.6724305E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 16), 144.21, 144.48, 141.52, 141.76, 2.1865118E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 20), 140.94, 143.3285, 140.8, 142.2, 1.8625589E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 21), 142.64, 143.98, 141.91, 143.84, 1.6499584E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 22), 146.12, 146.2, 144.01, 145.32, 2.3000707E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 2, 23), 144.97, 145.955, 144.79, 145.29, 1.4519434E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 26), 143.45, 143.84, 138.74, 138.75, 3.3513011E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 27), 139.41, 140.49, 138.5, 140.1, 2.2363981E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 28), 139.1, 139.28, 136.64, 137.43, 3.0628702E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 2, 29), 138.35, 139.95, 137.57, 139.78, 3.5485006E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 1), 139.61, 140.0, 137.975, 138.08, 2.8551525E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 4), 136.54, 136.63, 132.86, 134.2, 4.357151E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 5), 132.74, 134.02, 131.55, 133.78, 2.844755E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 6), 134.24, 134.74, 131.95, 132.56, 2.31752E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 7), 133.89, 135.82, 132.66, 135.24, 2.4107282E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 8), 135.035, 138.985, 134.8, 136.29, 2.649516E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 11), 137.07, 139.98, 137.07, 138.94, 2.2536365E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 12), 138.25, 140.28, 138.21, 139.62, 1.9019696E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 13), 140.06, 142.19, 140.01, 140.77, 1.9636999E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 14), 142.3, 144.73, 141.485, 144.34, 3.6117913E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 3, 15), 143.41, 144.34, 141.1301, 142.17, 4.1039494E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 18), 149.37, 152.93, 148.14, 148.48, 4.7676689E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 19), 148.98, 149.62, 147.01, 147.92, 1.7748367E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 3, 20), 148.79, 149.76, 147.665, 149.68, 1.7720246E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 3, 21), 150.32, 151.305, 148.0101, 148.74, 1.9843915E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 22), 150.24, 152.56, 150.09, 151.77, 1.9252925E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 25), 150.95, 151.456, 148.8, 151.15, 1.5114728E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 26), 151.24, 153.2, 151.03, 151.7, 1.9312694E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 3, 27), 152.145, 152.69, 150.13, 151.94, 1.6580364E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 3, 28), 152.0, 152.67, 151.33, 152.26, 2.1105628E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 1), 151.83, 157.0, 151.65, 156.5, 2.4469815E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 2), 154.75, 155.99, 153.46, 155.87, 1.7598064E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 4, 3), 154.92, 156.55, 154.1321, 156.37, 1.7266175E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 4), 155.08, 156.18, 151.88, 151.94, 2.4184842E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 5), 151.68, 154.84, 151.081, 153.94, 1.6297319E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 8), 154.015, 156.655, 153.99, 156.14, 1.664153E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 9), 157.35, 159.89, 156.64, 158.14, 2.153814E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 10), 157.88, 158.16, 156.2, 157.66, 1.6336974E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 4, 11), 158.34, 161.1199, 157.93, 160.79, 1.7841703E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 12), 159.405, 161.7, 158.6, 159.19, 1.6989765E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 15), 160.28, 160.83, 156.15, 156.33, 2.1140948E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 16), 155.64, 157.23, 155.05, 156.0, 1.5413201E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 4, 17), 157.19, 158.681, 156.135, 156.88, 1.6237752E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 4, 18), 156.925, 158.485, 156.21, 157.46, 1.4016065E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 19), 157.75, 157.99, 153.91, 155.72, 2.1479881E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 22), 156.01, 159.185, 155.66, 157.95, 1.724387E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 4, 23), 158.59, 160.48, 157.965, 159.92, 1.6100408E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 24), 159.09, 161.39, 158.82, 161.1, 1.9468194E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 4, 25), 153.36, 158.28, 152.768, 157.95, 3.6197789E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 26), 175.99, 176.42, 171.4, 173.69, 5.6300787E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 29), 170.77, 171.38, 167.06, 167.9, 3.5914561E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 4, 30), 167.38, 169.87, 164.5, 164.64, 2.9420755E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 1), 166.18, 168.81, 164.9, 165.57, 2.5204245E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 2), 166.67, 168.53, 165.69, 168.46, 1.7041119E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 3), 169.54, 169.85, 164.98, 168.99, 2.2767056E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 6), 169.22, 169.9, 167.89, 169.83, 1.5147906E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 7), 170.12, 173.47, 170.0, 172.98, 2.1102434E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 5, 8), 170.75, 171.9092, 170.522, 171.16, 1.4569858E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 9), 171.15, 172.44, 169.93, 171.58, 1.1937704E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 10), 169.69, 171.34, 167.91, 170.29, 1.8740458E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 13), 165.847, 170.95, 165.76, 170.9, 1.9648585E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 14), 171.59, 172.78, 170.42, 171.93, 1.8729463E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 5, 15), 172.3, 174.0459, 172.03, 173.88, 2.0958245E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 16), 174.6, 176.34, 174.05, 175.43, 1.7247311E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 5, 17), 175.55, 177.495, 174.98, 177.29, 1.6546353E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 5, 20), 177.31, 179.95, 177.225, 178.46, 1.7495122E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 21), 178.4, 179.82, 177.31, 179.54, 1.4706021E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 5, 22), 178.4, 178.852, 176.7801, 178.0, 1.6189404E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 23), 178.78, 179.91, 174.54, 175.06, 1.4928363E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 24), 176.52, 177.3044, 175.2, 176.33, 1.140356E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 28), 175.74, 178.51, 175.68, 178.02, 1.565534E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 29), 176.81, 178.23, 176.26, 177.4, 1.5023847E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 30), 176.69, 176.69, 173.23, 173.56, 1.8844036E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 5, 31), 173.4, 174.42, 170.97, 173.96, 2.8085151E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 3), 173.88, 175.86, 172.45, 174.42, 2.0742798E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 4), 174.45, 175.19, 173.22, 175.13, 1.4066602E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 5), 176.535, 177.97, 175.29, 177.07, 1.5233861E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 6), 177.43, 178.71, 177.21, 178.35, 1.4255818E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 7), 178.46, 179.42, 175.79, 175.95, 1.471627E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 10), 176.45, 178.47, 174.38, 176.63, 1.7122247E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 11), 177.72, 178.39, 175.44, 178.19, 1.4402401E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 12), 179.75, 182.08, 177.78, 179.56, 1.8600421E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 13), 177.84, 178.51, 176.66, 176.74, 1.5956941E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 6, 14), 175.852, 178.73, 175.852, 178.37, 1.2361571E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 17), 176.98, 179.92, 176.49, 178.78, 1.5272864E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 18), 178.79, 178.91, 175.62, 176.45, 1.5640257E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 20), 176.71, 178.74, 176.46, 177.71, 1.6753166E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 6, 21), 178.49, 182.5117, 178.06, 180.26, 5.9728019E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 24), 181.28, 182.08, 180.23, 180.79, 1.8198282E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 6, 25), 181.145, 185.75, 181.105, 185.58, 1.8917734E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 26), 184.2, 185.93, 183.99, 185.37, 1.3375715E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 6, 27), 185.645, 187.5, 185.45, 186.86, 1.3025656E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 6, 28), 185.72, 186.58, 183.325, 183.42, 2.3032362E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 1), 184.48, 185.34, 182.73, 184.49, 1.1815862E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 2), 183.47, 186.95, 183.06, 186.61, 1.2555545E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 3), 186.3, 187.62, 185.385, 187.39, 7409106.0));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 5), 187.32, 192.26, 187.32, 191.96, 1.4303361E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 7, 8), 191.365, 191.6791, 189.32, 190.48, 1.2097611E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 9), 191.75, 192.86, 190.23, 190.44, 1.0224925E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 10), 190.75, 193.31, 190.62, 192.66, 1.2047799E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 11), 191.34, 192.41, 186.82, 187.3, 1.6451981E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 12), 186.92, 188.69, 186.14, 186.78, 1.4449113E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 15), 186.49, 189.9, 186.49, 188.19, 1.2186015E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 16), 188.96, 190.34, 185.12, 185.5, 1.2760102E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 17), 184.68, 185.23, 181.62, 182.62, 1.7376563E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 18), 183.54, 184.05, 178.21, 179.22, 1.7850197E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 19), 180.37, 181.97, 178.86, 179.39, 1.4485899E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 22), 182.35, 184.3, 181.9, 183.35, 1.6303896E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 23), 183.84, 185.22, 183.33, 183.6, 2.3770502E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 24), 175.39, 177.95, 173.57, 174.37, 3.1250683E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 25), 174.25, 175.2, 169.05, 169.16, 2.896788E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 7, 26), 168.77, 169.84, 165.865, 168.68, 2.5150116E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 29), 170.5, 172.16, 169.72, 171.13, 1.3764831E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 30), 171.83, 172.95, 170.12, 171.86, 1.3648518E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 7, 31), 174.92, 175.91, 171.72, 173.15, 1.5650154E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 1), 171.98, 175.68, 170.51, 172.45, 1.7177833E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 2), 168.19, 170.21, 166.39, 168.4, 1.8907773E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 5), 157.37, 165.94, 156.6, 160.64, 3.4904215E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 6), 160.945, 162.35, 158.13, 160.54, 3.6146541E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 7), 163.24, 164.79, 160.24, 160.75, 1.9334246E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 8), 162.344, 165.5, 162.03, 163.84, 1.5363612E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 9), 161.645, 165.52, 160.93, 165.39, 1.354923E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 12), 165.995, 166.7, 163.55, 163.95, 1.2434969E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 8, 13), 165.185, 166.54, 164.77, 165.93, 1.2717628E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 14), 164.21, 164.96, 159.53, 162.03, 2.2515895E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 15), 162.21, 163.52, 161.49, 163.17, 1.8392452E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 16), 163.41, 166.95, 163.08, 164.74, 1.6849737E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 19), 167.0, 168.47, 166.09, 168.4, 1.3100762E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 20), 168.74, 170.41, 168.66, 168.96, 1.2622523E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 21), 166.99, 168.64, 166.57, 167.63, 1.526955E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 22), 169.04, 169.42, 165.03, 165.49, 1.9123778E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 23), 166.55, 167.95, 165.66, 167.43, 1.4281621E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 8, 26), 168.155, 169.38, 166.32, 167.93, 1.1990305E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 8, 27), 167.61, 168.245, 166.16, 166.38, 1.3718162E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 28), 166.78, 167.39, 163.28, 164.5, 1.5208736E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 29), 166.06, 167.63, 161.982, 163.4, 1.708683E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 8, 30), 164.22, 165.28, 163.41, 165.11, 1.8498777E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 3), 163.315, 163.38, 157.855, 158.61, 2.653311E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 4), 158.075, 160.4, 157.44, 157.81, 1.7410652E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 5), 157.78, 161.015, 157.52, 158.6, 1.4139501E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 6), 158.69, 159.22, 151.935, 152.13, 2.4997342E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 9), 153.63, 154.64, 148.2, 149.54, 2.8035996E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 9, 10), 151.45, 152.3, 149.5427, 150.01, 2.0395524E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 11), 151.09, 152.48, 148.7, 152.15, 1.8991486E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 12), 154.81, 155.61, 153.5, 155.54, 2.1015312E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 9, 13), 156.362, 159.275, 156.11, 158.37, 1.6733908E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 16), 158.33, 159.24, 157.61, 158.99, 1.4157614E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 17), 160.09, 161.59, 159.41, 160.28, 1.206479E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 18), 160.85, 161.63, 159.66, 160.81, 1.675645E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 9, 19), 164.82, 164.99, 162.5216, 163.24, 1.754819E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 20), 164.52, 164.75, 163.18, 164.64, 4.6362673E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 23), 165.34, 166.61, 162.95, 163.07, 1.5648446E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 24), 164.25, 164.55, 162.03, 163.64, 1.8546056E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 9, 25), 162.97, 164.217, 162.775, 162.99, 1.3607892E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 26), 165.03, 165.5, 163.5, 163.83, 1.8234497E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 27), 163.91, 166.97, 163.83, 165.29, 1.3604261E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 9, 30), 164.78, 167.36, 164.64, 167.19, 1.4083451E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 1), 168.86, 170.44, 165.9, 168.42, 1.8629506E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 2), 167.76, 168.88, 166.25, 167.31, 1.2744975E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 3), 165.82, 167.91, 165.37, 167.21, 1.1004333E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 4), 169.34, 169.55, 166.96, 168.56, 1.1435318E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 7), 169.14, 169.9, 164.13, 164.39, 1.4034722E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 8), 165.43, 166.1, 164.31, 165.7, 1.1723885E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 9), 164.855, 166.26, 161.12, 163.06, 1.9651411E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 10), 162.11, 164.3107, 161.64, 163.18, 1.2892892E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 11), 163.33, 165.27, 162.5, 164.52, 1.0945971E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 14), 164.91, 167.62, 164.78, 166.35, 9981765.0));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 15), 167.14, 169.09, 166.05, 166.9, 1.4829338E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 16), 166.03, 167.28, 165.216, 166.74, 9968474.0));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 17), 167.38, 167.93, 164.37, 164.51, 1.5113356E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 18), 164.87, 166.3699, 164.75, 165.05, 1.3091267E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 21), 164.58, 166.22, 164.305, 165.8, 1.1384047E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 22), 164.7, 167.47, 164.67, 166.82, 1.1958617E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 23), 166.43, 167.6, 163.6325, 164.48, 1.2754283E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 24), 164.59, 165.05, 162.77, 164.53, 1.276443E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 10, 25), 165.365, 167.4, 165.23, 166.99, 1.456641E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 28), 170.59, 170.606, 165.79, 168.34, 2.0858254E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 29), 169.385, 171.86, 168.66, 171.14, 2.8916106E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 30), 182.41, 183.79, 175.7451, 176.14, 4.9698313E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 10, 31), 174.72, 178.42, 172.56, 172.69, 3.2801898E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 11, 1), 171.54, 173.82, 170.31, 172.65, 2.1752859E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 4), 171.24, 171.92, 169.485, 170.68, 1.6193994E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 5), 170.83, 172.0973, 170.37, 171.41, 1.2518282E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 11, 6), 175.35, 178.64, 175.04, 178.33, 2.706151E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 11, 7), 179.11, 182.58, 178.89, 182.28, 1.6730407E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 11, 8), 182.0, 182.35, 179.57, 179.86, 1.5021549E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 11), 180.07, 182.085, 179.99, 181.97, 1.2503422E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 12), 181.38, 184.025, 180.99, 183.32, 1.4065845E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 13), 182.15, 182.615, 180.12, 180.49, 1.3969709E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 14), 179.75, 180.445, 176.03, 177.35, 1.7925763E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 15), 175.64, 175.88, 172.745, 173.89, 2.170887E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 18), 174.955, 176.91, 174.42, 176.8, 1.8725422E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 19), 175.235, 180.17, 175.116, 179.58, 1.5392866E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 20), 178.83, 179.11, 175.33, 177.33, 1.5729806E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 21), 175.455, 175.58, 165.31, 169.24, 3.8839431E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 22), 167.16, 168.2645, 165.71, 166.57, 2.4497042E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 11, 25), 167.99, 170.46, 167.4, 169.43, 2.1395652E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 26), 169.49, 171.495, 169.43, 170.62, 1.4937478E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 11, 27), 170.68, 171.14, 169.67, 170.82, 1.2433371E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 11, 29), 170.06, 170.87, 168.75, 170.49, 9250712.0));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 2), 170.32, 173.6, 170.27, 172.98, 1.6593444E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 3), 173.12, 174.32, 172.51, 173.02, 1.5721483E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 4), 172.78, 176.43, 172.75, 176.09, 1.8239842E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 5), 177.32, 177.71, 174.01, 174.31, 1.614552E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 6), 173.88, 176.8389, 173.55, 176.49, 1.3319549E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 9), 175.715, 178.04, 175.4, 177.1, 1.9887786E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 10), 184.535, 188.03, 182.67, 186.53, 3.4317438E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 11), 186.7, 196.89, 186.26, 196.71, 4.1664489E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 12), 196.3, 196.7053, 193.28, 193.63, 2.5197757E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 13), 192.71, 194.34, 191.26, 191.38, 1.8883217E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 16), 194.365, 200.64, 194.11, 198.16, 3.224864E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 17), 198.53, 202.88, 196.69, 197.12, 2.4099481E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 18), 196.83, 198.6899, 189.28, 190.15, 2.7638416E7));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 19), 193.28, 194.6, 189.52, 189.7, 2.6981196E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 20), 187.01, 194.1354, 186.37, 192.96, 4.5319703E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 23), 194.03, 196.49, 191.63, 195.99, 1.5235942E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 24), 196.17, 197.67, 195.1979, 197.57, 6809823.0));
    list.add(BasicOHLCV.of(LocalDate.of(2024, 12, 26), 196.74, 198.16, 195.87, 197.1, 7918434.0));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 27), 196.47, 196.8, 191.972, 194.04, 1.4692994E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 30), 190.865, 193.78, 190.36, 192.69, 1.2209534E7));
    list.add(
        BasicOHLCV.of(LocalDate.of(2024, 12, 31), 192.445, 193.25, 189.58, 190.44, 1.4355221E7));

    return List.copyOf(list);
  }
}
