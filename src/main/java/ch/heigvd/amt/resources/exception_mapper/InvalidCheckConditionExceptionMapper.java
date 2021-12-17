package ch.heigvd.amt.resources.exception_mapper;

import ch.heigvd.amt.database.exception.InvalidCheckConditionException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidCheckConditionExceptionMapper
    implements ExceptionMapper<InvalidCheckConditionException> {

  @Override
  public Response toResponse(InvalidCheckConditionException e) {
    return Response.status(Status.BAD_REQUEST)
        .entity("The given data did not satisfy the requirements")
        .build();
  }
}
