package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.services.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Path("/cart")
@ApplicationScoped
public class CartResource {

    private final CartService cartService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    public TemplateInstance getCart(@CookieParam("jwt_token") NewCookie jwtToken) {

        String username = "Visitor";
        boolean isMember = false;
        List<CartProduct> products = null;

        if (jwtToken != null) {
            username = getUsernameFromJWT(jwtToken);
            products = cartService.getAllProduct(username);
            isMember = true;
        }

        return cart.data("admin", false, "member", isMember,
                "username", username, "products", products);
    }

    @POST
    @Path("/product")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response addProduct(@CookieParam("jwt_token") NewCookie jwtToken,
                               @FormParam("product_name") String productName,
                               @FormParam("product_quantity") Integer productQuantity) {

        // Check if logged in
        if (jwtToken == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        // Try to get the username from jwt
        String username = getUsernameFromJWT(jwtToken);
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
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateProduct(@CookieParam("jwt_token") NewCookie jwtToken,
                                  @PathParam("name") String productName,
                                  @FormParam("product_quantity") Integer productQuantity) {

        // Check if logged in
        if (jwtToken == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        // Try to get the username from jwt
        String username = getUsernameFromJWT(jwtToken);
        if (username == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        // Update the quantity
        if (productQuantity > 0) {
            UpdateResult status = cartService.updateProductQuantity(username, productName, productQuantity);
            if (status != UpdateResult.success()) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            // Delete product if quantity is 0 or below and refresh page
            cartService.deleteProduct(username, productName);
            URI uri = null;
            try {
                uri = new URI("/cart/view");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return Response.seeOther(uri).build();
        }
        return Response.status(Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("/product/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteProduct(@CookieParam("jwt_token") NewCookie jwtToken,
                                  @PathParam("name") String productName) {

        // Check if logged in
        if (jwtToken == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        // Try to get the username from jwt
        String username = getUsernameFromJWT(jwtToken);
        if (username == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        cartService.deleteProduct(username, productName);
        return Response.ok().build();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response clearCart(@CookieParam("jwt_token") NewCookie jwtToken) {

        // Check if logged in
        if (jwtToken == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        // Try to get the username from jwt
        String username = getUsernameFromJWT(jwtToken);
        if (username == null) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        cartService.clear(username);
        return Response.ok().build();
    }

    /**
     * Get the username from the JWT token
     *
     * @param jwtToken JWT token
     * @return username or null if a parsing error occurred
     * @throws NullPointerException - if the jwtToken is null
     */
    private String getUsernameFromJWT(NewCookie jwtToken) throws NullPointerException {
        Objects.requireNonNull(jwtToken);
        String username = null;
        try {
            String[] chunks = jwtToken.toString().split("\\.");
            JsonNode payload = OBJECT_MAPPER.readTree(new String(Base64.getDecoder().decode(chunks[1])));
            username = payload.get("sub").asText();
        } catch (JsonProcessingException e) {
            Log.error("An error occurred while parsing the jwt token");
        }
        return username;
    }
}
