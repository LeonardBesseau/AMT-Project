package ch.heigvd.amt.database.exception;

public class DatabaseGenericException extends RuntimeException {
  public DatabaseGenericException() {}

  public DatabaseGenericException(String message) {
    super(message);
  }

  public DatabaseGenericException(String message, Throwable cause) {
    super(message, cause);
  }

  public DatabaseGenericException(Throwable cause) {
    super(cause);
  }

  public DatabaseGenericException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
