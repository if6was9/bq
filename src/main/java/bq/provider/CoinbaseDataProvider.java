package bq.provider;

import bq.BasicOHLCV;
import bq.OHLCV;
import bx.util.HttpResponseException;
import bx.util.Json;
import bx.util.S;
import bx.util.Slogger;
import bx.util.Zones;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.slf4j.Logger;
import tools.jackson.databind.JsonNode;

public class CoinbaseDataProvider extends CachingDataProvider {

  static Logger logger = Slogger.forEnclosingClass();
  RateLimiter limit = RateLimiter.create(3);

  public static final int MAX_PAGE_SIZE = 350;
  int pageSize = MAX_PAGE_SIZE;

  public LocalDate getLastClosedTradingDay() {
    return LocalDate.now(Zones.UTC).minusDays(1);
  }

  private JsonNode loadJson(String product, LocalDate start, long count) {

    if (start == null) {
      start = getLastClosedTradingDay();
    }
    long t0 = start.atStartOfDay(Zones.UTC).toEpochSecond();
    long t1 = start.plusDays(count).atStartOfDay(Zones.UTC).toEpochSecond();
    if (t0 > t1) {
      long tmp = t1;
      t1 = t0;
      t0 = tmp;
    }

    String url =
        String.format(
            "https://api.coinbase.com/api/v3/brokerage/market/products/%s/candles?granularity=ONE_DAY&start=%s&end=%s",
            product, t0, t1);

    JsonNode body = getCachedJson(url).orElse(null);
    if (body == null) {
      limit.acquire();
      logger.atInfo().log("GET {}", url);

      HttpResponse<JsonNode> response = Unirest.get(url).asObject(JsonNode.class);
      if (!response.isSuccess()) {

        throw new HttpResponseException(response.getStatus());
      }
      body = response.getBody();
      putCache(url, body);
    }

    return body;
  }

  public static OHLCV toOHLCV(JsonNode n) {

    Instant ts = Instant.ofEpochSecond(n.path("start").asLong());

    var candle =
        BasicOHLCV.ofDecimal(
            ts,
            getBigDecimal(n, "open").orElse(null),
            getBigDecimal(n, "high").orElse(null),
            getBigDecimal(n, "low").orElse(null),
            getBigDecimal(n, "close").orElse(null),
            getBigDecimal(n, "volume").orElse(null));

    return candle;
  }

  static Optional<BigDecimal> getBigDecimal(JsonNode n, String pos) {
    try {
      BigDecimal d = n.path(pos).asDecimal();
      return Optional.of(d);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  static Optional<Double> getDouble(JsonNode n, String pos) {

    try {
      double d = n.path(pos).asDouble();
      return Optional.of(d);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public Stream<OHLCV> fetch(Request request) {

    logger.atInfo().log(
        "symbol={} from={} to={}", toCoinbaseSymbol(request.symbol), request.from, request.to);

    if (request.from != null && request.to != null) {
      if (request.from.isAfter(request.to)) {
        LocalDate tmp = request.to;
        request.to = request.from;
        request.from = tmp;
      }
    }
    LocalDate notAfter = request.to;
    LocalDate notBefore = request.from;

    if (notBefore == null) {
      notBefore = getDefaultNotBefore();
    }
    if (notAfter == null) {
      notAfter = ZonedDateTime.now(Zones.UTC).toLocalDate();
    }

    if (request.isUnclosedPeriodIncluded() == false) {
      if (notAfter.isAfter(getLastClosedTradingDay())) {
        notAfter = getLastClosedTradingDay();
      }
    }
    logger.atTrace().log("start at {}", notAfter);

    List<OHLCV> results = Lists.newLinkedList();

    LocalDate ref = notAfter;

    int responseSize = 0;
    do {

      long requestCount = 0;

      requestCount = Math.min(pageSize, Math.abs(notBefore.until(notAfter, ChronoUnit.DAYS)));

      JsonNode n = loadJson(toCoinbaseSymbol(request.symbol), ref, requestCount * -1);
      logger.atTrace().log("from={} count={}", ref, requestCount * -1);
      responseSize = n.path("candles").size();

      for (OHLCV it :
          Json.asStream(n.path("candles")).map(CoinbaseDataProvider::toOHLCV).toList()) {

        if (it.getDate().isBefore(ref)) {
          ref = it.getDate();
        }

        if (!(it.getDate().isBefore(notBefore) || it.getDate().isAfter(notAfter))) {

          results.add(it);
        }
      }

      ref = ref.minus(1, ChronoUnit.DAYS);

    } while (responseSize > 0 && (notBefore == null || ref.isAfter(notBefore)));
    return results.reversed().stream();
  }

  public static String toCoinbaseSymbol(String symbol) {
    Preconditions.checkArgument(S.isNotBlank(symbol), "symbol must be provided");
    symbol = symbol.toUpperCase().trim();

    if (symbol.startsWith("X:") || symbol.startsWith("X_")) {
      symbol = symbol.substring(2);
    }
    if (symbol.endsWith("-USD")) {
      return symbol;
    }
    if (symbol.endsWith("/USD")) {
      return symbol.substring(0, symbol.length() - 4) + "-USD";
    }
    if (symbol.endsWith("_USD")) {
      return symbol.substring(0, symbol.length() - 4) + "-USD";
    }

    if (symbol.chars().allMatch(p -> Character.isAlphabetic(p))) {
      return symbol + "-USD";
    }

    return symbol.replace("/", "-").replace("_", "-");
  }
}
