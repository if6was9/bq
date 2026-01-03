package bq.command;

import bq.PriceTable;
import bq.provider.DataProviders;
import bq.provider.S3StorageProvider;
import bq.provider.S3StorageProvider.PriceDataS3Object;
import bx.util.Slogger;
import bx.util.Zones;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;

public class RefreshCommand {

  static Logger logger = Slogger.forEnclosingClass();

  S3StorageProvider storageProvider;
  DataSource dataSource;
  String bucket;

  public RefreshCommand dataSource(DataSource ds) {
    this.dataSource = ds;
    return this;
  }

  public RefreshCommand bucket(String name) {
    this.bucket = name;
    return this;
  }

  public boolean isUpdatedRecently(PriceDataS3Object x) {
    Duration age = Duration.between(x.getS3Object().lastModified(), Instant.now());

    System.out.println(age.get(ChronoUnit.SECONDS));
    if (age.getSeconds() < 60 * 60 * 4) {
      return true;
    }
    return false;
  }

  public void refresh(PriceDataS3Object obj) {
    List<PriceTable> tablesToDrop = Lists.newArrayList();
    PriceTable s3Table = null;

    try {
      logger.atInfo().log("refresh: {}", obj);

      if (isUpdatedRecently(obj)) {
        logger.atInfo().log(
            "skipping refresh of {} because it was updated {}",
            obj.getTicker(),
            obj.getS3Object().lastModified());

        return;
      }

      s3Table = this.storageProvider.createTableFromStorage(obj);
      tablesToDrop.add(s3Table);
      long s3RowCount = s3Table.rowCount();
      logger.atInfo().log("{} has {} rows in s3", obj, s3RowCount);

      Optional<Object> dt =
          s3Table
              .sql(
                  String.format(
                      "select date from '%s' order by date desc limit 1", s3Table.getName()))
              .query()
              .optionalValue();
      boolean refresh = false;
      if (dt.isPresent()) {
        Object x = dt.get();
        if (x instanceof LocalDate) {

          LocalDate cutoff =
              Instant.now().minus(1, ChronoUnit.DAYS).atZone(Zones.UTC).toLocalDate();

          LocalDate lastData = (LocalDate) x;

          if (lastData.isBefore(cutoff)) {
            refresh = true;
          }
          refresh = true;
        }

      } else {
        refresh = true;
      }

      long afterRefreshRowCount = s3RowCount;
      if (refresh) {
        PriceTable newData = DataProviders.newRequest(obj.getTicker()).fetchIntoTable();
        tablesToDrop.add(newData);
        s3Table.insertMissing(newData);
        afterRefreshRowCount = s3Table.rowCount();

        if (afterRefreshRowCount > s3RowCount) {
          logger.atInfo().log("writing {} rows back to s3", afterRefreshRowCount);
          writeTableToS3(s3Table, obj);
        }
      }
      // PriceTable pt = DataProviders.newRequest(obj.getTicker()).fetchIntoTable();

      // pt.show();
    } catch (RuntimeException e) {
      logger.atWarn().setCause(e).log("could not process: {}", obj);
    } finally {

      for (PriceTable tmp : tablesToDrop) {
        try {
          logger.atInfo().log("dropping {}", tmp);
          tmp.drop();
        } catch (Exception e) {
          logger.atInfo().setCause(e).log();
        }
      }
    }
  }

  public void writeTableToS3(PriceTable table, PriceDataS3Object s3Obj) {
    String sql =
        String.format(
            "COPY (SELECT * FROM '%s' ORDER BY date asc) TO '%s' (FORMAT csv, HEADER)",
            table.getName(), s3Obj.toUrl());
    JdbcClient jdbc = JdbcClient.create(this.storageProvider.getDataSource());

    jdbc.sql(sql).update();
  }

  public void run() {
    S3StorageProvider p = new S3StorageProvider().dataSource(dataSource).bucket(bucket);
    this.storageProvider = p;

    List<PriceDataS3Object> list = Lists.newArrayList();

    list.addAll(p.listCrypto());
    list.addAll(p.listStocks());
    // list.addAll(p.listIndices());

    Collections.shuffle(list);

    AtomicInteger count = new AtomicInteger();
    list.forEach(
        it -> {
          int c = count.incrementAndGet();
          logger.atInfo().log("{}/{}: {}", c, list.size(), it.getTicker());
          refresh(it);
        });
  }
}
