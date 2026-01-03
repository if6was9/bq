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
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;
import tools.jackson.databind.JsonNode;

public class BitcoinIndexer {

  static Logger logger = Slogger.forEnclosingClass();
  DataSource dataSource;
  BitcoinClient client;
  DuckTable blockTable;
  DuckTable txTable;
  DuckTable txInputTable;
  DuckTable txOutputTable;

  Cache<Integer, String> blockHeightCache = CacheBuilder.newBuilder().maximumSize(1000).build();

  public BitcoinIndexer createTables() {
    this.blockTable = DuckTable.of(dataSource, "block");
    this.txTable = DuckTable.of(dataSource, "tx");
    this.txInputTable = DuckTable.of(dataSource, "tx_input");
    this.txOutputTable = DuckTable.of(dataSource, "tx_output");
    if (!blockTable.exists()) {
      this.blockTable = createBlockTable();
    }
    if (!txTable.exists()) {
      this.txTable = createTxTable();
    }
    if (!txInputTable.exists()) {
      this.txInputTable = createTxInputTable();
    }
    if (!txOutputTable.exists()) {
      this.txOutputTable = createTxOutputTable();
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

  public DuckTable createTxOutputTable() {
    DuckTable t = DuckTable.of(dataSource, "tx_output");

    String sql =
        """
        create table tx_output (
        txid varchar(64),
        blockhash varchar(64),
        n int,
        value double,
        address varchar(64)
        )
        """;
    t.sql(sql).update();

    return t;
  }

  public DuckTable createTxInputTable() {
    DuckTable t = DuckTable.of(dataSource, "tx_input");

    String sql =
        """
        create table tx_input (
        txid varchar (64),
        	blockhash varchar(64),
        	from_txid varchar(64),
        	from_n int,
        	from_coinbase varchar(200)
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
        	locktime int64,
        	weight int32,
        	size int,
        	version int,
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
    if (!hash.isPresent()) {

      hash = S.notEmpty(getClient().getBlockHash(height));
      if (hash.isPresent()) {
        blockHeightCache.put(height, hash.get());
      }
    }

    logger.atInfo().log("{}: {}", height, hash);

    return hash;
  }

  public DuckTable getBlockTable() {
    return this.blockTable;
  }

  public DuckTable getTxOutputTable() {
    return this.txOutputTable;
  }

  public DuckTable getTxInputTable() {
    return this.txInputTable;
  }

  public DuckTable getTxTable() {
    return this.txTable;
  }

  private void writeTx(JsonNode blockNode) {

    String blockHash = blockNode.path("hash").asString();

    logger.atInfo().log("writing transactions for {}", blockHash);

    Preconditions.checkArgument(blockNode.has("hash"));
    String sql =
        """
insert into tx (txid,blockhash,fee,value,coinbase,locktime,weight,size,version) values (:txid,:blockhash,:fee,:value,:coinbase,
:locktime,
:weight,
:size,
:version

) ON CONFLICT(txid)

DO UPDATE
set blockhash=excluded.blockhash,
fee = excluded.fee,
value = excluded.value,
coinbase = excluded.coinbase,
locktime = excluded.locktime,
version = excluded.version,
weight = excluded.weight,
size = excluded.size
""";

    String insertTxOutputSql =
        """
        insert into tx_output
        	(txid,blockhash,n,value,address) values (:txid,:blockhash,:n,:value,:address)
        """;

    JdbcClient jdbc = JdbcClient.create(dataSource);
    StatementSpec insertTxOutputSPec = jdbc.sql(insertTxOutputSql);
    StatementSpec insertTxSpec = jdbc.sql(sql);
    blockNode
        .path("tx")
        .forEach(
            tx -> {
              AtomicReference<BigDecimal> txValue =
                  new AtomicReference<BigDecimal>(new BigDecimal(0));
              String txid = tx.path("txid").asString();
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
                        double value = vout.path("value").asDouble(0d);
                        int n = vout.path("n").asInt(0);
                        String address = vout.path("address").asString(null);

                        insertTxOutputSPec
                            .param("blockhash", blockHash)
                            .param("txid", txid)
                            .param("n", n)
                            .param("value", value)
                            .param("address", address)
                            .update();

                        BigDecimal total =
                            txValue.get().add(vout.path("value").asDecimal(new BigDecimal(0)));
                        txValue.set(total);
                      });

              double fee = tx.path("fee").asDouble(0d);
              int locktime = tx.path("locktime").asInt(0);
              int weight = tx.path("weight").asInt(0);
              int size = tx.path("size").asInt(0);
              int version = tx.path("version").asInt(0);
              AtomicReference<String> coinbaseRef = new AtomicReference<String>(null);

              String txinputSql =
                  """

insert into tx_input (txid,blockhash,from_coinbase,from_n, from_txid) values
(
	:txid,
	:blockhash,
	:from_coinbase,
	:from_n,
	:from_txid
	)

""";
              StatementSpec txInputSpec = jdbc.sql(txinputSql);

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

                        // also has txwitness array which we don't care about for now

                        int c =
                            txInputSpec
                                .param("txid", txid)
                                .param("blockhash", blockHash)
                                .param("from_txid", inputTxId)
                                .param("from_coinbase", coinbase)
                                .param("from_n", vout)
                                .update();
                      });
              insertTxSpec
                  .param("txid", txid)
                  .param("fee", fee)
                  .param("locktime", locktime)
                  .param("weight", weight)
                  .param("size", size)
                  .param("version", version)
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

insert into block (time,mediantime,hash,height,value,nonce,version,versionhex,previousblockhash,nextblockhash,merkleroot,
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
