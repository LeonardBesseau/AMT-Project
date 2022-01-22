package ch.heigvd.amt.services.exception;

public class CDNNotReachableException extends RuntimeException {
  public CDNNotReachableException() {}

  public CDNNotReachableException(String message) {
    super(message);
  }

  public CDNNotReachableException(String message, Throwable cause) {
    super(message, cause);
  }

  public CDNNotReachableException(Throwable cause) {
    super(cause);
  }

  public CDNNotReachableException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
