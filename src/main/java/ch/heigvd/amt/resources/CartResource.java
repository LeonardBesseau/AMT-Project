package ch.heigvd.amt.resources;

import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.services.CartService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/cart")
@ApplicationScoped
public class CartResource {

  private final CartService cartService;
  private static final String SERVER_ERROR_URL = "/html/500.html";

  @Inject
  @Location("CartView/cart.html")
  Template cart;

  @Inject
  CartResource(CartService cartService) {
    this.cartService = cartService;
  }

  /**
   * Get the cart page corresponding to the user session or simple visitor
   *
   * @return a html page of the cart
   */
  @GET
  @Path("/view")
  @PermitAll
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getCart(@CookieParam("jwt_token") Cookie jwtToken) {

    String username = "Visitor";
    boolean isMember = false;
    List<CartProduct> products = null;

    if (jwtToken != null) {
      username = LoginResource.getUserInfo(jwtToken)[0];
      products = cartService.getAllProduct(username);
      isMember = true;
    }

    return cart.data(
        "admin", false, "member", isMember, "username", username, "products", products);
  }

  @POST
  @Path("/product")
  @RolesAllowed("MEMBER")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response addProduct(
      @CookieParam("jwt_token") Cookie jwtToken,
      @FormParam("product_name") String productName,
      @FormParam("product_quantity") Integer productQuantity) {

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return redirectTo(SERVER_ERROR_URL);
    }

    // Add product or update the quantity if it already exists
    CartProduct product = new CartProduct(productName, null, null, productQuantity);
    cartService.addProduct(username, product);
    return Response.status(Status.NO_CONTENT).build();
  }

  @POST
  @Path("/product/{name}")
  @RolesAllowed("MEMBER")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response updateProduct(
      @CookieParam("jwt_token") Cookie jwtToken,
      @PathParam("name") String productName,
      @FormParam("product_quantity") Integer productQuantity) {

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return redirectTo(SERVER_ERROR_URL);
    }

    // Update the quantity
    if (productQuantity > 0) {
      cartService.updateProductQuantity(username, productName, productQuantity);
    } else {
      // Delete product if quantity is 0 or below and refresh page
      cartService.deleteProduct(username, productName);
      return redirectTo("/cart/view");
    }
    return Response.status(Status.NO_CONTENT).build();
  }

  @DELETE
  @Path("/product/{name}")
  @RolesAllowed("MEMBER")
  @Produces(MediaType.TEXT_HTML)
  public Response deleteProduct(
      @CookieParam("jwt_token") Cookie jwtToken, @PathParam("name") String productName) {

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return redirectTo(SERVER_ERROR_URL);
    }

    cartService.deleteProduct(username, productName);
    return Response.ok().build();
  }

  @DELETE
  @RolesAllowed("MEMBER")
  @Produces(MediaType.TEXT_HTML)
  public Response clearCart(@CookieParam("jwt_token") Cookie jwtToken) {

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return redirectTo(SERVER_ERROR_URL);
    }

    cartService.clearCart(username);
    return Response.ok().build();
  }

  /**
   * Redirect user's to a page
   *
   * @param url url of the page
   * @return page to redirect the user to
   */
  private Response redirectTo(String url) {
    Response response = null;
    try {
      URI uri = new URI(url);
      response = Response.seeOther(uri).build();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return response;
  }
}
