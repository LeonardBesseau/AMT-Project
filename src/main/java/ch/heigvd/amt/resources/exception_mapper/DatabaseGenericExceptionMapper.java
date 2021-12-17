package ch.heigvd.amt.resources.exception_mapper;

import ch.heigvd.amt.database.exception.DatabaseGenericException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DatabaseGenericExceptionMapper implements ExceptionMapper<DatabaseGenericException> {

  @Override
  public Response toResponse(DatabaseGenericException e) {
    return Response.status(Status.BAD_REQUEST)
        .entity("The server could not be handled by the server")
        .build();
  }
}
