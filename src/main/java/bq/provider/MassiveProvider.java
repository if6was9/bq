package bq.provider;

import bq.BasicOHLCV;
import bq.OHLCV;
import bx.util.BxException;
import bx.util.Config;
import bx.util.Json;
import bx.util.S;
import bx.util.Slogger;
import bx.util.Zones;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

public class MassiveProvider extends CachingDataProvider {

  static Logger logger = Slogger.forEnclosingClass();

  String apiKey = null;

  public String getApiKey() {
    Optional<String> val = S.notBlank(apiKey);
    if (val.isPresent()) {
      return val.get();
    }
    val = Config.get().get("MASSIVE_API_KEY");
    if (val.isPresent()) {
      return val.get();
    }
    throw new IllegalStateException("Massive API Key Not Set (env: MASSIVE_API_KEY)");
  }

  public LocalDate getDefaultNotBefore() {
    return LocalDate.now(Zones.UTC).minusYears(2);
  }

  @Override
  protected Stream<OHLCV> fetch(Request request) {

    // https://api.massive.com/v2/aggs/ticker/AAPL/range/1/day/2023-01-09/2023-02-10\?adjusted\=true\&sort\=asc\&limit\=120\&apiKey\=fQJn0eXk0jzCLMdopVmeYlF1BX4ZrJVC

    String from = "";
    String to = null;
    if (request.from == null) {

      from = getDefaultNotBefore().toString();
    } else {
      from = request.from.toString();
    }
    if (request.to == null) {
      to = LocalDate.now(Zones.UTC).toString();
    } else {
      to = request.to.toString();
    }

    String url =
        String.format(
            "https://api.massive.com/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc",
            request.symbol, from, to);

    logger.atInfo().log("GET {}", url);

    JsonNode body = null;
    body = getCachedJson(url).orElse(null);

    if (body == null) {
      kong.unirest.core.HttpResponse<tools.jackson.databind.JsonNode> response =
          Unirest.get(url)
              .headerReplace("Accept", "application/json")
              .headerReplace("Authorization", String.format("Bearer %s", getApiKey()))
              .asObject(tools.jackson.databind.JsonNode.class);

      int rc = response.getStatus();
      if (!response.isSuccess()) {
        JsonNode n = response.getBody();
        if (n == null) {
          n = MissingNode.getInstance();
        }

        String msg = String.format("rc=%s msg=%s", rc, n.path("error").asString(""));
        throw new BxException(msg);
      }

      body = response.getBody();
      putCache(url, body);
    }

    // timestamps from massive for daily aggregates use 00:00 NYC as the start period
    return Json.asStream(body.path("results"))
        .map(this::toOHLCV)
        .filter(
            it -> {
              if (request.isUnclosedPeriodIncluded() == false) {
                if (it.getDate().isAfter(getLastClosedTradingDay())) {
                  return false;
                }
              }
              ;

              return true;
            });
  }

  public LocalDate getLastClosedTradingDay() {
    return LocalDate.now(Zones.UTC).minusDays(1);
  }

  OHLCV toOHLCV(JsonNode n) {

    var d =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(n.path("t").asLong()), Zones.UTC)
            .toLocalDate();

    ;
    return BasicOHLCV.ofDecimal(
        d,
        (BigDecimal) n.path("o").asDecimalOpt().orElse(null),
        (BigDecimal) n.path("h").asDecimalOpt().orElse(null),
        (BigDecimal) n.path("l").asDecimalOpt().orElse(null),
        (BigDecimal) n.path("c").asDecimalOpt().orElse(null),
        (BigDecimal) n.path("v").asDecimalOpt().orElse(null));
  }
}
