package ch.heigvd.amt.database.exception;

public class DuplicateEntryException extends DatabaseGenericException {
  public DuplicateEntryException() {}

  public DuplicateEntryException(String message) {
    super(message);
  }

  public DuplicateEntryException(String message, Throwable cause) {
    super(message, cause);
  }

  public DuplicateEntryException(Throwable cause) {
    super(cause);
  }

  public DuplicateEntryException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
