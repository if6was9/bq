package bq.provider.bitcoin;

import bx.util.HttpResponseException;
import bx.util.S;

public class BitcoinClientException extends HttpResponseException {

  int jsonRpcErrorCode = -1;

  public BitcoinClientException(int httpStatus) {
    super(httpStatus);
  }

  public BitcoinClientException(int httpStatus, String message, int code) {
    super(httpStatus, String.format("%s (code=%s)", S.notBlank(message).orElse("").trim(), code));
    this.jsonRpcErrorCode = code;
  }

  public BitcoinClientException(int httpStatus, String message) {
    super(httpStatus, message);
  }

  public int getCode() {
    return this.jsonRpcErrorCode;
  }
}
