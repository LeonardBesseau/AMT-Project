package ch.heigvd.amt.resources.exception_mapper;

import ch.heigvd.amt.services.exception.CDNNotReachableException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CDNNotReachableExceptionMapper implements ExceptionMapper<CDNNotReachableException> {

  @Override
  public Response toResponse(CDNNotReachableException e) {
    return Response.status(Status.NOT_FOUND).entity("The image server is not available").build();
  }
}
