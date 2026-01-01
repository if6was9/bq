package bq.provider.bitcoin;

import bx.util.HttpResponseException;

public class BitcoinClientException extends HttpResponseException {

  public BitcoinClientException(int httpStatus) {
    super(httpStatus);
  }

  public BitcoinClientException(int httpStatus, String message) {
    super(httpStatus, message);
  }
}
