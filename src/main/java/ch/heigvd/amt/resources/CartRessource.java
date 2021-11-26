package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.services.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Base64;
import java.util.List;

@Path("/cart")
@ApplicationScoped
public class CartRessource {

    private final CartService cartService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

        // #TODO(check for session and get specific member cart)
        return cart.data("isMember", false, "admin", false);
    }

    @POST
    @Path("/product")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response addProduct(@CookieParam("jwt_token") NewCookie jwtToken,
                               @FormParam("product_name") String productName,
                               @FormParam("product_quantity") Integer productQuantity) {

        // Get the username from the jwt token
        String username;
        try {
            String[] chunks = jwtToken.toString().split("\\.");
            JsonNode payload = OBJECT_MAPPER.readTree(new String(Base64.getDecoder().decode(chunks[1])));
            username = payload.get("sub").asText();
        } catch (JsonProcessingException e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }

        // Add to the cart or update quantity if it already exists
        CartProduct product = new CartProduct(productName, null, null, productQuantity);
        UpdateResult status = cartService.addProduct(username, product);
        if (status == UpdateResult.success()) {
            return Response.status(Status.NO_CONTENT).build();
        } else {
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
