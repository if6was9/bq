package bq.provider;

import bq.PriceTable;
import bq.Ticker;
import bq.Ticker.TickerType;
import bx.sql.duckdb.DuckS3Extension;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Preconditions;
import java.util.UUID;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;

public class S3StorageProvider extends StorageProvider {

  Logger logger = Slogger.forEnclosingClass();

  DataSource dataSource;

  public S3StorageProvider() {}

  public S3StorageProvider(DataSource ds, String bucket) {
    this();
    dataSource(ds);
    bucket(bucket);
  }

  String toPrefix(TickerType tt) {
    Preconditions.checkNotNull(tt);
    switch (tt) {
      case TickerType.STOCK:
        return "stocks";
      case TickerType.INDEX:
        return "indices";
      case TickerType.CRYPTO:
        return "crypto";
    }
    throw new IllegalArgumentException("unsupported ticker type: " + tt);
  }

  String toKey(Ticker t) {

    return String.format("%s/1d/%s.csv", toPrefix(t.getType()), t.getSymbol().toUpperCase());
  }

  String toUrl(Ticker ticker) {
    Preconditions.checkState(S.isNotBlank(bucket), "bucket must be set");
    return String.format("s3://%s/%s", bucket, toKey(ticker));
  }

  @Override
  public PriceTable createTableFromStorage(Ticker ticker) {

    String table = "temp_" + UUID.randomUUID().toString().substring(0, 8);
    return createTableFromStorage(ticker, table);
  }

  private JdbcClient getJdbcClient() {
    return JdbcClient.create(getDataSource());
  }

  private PriceTable loadS3CSV(String url, String table) {
    var ds = getDataSource();
    Preconditions.checkState(ds != null, "dataSource not set");
    DuckS3Extension.load(ds).useCredentialChain();

    String sql = String.format("create table '%s' as (select * from '%s')", table, url);
    int count = getJdbcClient().sql(sql).update();

    logger.atInfo().log("loaded {} records into {} from {}", count, table, url);
    return PriceTable.from(ds, table);
  }

  @Override
  public PriceTable createTableFromStorage(Ticker ticker, String table) {

    String url = toUrl(ticker);

    return loadS3CSV(url, table);
  }

  @Override
  public void writeTableToStorage(Ticker ticker, PriceTable table) {

    String url = toUrl(ticker);
    logger.atInfo().log("writing data to {}", url);

    String sql =
        String.format(
            "COPY (SELECT * FROM '%s' order by date) TO '%s' (HEADER, DELIMITER ',')",
            table.getName(), url);

    logger.atInfo().log("SQL: {}", sql);
    System.out.println(sql);

    int count = getJdbcClient().sql(sql).update();
    logger.atInfo().log("wrote {}", count);
  }

  /*
  	public List<S3Object> listIndices(String bucket) {
  		return list(bucket, "indices/1d/");
  	}

  	public List<S3Object> listCrypto(String bucket) {
  		return list(bucket, "crypto/1d/");
  	}

  	public List<S3Object> listStocks(String bucket) {
  		return list(bucket, "stocks/1d/");
  	}

  	private Closeable closeable(S3Client c) {
  		Closeable ac = new Closeable() {

  			@Override
  			public void close() {
  				if (c != null) {
  					c.close();
  				}

  			}

  		};
  		return ac;
  	}

  	private List<S3Object> list(String bucket, String prefix) {

  		try (Closer closer = Closer.create()) {
  			AtomicReference<String> token = new AtomicReference<String>();

  			List<S3Object> list = Lists.newArrayList();
  			do {

  				var s3 = S3Client.create();
  				closer.register(closeable(s3));
  				ListObjectsV2Response response = s3.listObjectsV2(request -> {

  					request.continuationToken(token.get());

  					request.bucket("data.bitquant.cloud");
  					request.prefix(prefix);
  				});

  				token.set(response.nextContinuationToken());

  				response.contents().stream().filter(t -> t.key().contains("/1d/")).filter(t -> {

  					return t.key().endsWith(".csv") || t.key().endsWith(".csv.gz");
  				}).forEach(t -> {
  					list.add(t);
  				});

  			} while (S.isNotBlank(token.get()));

  			return list;
  		} catch (IOException e) {
  			throw new BxException(e);
  		}
  	}

  	public void testIt() {

  		var ds = getDataSource();
  		DuckS3Extension.load(ds).useCredentialChain();
  		listCrypto("data.bitquant.cloud").forEach(it -> {

  			String url = toS3Url("data.bitquant.cloud", it.key());

  			String symbol = Splitter.on("/").splitToList(url).getLast().replace(".csv", "");
  			String table = "s_"+symbol;
  			PriceTable pt = null;//loadS3CSV(url, table);

  			PriceTable cbt = new CoinbaseDataProvider().dataSource(getDataSource()).newRequest(symbol).fetchIntoTable();

  			int count = pt.insertMissing(cbt);

  			System.out.println("Added "+count);
  		//	pt.show();

  		//	Sleep.sleepSecs(10);

  		});
  	}

  	String toS3Url(String bucket, String key) {
  		return String.format("s3://%s/%s", bucket, key);
  	}
  */
}
