package ch.heigvd.amt.resources.exception_mapper;

import ch.heigvd.amt.database.exception.DuplicateEntryException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class DuplicateExceptionMapper implements ExceptionMapper<DuplicateEntryException> {

  @Override
  public Response toResponse(DuplicateEntryException e) {
    return Response.status(Status.BAD_REQUEST).entity("The given entry already exists").build();
  }
}
