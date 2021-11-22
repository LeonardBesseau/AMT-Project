package ch.heigvd.amt.resources;

import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.services.CartService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/cart")
@ApplicationScoped
public class CartRessource {

  private final CartService cartService;

  // Inject the template html.
  @Inject
  @Location("CartView/cart.html")
  Template cart;

  @Inject
  CartRessource(CartService cartService) {
    this.cartService = cartService;
  }

  /**
   * Get the cart page corresponding to the user session or simple visitor
   *
   * @return a html page of the cart
   */
  @GET
  @Path("/view")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getCart() {

    List<CartProduct> yo = cartService.getAllProduct(1);

    // #TODO(check for session and get specific member cart)
    return cart.data("isMember", false, "admin", false);
  }
}
