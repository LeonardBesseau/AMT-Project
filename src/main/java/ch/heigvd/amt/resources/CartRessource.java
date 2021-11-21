package ch.heigvd.amt.resources;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/cart")
@ApplicationScoped
public class CartRessource {

  // Inject the template html.
  @Inject
  @Location("CartView/cart.html")
  Template cart;

  /**
   * Get the cart page corresponding to the user session or simple visitor
   *
   * @return a html page of the cart
   */
  @GET
  @Path("/view")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getCart() {
    // #TODO(check for session and get specific member cart)
    return cart.data("isMember", false, "admin", false);
  }
}
