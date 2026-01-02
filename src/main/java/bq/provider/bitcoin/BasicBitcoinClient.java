package bq.provider.bitcoin;

import bx.util.Config;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import kong.unirest.core.HttpRequestWithBody;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

public class BasicBitcoinClient extends BitcoinClient {

  Logger logger = Slogger.forEnclosingClass();

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

    BasicBitcoinClient client =
        new BasicBitcoinClient(
            cfg.get("BITCOIN_RPC_URL")
                .orElseThrow(
                    () -> {
                      return new IllegalStateException("BITCOIN_RPC_URL not set");
                    }),
            cfg.get("BITCOIN_RPC_USERNAME")
                .orElseThrow(
                    () -> {
                      return new IllegalStateException("BITCOIN_RPC_USERNAME not set");
                    }),
            cfg.get("BITCOIN_RPC_PASSWORD")
                .orElseThrow(
                    () -> {
                      return new IllegalStateException("BITCOIN_RPC_PASSWORD not set");
                    }));

    return client;
  }

  @Override
  public JsonNode invokeRaw(JsonNode n) {

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
