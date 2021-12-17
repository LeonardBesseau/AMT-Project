package ch.heigvd.amt.database.exception;

public class InvalidCheckConditionException extends DatabaseGenericException {

  public InvalidCheckConditionException() {}

  public InvalidCheckConditionException(String message) {
    super(message);
  }

  public InvalidCheckConditionException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidCheckConditionException(Throwable cause) {
    super(cause);
  }

  public InvalidCheckConditionException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
