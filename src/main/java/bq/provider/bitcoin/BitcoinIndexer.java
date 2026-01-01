package bq.provider.bitcoin;

import bx.sql.duckdb.DuckTable;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.AtomicDouble;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.JsonNode;

public class BitcoinIndexer {

  static Logger logger = Slogger.forEnclosingClass();
  DataSource dataSource;
  BitcoinClient client;
  DuckTable blockTable;
  DuckTable txTable;
  DuckTable txInputTable;

  Cache<Integer, String> blockHeightCache = CacheBuilder.newBuilder().maximumSize(1000).build();

  public BitcoinIndexer createTables() {
    this.blockTable = DuckTable.of(dataSource, "block");
    this.txTable = DuckTable.of(dataSource, "tx");
    this.txInputTable = DuckTable.of(dataSource, "tx_input");
    if (!blockTable.exists()) {
      this.blockTable = createBlockTable();
    }
    if (!txTable.exists()) {
      this.txTable = createTxTable();
    }
    if (!txInputTable.exists()) {
      this.txInputTable = createTxInputTable();
    }
    return this;
  }

  public BitcoinIndexer dataSource(DataSource ds) {
    this.dataSource = ds;

    return this;
  }

  public BitcoinIndexer client(BitcoinClient bc) {
    this.client = bc;
    return this;
  }

  public DuckTable createTxInputTable() {
    DuckTable t = DuckTable.of(dataSource, "tx_input");

    String sql =
        """
        create table tx_input (

        	vin_txid varchar(64),
        	vin_coinbase varchar(200),
        	vin_vout int,
        	vout_txid varchar (64),
        	vout_blockhash varchar(64)

        )
        """;
    t.sql(sql).update();

    return t;
  }

  public DuckTable createTxTable() {

    DuckTable t = DuckTable.of(dataSource, "tx");
    String sql =
        """
        create table tx (
        	txid varchar (64),
        	blockhash varchar(64),
        	value double,
        	fee double,
        	coinbase varchar(200)
        )
        """;
    t.sql(sql).update();

    t.addPrimaryKey("txid");
    return t;
  }

