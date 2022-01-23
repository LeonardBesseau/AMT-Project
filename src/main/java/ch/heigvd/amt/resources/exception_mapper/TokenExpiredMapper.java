package ch.heigvd.amt.resources.exception_mapper;

import io.quarkus.security.AuthenticationFailedException;
import javax.annotation.Priority;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class TokenExpiredMapper implements ExceptionMapper<AuthenticationFailedException> {
  @Override
  public Response toResponse(AuthenticationFailedException e) {
    return Response.seeOther(UriBuilder.fromUri("/login/view").build()).build();
  }
}
