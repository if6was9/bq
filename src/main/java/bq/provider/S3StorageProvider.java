package bq.provider;


import bq.BqException;

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
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;


public class S3StorageProvider extends StorageProvider {

  Logger logger = Slogger.forEnclosingClass();


  DataSource dataSource;


  public S3StorageProvider() {}

  public S3StorageProvider(DataSource ds, String bucket) {
    this();
    dataSource(ds);
    bucket(bucket);
  }


  public class PriceDataS3Object {
    S3Object obj;

    PriceDataS3Object(S3Object x) {
      this.obj = x;
    }

    public S3Object getS3Object() {
      return obj;
    }

    public Ticker getTicker() {
      return S3StorageProvider.this.getTicker(obj).orElse(null);
    }


    public String toUrl() {
    	return toUrl(getTicker());
    }
    String toUrl(Ticker ticker) {
        Preconditions.checkState(S.isNotBlank(getBucket()), "bucket must be set");
        return String.format("s3://%s/%s", getBucket(), toKey(ticker));

      }

    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("ticker", getTicker())
          .add("s3Key", getS3Object().key())
          .toString();
    }
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
    Preconditions.checkState(S.isNotBlank(getBucket()), "bucket must be set");
    return String.format("s3://%s/%s", getBucket(), toKey(ticker));
  }
  Duration getTimeSinceLastModified(S3Object x) {
    Instant t = x.lastModified();
    return Duration.between(t, Instant.now());
  }

  Optional<Ticker> getTicker(S3Object x) {

    String key = x.key();
    if (x == null || S.isBlank(key)) {
      return Optional.empty();
    }
    Ticker.TickerType type = null;
    if (key.contains("stocks/")) {
      type = TickerType.STOCK;
    } else if (key.contains("crypto/")) {
      type = TickerType.CRYPTO;
    } else if (key.contains("indices/")) {
      type = TickerType.INDEX;
    } else {
      return Optional.empty();
    }

    List<String> parts = Splitter.on("/").splitToList(key);
    if (parts.size() < 1) {
      return Optional.empty();
    }
    String f = parts.getLast();
    if (f.endsWith(".csv.gz")) {
      f = f.replace(".csv.gz", "");
      return Optional.of(Ticker.of(type, f));
    }
    if (f.endsWith(".csv")) {
      f = f.replace(".csv", "");
      return Optional.of(Ticker.of(type, f));
    }
    return Optional.empty();
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



  public List<PriceDataS3Object> listIndices() {
    return list("indices/1d/");
  }

  public List<PriceDataS3Object> listCrypto() {
    return list("crypto/1d/");
  }

  public List<PriceDataS3Object> listStocks() {
    return list("stocks/1d/");
  }

  private S3Client openS3Client() {
    Preconditions.checkState(S.isNotBlank(getBucket()), "bucket must be set");
    return S3Client.create();
  }

  private List<PriceDataS3Object> list(String prefix) {

    try (S3Client s3 = openS3Client()) {
      AtomicReference<String> token = new AtomicReference<String>();

      List<PriceDataS3Object> list = Lists.newArrayList();
      do {

        ListObjectsV2Response response =
            s3.listObjectsV2(
                request -> {
                  request.continuationToken(token.get());

                  request.bucket(getBucket());
                  request.prefix(prefix);
                });

        token.set(response.nextContinuationToken());

        response.contents().stream()
            .filter(t -> t.key().contains("/1d/"))
            .filter(
                t -> {
                  return t.key().endsWith(".csv") || t.key().endsWith(".csv.gz");
                })
            .forEach(
                t -> {
                  PriceDataS3Object pdf = new PriceDataS3Object(t);
                  if (pdf.getTicker() != null) {
                    list.add(pdf);
                  }
                });

      } while (S.isNotBlank(token.get()));

      return list;
    }
  }

  public PriceTable createTableFromStorage(PriceDataS3Object obj) {
    String url = String.format("s3://%s/%s", getBucket(), obj.getS3Object().key());
    logger.atInfo().log("loading {}", url);

    String table =
        String.format(
            "temp_%s_%s", obj.getTicker().getSymbol().toLowerCase(), System.currentTimeMillis());

    try (S3Client s3 = S3Client.create()) {

      Path tmp = Files.createTempFile("temp", ".csv");
      tmp.toFile().delete();
      s3.getObject(
          x -> {
            x.bucket(getBucket());
            x.key(obj.getS3Object().key());
          },
          ResponseTransformer.toFile(tmp));

      String sql =
          String.format(
              "create table '%s' as (select * from '%s' order by date)",
              table, tmp.toFile().getAbsolutePath());

      JdbcClient.create(getDataSource()).sql(sql).update();

    } catch (IOException e) {
      throw new BqException(e);
    }
    return new PriceTable(getDataSource(), table);
  }

}
