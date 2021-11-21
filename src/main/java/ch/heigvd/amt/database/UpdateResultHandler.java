package ch.heigvd.amt.database;

import org.jdbi.v3.core.statement.StatementException;

public interface UpdateResultHandler {

  /**
   * Handle a SQL Statement error if it is an Integrity Constraint error. Rethrows it otherwise
   *
   * @param e the error to handle
   * @return an UpdateResult corresponding to the error
   * @throws StatementException if the error cannot be handled
   */
  public UpdateResult handleUpdateError(StatementException e);
}
