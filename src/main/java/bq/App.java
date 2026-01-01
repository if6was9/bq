package bq;

import bq.provider.AmazonBitcoinClient;
import bq.provider.BitcoinClient;
import bq.provider.BitcoinMetadataExtractor;
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

    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  private void initConfig() {
    Config cfg = Config.get();

    if (cfg.get("DB_URL").isEmpty()) {

      logger.atInfo().log("DB_URL not set ... using in-memory default '{}'", DEFAULT_DB_URL);
      cfg.override("DB_URL", DEFAULT_DB_URL, true);

      Preconditions.checkState(cfg.get("DB_URL").orElse("").equals(DEFAULT_DB_URL));
    }
    Db db = Db.get();

    logger.atInfo().log("db: {}", db);
  }

  @Command(name = "fetch", description = "fetch data")
  int subCommandViaMethod(
      @Parameters(
              arity = "1..*",
              paramLabel = "<countryCode>",
              description = "country code(s) to be resolved")
          String[] countryCodes) {

    initConfig();
    return 0;
  }

  @Command(name = "update", description = "update data")
  void subCommandViaMethod2(
      @Parameters(
              arity = "1..*",
              paramLabel = "<countryCode>",
              description = "country code(s) to be resolved")
          String[] countryCodes) {
    initConfig();
    System.out.println("update");
  }

  @Command(name = "bcfetch", description = "update data")
  void fetchBlockChain() {

    BitcoinClient client = AmazonBitcoinClient.create();

    var info = client.getBlockChainInfo();

    final int lastBlock = info.path("blocks").asInt() - 1;

    DataSource ds = Db.get().getDataSource();

    BitcoinMetadataExtractor bme =
        new BitcoinMetadataExtractor()
            .client(AmazonBitcoinClient.create())
            .dataSource(ds)
            .table("block");

    bme.processBlock(lastBlock);

    while (bme.hasPrev()) {
      bme.processPrev();
    }

    bme.getTable().show();
  }
}
