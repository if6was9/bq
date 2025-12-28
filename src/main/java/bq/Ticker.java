package bq;

import bx.util.S;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.util.Set;

public class Ticker {

  TickerType type;
  String unqualified;

  static Set<String> KNOWN_INDICES = Set.of("RUT", "NQ", "NDQ", "SPX", "DJIA");
  static Set<String> KNOWN_CRYPTO =
      Set.of(
          "BTC", "ETH", "ADA", "DOGE", "WIF", "SUI", "SOL", "HYPE", "RNDR", "RENDER", "FETCH",
          "XRP", "XMR", "USDT", "BNB", "USDC", "UNI", "AVAX", "LTC", "BCH", "WBTC", "USDS", "ZEC",
          "LINK", "USDE", "SHIB", "PYUSD", "TON", "XLM", "WLFI", "MNT", "CRO", "USD1", "DOT", "M",
          "XAUT", "TAO", "PEPE", "OP", "ARB", "POL", "MATIC", "ASTER", "ATOM", "ONDO", "FIL",
          "NEAR", "ETC");

  public static enum TickerType {
    STOCK,
    INDEX,
    CRYPTO;

    public String getPrefix() {
      switch (this) {
        case STOCK:
          return "S";
        case INDEX:
          return "I";

        case CRYPTO:
          return "X";
        default:
      }
      throw new IllegalArgumentException("invalid TickerType." + this);
    }
  }

  public boolean isCrypto() {
    return type == TickerType.CRYPTO;
  }

  public boolean isIndex() {
    return type == TickerType.INDEX;
  }

  public boolean isStock() {
    return type == TickerType.STOCK;
  }

  public String getSymbol() {
    return this.unqualified;
  }

  public TickerType getType() {
    return this.type;
  }

  public String getQualifiedSymbol() {

    return String.format("%s:%s", type.getPrefix(), getSymbol());
  }

  public static Ticker of(TickerType type, String unqualified) {
    Preconditions.checkArgument(type != null);
    Preconditions.checkArgument(S.isNotBlank(unqualified));

    Ticker t = new Ticker();
    t.type = type;
    t.unqualified = unqualified.trim().toUpperCase();

    check(t);

    return t;
  }

  public static Ticker of(String input) {

    String s = input;
    Preconditions.checkArgument(s != null);
    if (s.startsWith("$")) {
      s = s.substring(1);
    }
    Preconditions.checkArgument(S.isNotBlank(s), "symbol not specified: %s", s);

    s = s.trim().toUpperCase();
    int colon = s.indexOf(":");
    if (colon < 0) {
      colon = s.indexOf("_");
    }
    if (colon >= 0) {
      String prefix = s.substring(0, colon).toUpperCase().trim();
      String unqualified = s.substring(colon + 1).toUpperCase().trim();
      Ticker t = new Ticker();

      Preconditions.checkArgument(S.isNotBlank(unqualified), "invalid symbol: %s", input);

      t.unqualified = unqualified;
      if (prefix.equalsIgnoreCase("X")) {
        t.type = TickerType.CRYPTO;
      } else if (prefix.equalsIgnoreCase("I")) {
        t.type = TickerType.INDEX;
      } else if (prefix.equalsIgnoreCase("S")) {
        t.type = TickerType.STOCK;
      } else {

        Preconditions.checkArgument(false, "invalid prefix: %s (%s)", prefix, s);
      }
      return check(t);
    }
    if (KNOWN_INDICES.contains(s.toUpperCase())) {
      Ticker t = new Ticker();
      t.type = TickerType.INDEX;
      t.unqualified = s;
      return check(t);
    }
    if (KNOWN_CRYPTO.contains(s.toUpperCase()) || s.trim().length() > 4) {
      Ticker t = new Ticker();
      t.type = TickerType.CRYPTO;
      t.unqualified = s;
      return check(t);
    }
    Ticker t = new Ticker();
    t.type = TickerType.STOCK;
    t.unqualified = s;
    return check(t);
  }

  private static Ticker check(Ticker t) {
    Preconditions.checkNotNull(t);
    Preconditions.checkArgument(t.getType() != null, "invalid symbol: %s", t);
    Preconditions.checkArgument(S.isNotBlank(t.getSymbol()), "invalid symbol: %s", t.getSymbol());
    Preconditions.checkArgument(
        t.getSymbol().chars().allMatch(c -> Character.isLetterOrDigit(c)),
        "invalid symbol: %s",
        t.getSymbol());
    return t;
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("symbol", getSymbol())
        .add("type", this.type)
        .toString();
  }
}
