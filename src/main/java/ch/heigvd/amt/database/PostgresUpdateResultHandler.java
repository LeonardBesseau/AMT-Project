package ch.heigvd.amt.database;

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
public class PostgresUpdateResultHandler implements UpdateResultHandler {

  public UpdateResult handleUpdateError(StatementException e) {
    if (e.getCause() instanceof PSQLException) {
      PSQLException sqlException = (PSQLException) e.getCause();
      ServerErrorMessage errorMessage = sqlException.getServerErrorMessage();
      if (errorMessage != null && errorMessage.getSQLState() != null) {
        switch (errorMessage.getSQLState()) {
          case "23503":
            return UpdateResult.INVALID_REFERENCE;
          case "23505":
            return UpdateResult.DUPLICATE;
          case "23514":
            return UpdateResult.INVALID_CHECK;
          default:
            break;
        }
      }
    }
    throw e;
  }
}
