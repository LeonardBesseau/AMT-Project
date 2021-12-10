package ch.heigvd.amt.database;

/** Status of update operation */
public enum UpdateStatus {
  SUCCESS,
  DUPLICATE,
  INVALID_REFERENCE,
  INVALID_CHECK
}
