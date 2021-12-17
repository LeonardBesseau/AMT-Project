package ch.heigvd.amt.resources.exception_mapper;

import ch.heigvd.amt.database.exception.InvalidReferenceException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidReferenceExceptionMapper implements ExceptionMapper<InvalidReferenceException> {

  @Override
  public Response toResponse(InvalidReferenceException e) {
    return Response.status(Status.BAD_REQUEST)
        .entity("The given data had unsatisfied references")
        .build();
  }
}
