package ch.heigvd.amt.database;

import ch.heigvd.amt.database.exception.DuplicateEntryException;
import ch.heigvd.amt.database.exception.InvalidCheckConditionException;
import ch.heigvd.amt.database.exception.InvalidReferenceException;
import javax.inject.Singleton;
import org.jdbi.v3.core.statement.StatementException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

/**
 * @apiNote THIS CLASS IS POSTGRESQL DEPENDENT
 * @see <a href="https://www.postgresql.org/docs/current/errcodes-appendix.html">Error code for
 *     postgresql</a>
 */
@Singleton
public class PostgresUpdateHandler implements UpdateHandler {

  /** {@inheritDoc} */
  public void handleUpdateError(StatementException e) {
    if (e.getCause() instanceof PSQLException) {
      PSQLException sqlException = (PSQLException) e.getCause();
      ServerErrorMessage errorMessage = sqlException.getServerErrorMessage();
      if (errorMessage != null && errorMessage.getSQLState() != null) {
        switch (errorMessage.getSQLState()) {
          case "23503":
            throw new InvalidReferenceException(errorMessage.getMessage(), e);
          case "23505":
            throw new DuplicateEntryException(errorMessage.getMessage(), e);
          case "23514":
            throw new InvalidCheckConditionException(errorMessage.getMessage(), e);
          default:
            break;
        }
      }
    }
    throw e;
  }
}
