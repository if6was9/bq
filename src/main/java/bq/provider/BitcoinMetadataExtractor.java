package bq.provider;

import bx.sql.duckdb.DuckTable;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;

public class BitcoinMetadataExtractor {

  static Logger logger = Slogger.forEnclosingClass();
  DataSource dataSource;
  BitcoinClient client;

  DuckTable table;

  String currentBlock;

  public BitcoinMetadataExtractor dataSource(DataSource ds) {
    this.dataSource = ds;
    return this;
  }

  public BitcoinMetadataExtractor client(BitcoinClient bc) {
    this.client = bc;
    return this;
  }

  public BitcoinMetadataExtractor table(String name) {
    Preconditions.checkState(dataSource != null, "dataSource must be set before table");
    table = DuckTable.of(dataSource, name);
    return this;
  }

  public DuckTable createTable(String name) {
    DuckTable t = DuckTable.of(dataSource, name);

    String sql =
        """

        create table '%s' (


        	hash varchar(64),
        	height int,
        	time timestamptz,
        	value double,
        	nonce varchar(40),
        	version varchar(10),
        	versionhex varchar(10),
        	previousblockhash varchar(64),
        	nextblockhash varchar(64),
        	merkleroot varchar(64),
        	mediantime timestamptz,
        	ntx int,
        	difficulty double,
        	chainwork varchar(64),
        	bits varchar(50),
        	size int64,
        	weight int64,
        	strippedsize int64
        )
        """;

    sql = String.format(sql, name);

    t.sql(sql).update();

    return t;
  }

  public void processBlock(int height) {
    processBlock(client.getBlockHash(height));
  }

  boolean hasBlock(String hash) {
    List<Object> list =
        table
            .sql("select hash from '" + table.getName() + "' where hash=:hash limit 1")
            .param("hash", hash)
            .query()
            .singleColumn();

    return !list.isEmpty();
  }

  Optional<String> getPrevBlockHash(String hash) {
    List<Object> x =
        table
            .sql("select previousblockhash from '" + table.getName() + "' where hash=:hash limit 1")
            .param("hash", hash)
            .query()
            .singleColumn();

    if (x.isEmpty() || x.getFirst() == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(x.getFirst().toString());
  }

  Optional<String> getNextBlockHash(String hash) {
    List<Object> x =
        table
            .sql("select nextblockhash from '" + table.getName() + "' where hash=:hash limit 1")
            .param("hash", hash)
            .query()
            .singleColumn();

    if (x.isEmpty() || x.getFirst() == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(x.getFirst().toString());
  }

  Optional<String> getBlockHash(int height) {
    List<Object> x =
        table
            .sql("select hash from '" + table.getName() + "' where height=:height limit 1")
            .param("height", height)
            .query()
            .singleColumn();
    if (x.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable((String) x.getFirst());
  }

  public boolean hasPrev() {
    if (S.isBlank(currentBlock)) {
      return false;
    }
    Optional<String> prev = getPrevBlockHash(currentBlock);

    return prev.isPresent();
  }

  public void processNext() {
    Optional<String> nextHash = getNextBlockHash(currentBlock);
    if (nextHash.isPresent()) {
      processBlock(nextHash.get());
    }
  }

  public void processPrev() {
    Optional<String> prevHash = getPrevBlockHash(currentBlock);
    if (prevHash.isPresent()) {
      processBlock(prevHash.get());
    }
  }

  public boolean hasNext() {
    if (S.isBlank(currentBlock)) {
      return false;
    }
    Optional<String> prev = getNextBlockHash(currentBlock);

    return prev.isPresent();
  }

  public DuckTable getTable() {
    return this.table;
  }

  public void processBlock(String blockHash) {

    if (hasBlock(blockHash)) {
      logger.atInfo().log("already have {}", blockHash);
      this.currentBlock = blockHash;
      return;
    }
    JsonNode n = client.getBlock(blockHash, 2);

    String hash = n.path("hash").asString();
    int height = n.path("height").asInt();
    String version = n.path("version").asString();
    String versionHex = n.path("versionHex").asString();
    String merkleRoot = n.path("merkleroot").asString();
    Instant time = Instant.ofEpochSecond(n.path("time").asLong());
    Instant mediantime = Instant.ofEpochSecond(n.path("mediantime").asLong());
    String nonce = n.path("nonce").asString();
    String bits = n.path("bits").asString();
    String chainwork = n.path("chainwork").asString();
    double difficulty = n.path("difficulty").asDouble();
    int ntx = n.path("nTx").asInt();
    String previousblockhash = n.path("previousblockhash").asString(null);
    String nextblockhash = n.path("nextblockhash").asString(null);
    long strippedSize = n.path("strippedsize").asLong();
    long size = n.path("size").asLong();
    long weight = n.path("weight").asLong();

    this.currentBlock = blockHash;
    AtomicDouble value = new AtomicDouble();
    n.path("tx")
        .forEach(
            it -> {
              it.path("vout")
                  .forEach(
                      vout -> {
                        double val = vout.path("value").asDouble();
                        value.addAndGet(val);
                      });
            });

    logger.atInfo().log("process height={} time={} hash={}", height, time, hash);

    String sql =
        """

insert into %s (time,mediantime,hash,height,value,nonce,version,versionhex,previousblockhash,nextblockhash,merkleroot,
difficulty,chainwork,bits,ntx,strippedsize,size,weight) values
(:time,:mediantime,:hash,:height,:value,:nonce,:version,:versionhex,:previousblockhash,:nextblockhash,:merkleroot,
:difficulty,
:chainwork,
:bits,
:ntx,
:strippedsize,
:size,
:weight
)

""";
    sql = String.format(sql, table.getName());

    if (S.isBlank(nextblockhash)) {
      return;
    }

    table
        .sql(sql)
        .param("hash", hash)
        .param("time", Timestamp.from(time))
        .param("mediantime", Timestamp.from(mediantime))
        .param("nonce", nonce)
        .param("height", height)
        .param("version", version)
        .param("versionhex", versionHex)
        .param("previousblockhash", previousblockhash)
        .param("nextblockhash", nextblockhash)
        .param("merkleroot", merkleRoot)
        .param("difficulty", difficulty)
        .param("chainwork", chainwork)
        .param("bits", bits)
        .param("ntx", ntx)
        .param("strippedsize", strippedSize)
        .param("size", size)
        .param("weight", weight)
        .param("value", value.get())
        .update();
  }
}
