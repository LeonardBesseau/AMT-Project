package ch.heigvd.amt.resources.exception_mapper;
import io.quarkus.security.UnauthorizedException;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(1)
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {
    @Override
    public Response toResponse(UnauthorizedException e) {
        return Response.seeOther(UriBuilder.fromUri("/html/500.html").build()).build();
    }
}