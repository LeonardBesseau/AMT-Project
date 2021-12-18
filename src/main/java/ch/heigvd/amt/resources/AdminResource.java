package ch.heigvd.amt.resources;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/** Manages admin specific pages */
@Path("/admin")
@ApplicationScoped
public class AdminResource {

  @Inject
  @Location("admin.html")
  Template adminMainPage;

  @GET
  @Path("/view")
  @RolesAllowed("ADMIN")
  @Produces(MediaType.TEXT_HTML)
  public Object getAdminPanel() {
    return adminMainPage.instance();
  }
}