  public DuckTable createBlockTable() {
    DuckTable t = DuckTable.of(dataSource, "block");

    String sql =
        """
        create table 'block' (
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

    t.sql(sql).update();
    t.addPrimaryKey("hash");
    return t;
  }

  public void processBlock(int height) {

    processBlock(getBlockHash(height).get());
  }

  public boolean hasBlockInDb(int height) {
    List<Object> list =
        blockTable
            .sql("select hash from '" + blockTable.getName() + "' where height=:height")
            .param("height", height)
            .query()
            .singleColumn();

    if (list.isEmpty()) {
      return false;
    }
    return true;
  }

  public boolean hasBlockInDb(String hash) {
    List<Object> list =
        blockTable
            .sql("select hash from '" + blockTable.getName() + "' where hash=:hash limit 1")
            .param("hash", hash)
            .query()
            .singleColumn();

    return !list.isEmpty();
  }

  Optional<String> getPrevBlockHash(String hash) {
    List<Object> x =
        blockTable
            .sql(
                "select previousblockhash from '"
                    + blockTable.getName()
                    + "' where hash=:hash limit 1")
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
        blockTable
            .sql(
                "select nextblockhash from '" + blockTable.getName() + "' where hash=:hash limit 1")
            .param("hash", hash)
            .query()
            .singleColumn();

    if (x.isEmpty() || x.getFirst() == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(x.getFirst().toString());
  }

  Optional<String> getBlockHash(int height) {
    Optional<String> hash = S.notBlank(blockHeightCache.getIfPresent(height));
    if (hash.isPresent()) {

      return hash;
    }
    hash = S.notEmpty(getClient().getBlockHash(height));
    if (hash.isPresent()) {
      blockHeightCache.put(height, hash.get());
    }
    return hash;
  }

  /*
   * Optional<String> getBlockHashx(int height) { List<Object> x = blockTable
   * .sql("select hash from '" + blockTable.getName() +
   * "' where height=:height limit 1") .param("height", height) .query()
   * .singleColumn(); if (x.isEmpty()) { return Optional.empty(); } return
   * Optional.ofNullable((String) x.getFirst()); }
   */

  public DuckTable getBlockTable() {
    return this.blockTable;
  }

  public DuckTable getTxInputTable() {
    return this.txInputTable;
  }

  public DuckTable getTxTable() {
    return this.txTable;
  }

  private void writeTx(JsonNode blockNode) {

    String blockHash = blockNode.path("hash").asString();

    logger.atInfo().log("writing vout transactions for {}", blockHash);

    Preconditions.checkArgument(blockNode.has("hash"));
    String sql =
        """
insert into tx (txid,blockhash,fee,value,coinbase) values (:txid,:blockhash,:fee,:value,:coinbase) ON CONFLICT(txid)

DO UPDATE
set blockhash=excluded.blockhash,
fee = excluded.fee,
value = excluded.value,
coinbase = excluded.coinbase
""";

    JdbcClient jdbc = JdbcClient.create(dataSource);

    blockNode
        .path("tx")
        .forEach(
            tx -> {
              AtomicReference<BigDecimal> txValue =
                  new AtomicReference<BigDecimal>(new BigDecimal(0));

              tx.path("vout")
                  .forEach(
                      vout -> {
                        // outputs will loook like this.  We might care about them in the future,
                        // but for now, all we care about is the
                        // sum of the transaction values

                        //
                        // {
                        //      "value" : 0.00107694,
                        //      "n" : 1,
                        //      "scriptPubKey" : {
                        //        "asm" : "1
                        // 52ad9ce6524351d0c78287c3e3f5ad774b12a96598d58d71051700488719f628",
                        //        "desc" :
                        // "addr(bc1p22keeejjgdgap3uzslp78addwa9392t9nr2c6ug9zuqy3pce7c5qtud4j7)#kgdjh9ht",
                        //        "hex" :
                        // "512052ad9ce6524351d0c78287c3e3f5ad774b12a96598d58d71051700488719f628",
                        //        "address" :
                        // "bc1p22keeejjgdgap3uzslp78addwa9392t9nr2c6ug9zuqy3pce7c5qtud4j7",
                        //        "type" : "witness_v1_taproot"
                        //      }
                        //   }

                        BigDecimal total =
                            txValue.get().add(vout.path("value").asDecimal(new BigDecimal(0)));
                        txValue.set(total);
                      });

              String txid = tx.path("txid").asString();

              double fee = tx.path("fee").asDouble(0d);

              AtomicReference<String> coinbaseRef = new AtomicReference<String>(null);
              tx.path("vin")
                  .forEach(
                      vin -> {
                        String coinbase = vin.path("coinbase").asString(null);
                        if (S.isNotBlank(coinbase)) {
                          Preconditions.checkState(coinbaseRef.get() == null);
                          coinbaseRef.set(coinbase);
                        }

                        String inputTxId = vin.path("txid").asString(null);

                        int vout = vin.path("vout").asInt(0);

                        String txinputSql =
                            """

insert into tx_input (vin_txid,vin_coinbase,vout_txid,vout_blockhash,vin_vout) values
(
		:vin_txid,
		:vin_coinbase,
		:vout_txid,
		:vout_blockhash,
		:vin_vout)

""";

                        // also has txwitness array which we don't care about for now

                        jdbc.sql(txinputSql)
                            .param("vout_txid", txid)
                            .param("vout_blockhash", blockHash)
                            .param("vin_txid", inputTxId)
                            .param("vin_coinbase", coinbase)
                            .param("vin_vout", vout)
                            .update();
                      });
              jdbc.sql(sql)
                  .param("txid", txid)
                  .param("fee", fee)
                  .param("value", txValue.get())
                  .param("blockhash", blockHash)
                  .param("coinbase", coinbaseRef.get())
                  .update();
            });
  }

  public void processBlock(String blockHash) {

    JsonNode n = client.getBlock(blockHash, 2);

    String hash = n.path("hash").asString();
    int height = n.path("height").asInt();
    String previousblockhash = n.path("previousblockhash").asString(null);
    String nextblockhash = n.path("nextblockhash").asString(null);

    if (nextblockhash != null) {
      this.blockHeightCache.put(height + 1, nextblockhash);
    }
    if (previousblockhash != null) {
      this.blockHeightCache.put(height - 1, previousblockhash);
    }

    if (hasBlockInDb(blockHash)) {
      logger.atInfo().log("already have {}", blockHash);
      return;
    }
    writeTx(n);

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

    long strippedSize = n.path("strippedsize").asLong();
    long size = n.path("size").asLong();
    long weight = n.path("weight").asLong();

    if (S.isBlank(nextblockhash)) {
      // don't write if this is the tip block
      return;
    }

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
    sql = String.format(sql, blockTable.getName());

    blockTable
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

  public BitcoinClient getClient() {
    return this.client;
  }
}
