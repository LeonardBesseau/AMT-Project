package ch.heigvd.amt.resources;

import java.net.URI;
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

@Path("/logout")
@ApplicationScoped
public class LogoutResource {

  @GET
  @Path("/")
  @PermitAll
  @Produces(MediaType.TEXT_HTML)
  public Object logout(@CookieParam("jwt_token") NewCookie jwtToken) {
    String resource = "/login/view";
    // Put maxAge to 0 to delete the cookie
    jwtToken = new NewCookie("jwt_token", null, "/", "localhost", "", 0, false, true);

    return Response.status(Response.Status.FOUND)
        .cookie(jwtToken)
        .location(URI.create(resource))
        .build();
  }
}
