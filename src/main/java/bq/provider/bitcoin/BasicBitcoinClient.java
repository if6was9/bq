package bq.provider.bitcoin;

import bq.BqException;
import bx.util.Config;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import kong.unirest.core.HttpRequestWithBody;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

public class BasicBitcoinClient extends BitcoinClient {

  static Logger logger = Slogger.forEnclosingClass();

  String url;
  String username;
  String password;

  public BasicBitcoinClient(String url, String username, String password) {
    this.url = url;
    this.password = password;
    this.username = username;
  }

  public static BitcoinClient create() {
    Config cfg = Config.get();

    String url = cfg.get("BITCOIN_RPC_URL").orElse("http://localhost:8332");
    String username = cfg.get("BITCOIN_RPC_USERNAME").orElse(null);
    String password = cfg.get("BITCOIN_RPC_PASSWORD").orElse(null);

    logger.atInfo().log("using url: {}", url);
    if (S.isBlank(username)) {
      File bitcoinCookieFile = new File(System.getProperty("user.home"), ".bitcoin/.cookie");
      if (bitcoinCookieFile.exists()) {
        try {
          String line =
              Files.asCharSource(bitcoinCookieFile, StandardCharsets.UTF_8).readFirstLine();
          List<String> parts = Splitter.on(":").splitToList(line);
          username = parts.get(0);
          password = parts.get(1);
        } catch (IOException e) {
          throw new BqException(e);
        }
      }
    }
    BasicBitcoinClient client = new BasicBitcoinClient(url, username, password);
    return client;
  }

  @Override
  protected JsonNode invokeRaw(JsonNode n) {

    Stopwatch sw = Stopwatch.createStarted();
    int status = -1;
    try {

      HttpRequestWithBody request = Unirest.post(url);

      if (S.isNotBlank(username)) {
        request = request.basicAuth(username, password);
      }

      HttpResponse<JsonNode> response = request.body(n.toString()).asObject(JsonNode.class);

      status = response.getStatus();
      if (!response.isSuccess()) {

        extractResult(response.getBody()); // this will throw

        throw new BitcoinClientException(response.getStatus());
      }
      JsonNode responseBody = response.getBody();
      if (responseBody == null) {
        responseBody = NullNode.instance;
      }

      return responseBody;
    } finally {
      logger.atInfo().log(
          "invoke method={} status={} time={}ms",
          n.path("method").asString(null),
          status,
          sw.elapsed(TimeUnit.MILLISECONDS));
    }
  }
}
