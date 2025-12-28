package bq.provider;

import bx.util.Json;
import bx.util.Rounding;
import bx.util.Slogger;
import com.google.common.hash.Hashing;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import tools.jackson.databind.JsonNode;

public abstract class CachingDataProvider extends DataProvider {

  static org.slf4j.Logger logger = Slogger.forEnclosingClass();

  File cacheDir =
      new File(new File(System.getProperty("java.io.tmpdir"), "bq"), getClass().getName());

  static AtomicLong invalidBefore = new AtomicLong(0);

  static AtomicLong cacheHitCount = new AtomicLong();
  static AtomicLong cacheAttemptCount = new AtomicLong();

  public CachingDataProvider() {
    super();
    cacheDir.mkdirs();

    logger.atDebug().log("cache dir: {}", cacheDir);
  }

  public void invalidateAll() {
    invalidBefore.set(System.currentTimeMillis());
  }

  String toKey(String url) {
    return Hashing.sha256().hashString(url, StandardCharsets.UTF_8).toString();
  }

  File getCachedFile(String url) {
    return new File(cacheDir, toKey(url) + ".json");
  }

  public boolean isExpired(File f) {
    if (f == null || !f.exists()) {
      return true;
    }
    long lastModified = f.lastModified();
    if (lastModified < invalidBefore.get()) {
      return true;
    }
    if (lastModified <= 0) {
      return true;
    }
    long age = System.currentTimeMillis() - lastModified;

    long secs = TimeUnit.SECONDS.convert(age, TimeUnit.MILLISECONDS);

    if (secs < 60) {
      return false;
    }
    return true;
  }

  public synchronized Optional<JsonNode> getCachedJson(String url) {
    try {
      cacheAttemptCount.incrementAndGet();
      File file = getCachedFile(url);

      if (!isExpired(file)) {

        JsonNode n = Json.readTree(file);
        cacheHitCount.incrementAndGet();
        return Optional.of(n);
      }
      return Optional.empty();
    } finally {
      if (cacheAttemptCount.get() % 10 == 0) {
        logger.atInfo().log(
            "cache hit={} total={} ratio={}",
            cacheHitCount.get(),
            cacheAttemptCount.get(),
            Rounding.format(
                cacheHitCount.get() / (double) Math.max(1, cacheAttemptCount.get()), 2));
      }
    }
  }

  public synchronized void putCache(String url, JsonNode data) {
    File file = new File(cacheDir, toKey(url) + ".json");
    if (data != null && data.isObject() || data.isArray()) {
      Json.mapper().writeValue(file, data);
    }
  }
}
