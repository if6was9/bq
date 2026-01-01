package bq;

import bq.provider.bitcoin.AmazonBitcoinClient;
import bq.provider.bitcoin.BitcoinClient;
import bq.provider.bitcoin.BitcoinIndexer;
import bx.sql.Db;
import bx.util.Config;
import bx.util.Slogger;
import com.google.common.base.Preconditions;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import tools.jackson.databind.JsonNode;

@Command(
    name = "bq",
    mixinStandardHelpOptions = true,
    version = "0.0.1",
    description = "BitQuant Tool")
public class App {

  public static final String DEFAULT_DB_URL = "jdbc:duckdb:";
  static Logger logger = Slogger.forEnclosingClass();

  public static void main(String[] args) throws Exception {

    logger.atDebug().log("args: {}", List.of(args));
    Config cfg = Config.get();

    if (cfg.get("DB_URL").isEmpty()) {

      logger.atInfo().log("DB_URL not set ... using in-memory default '{}'", DEFAULT_DB_URL);
      cfg.override("DB_URL", DEFAULT_DB_URL, true);

      Preconditions.checkState(cfg.get("DB_URL").orElse("").equals(DEFAULT_DB_URL));
    }
    Db db = Db.get();

    logger.atInfo().log("db: {}", db);

    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Command(name = "fetch", description = "fetch data")
  int subCommandViaMethod(
      @Parameters(
              arity = "1..*",
              paramLabel = "<countryCode>",
              description = "country code(s) to be resolved")
          String[] countryCodes) {

    return 0;
  }

  @Command(name = "update", description = "update data")
  void subCommandViaMethod2(
      @Parameters(
              arity = "1..*",
              paramLabel = "<countryCode>",
              description = "country code(s) to be resolved")
          String[] countryCodes) {
    System.out.println("update");
  }

  @Command(name = "bcfetch", description = "update data")
  void fetchBlockChain() {

    DataSource ds = Db.get().getDataSource();
    BitcoinClient client = AmazonBitcoinClient.create();

    JsonNode info = client.getBlockChainInfo();
    int blockCount = info.path("blocks").asInt();
    BitcoinIndexer bi = new BitcoinIndexer().client(client).dataSource(ds).createTables();
    for (int i = 0; i < blockCount; i++) {

      if (bi.hasBlockInDb(i)) {
        logger.atInfo().log("already have block: {}", i);
      } else {
        bi.processBlock(i);

        if (i % 100 == 0) {
          bi.getBlockTable().show();
          bi.getTxTable().show();
        }
      }
    }
  }
}
