package bq;

public class BqException extends RuntimeException {

  public BqException(String message) {
    super(message);
  }

  public BqException(Throwable cause) {
    super(cause);
  }

  public BqException(String message, Throwable cause) {
    super(message, cause);
  }

  public BqException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
