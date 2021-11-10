package ch.heigvd.amt.utils;

import org.jdbi.v3.core.statement.StatementException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

/**
 * @apiNote THIS CLASS IS POSTGRESQL DEPENDENT
 * @see <a href="https://www.postgresql.org/docs/current/errcodes-appendix.html">Error code for
 *     postgresql</a>
 */
public enum UpdateResult {
  SUCCESS,
  DUPLICATE,
  INVALID_REFERENCE,
  INVALID_CHECK;

  /**
   * Handle a SQL Statement error if it is an Integrity Constraint error. Rethrows it otherwise
   *
   * @param e the error to handle
   * @return an UpdateResult corresponding to the error
   * @throws StatementException if the error cannot be handled
   */
  public static UpdateResult handleUpdateError(StatementException e) {
    if (e.getCause() instanceof PSQLException) {
      PSQLException sqlException = (PSQLException) e.getCause();
      ServerErrorMessage errorMessage = sqlException.getServerErrorMessage();
      if (errorMessage != null && errorMessage.getSQLState() != null) {
        switch (errorMessage.getSQLState()) {
          case "23503":
            return INVALID_REFERENCE;
          case "23505":
            return DUPLICATE;
          case "23514":
            return INVALID_CHECK;
          default:
            break;
        }
      }
    }
    throw e;
  }
}
