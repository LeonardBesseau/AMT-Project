package ch.heigvd.amt.database.exception;

public class InvalidReferenceException extends DatabaseGenericException {

  public InvalidReferenceException() {}

  public InvalidReferenceException(String message) {
    super(message);
  }

  public InvalidReferenceException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidReferenceException(Throwable cause) {
    super(cause);
  }

  public InvalidReferenceException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
