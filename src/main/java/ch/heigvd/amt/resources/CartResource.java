package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.services.CartService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/cart")
@ApplicationScoped
public class CartResource {

  public static final String LOGIN_VIEW_URL = "/login/view";
  private final CartService cartService;

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
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response addProduct(
      @CookieParam("jwt_token") Cookie jwtToken,
      @FormParam("product_name") String productName,
      @FormParam("product_quantity") Integer productQuantity) {

    // Check if logged in
    if (jwtToken == null) {
      return redirectTo(LOGIN_VIEW_URL);
    }

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    // Add product or update the quantity if it already exists
    CartProduct product = new CartProduct(productName, null, null, productQuantity);
    UpdateResult status = cartService.addProduct(username, product);
    if (status == UpdateResult.success()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @Path("/product/{name}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response updateProduct(
      @CookieParam("jwt_token") Cookie jwtToken,
      @PathParam("name") String productName,
      @FormParam("product_quantity") Integer productQuantity) {

    // Check if logged in
    if (jwtToken == null) {
      return redirectTo(LOGIN_VIEW_URL);
    }

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    // Update the quantity
    if (productQuantity > 0) {
      UpdateResult status =
          cartService.updateProductQuantity(username, productName, productQuantity);
      if (status != UpdateResult.success()) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
    } else {
      // Delete product if quantity is 0 or below and refresh page
      cartService.deleteProduct(username, productName);
      return redirectTo("/cart/view");
    }
    return Response.status(Status.NO_CONTENT).build();
  }

  @DELETE
  @Path("/product/{name}")
  @Produces(MediaType.TEXT_HTML)
  public Response deleteProduct(
      @CookieParam("jwt_token") Cookie jwtToken, @PathParam("name") String productName) {

    // Check if logged in
    if (jwtToken == null) {
      return redirectTo(LOGIN_VIEW_URL);
    }

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    cartService.deleteProduct(username, productName);
    return Response.ok().build();
  }

  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response clearCart(@CookieParam("jwt_token") Cookie jwtToken) {

    // Check if logged in
    if (jwtToken == null) {
      return redirectTo(LOGIN_VIEW_URL);
    }

    // Try to get the username from jwt
    String username = LoginResource.getUserInfo(jwtToken)[0];
    if (username == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
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
