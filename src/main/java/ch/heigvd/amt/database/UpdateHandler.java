package ch.heigvd.amt.database;

import org.jdbi.v3.core.statement.StatementException;

/** Handler for update operation on databases */
public interface UpdateHandler {

  /**
   * Convert a generic sql error into a more precise one. Rethrows the generic one if not handled
   *
   * @param e the error to handle
   * @throws StatementException if the error cannot be handled
   */
  public void handleUpdateError(StatementException e);
}
