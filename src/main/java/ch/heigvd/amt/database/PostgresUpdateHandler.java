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
public class PostgresUpdateHandler implements UpdateHandler {

  public UpdateResult handleUpdateError(StatementException e) {
    if (e.getCause() instanceof PSQLException) {
      PSQLException sqlException = (PSQLException) e.getCause();
      ServerErrorMessage errorMessage = sqlException.getServerErrorMessage();
      if (errorMessage != null && errorMessage.getSQLState() != null) {
        switch (errorMessage.getSQLState()) {
          case "23503":
            return new UpdateResult(UpdateStatus.INVALID_REFERENCE);
          case "23505":
            return new UpdateResult(UpdateStatus.DUPLICATE);
          case "23514":
            return new UpdateResult(UpdateStatus.INVALID_CHECK);
          default:
            break;
        }
      }
    }
    throw e;
  }
}
