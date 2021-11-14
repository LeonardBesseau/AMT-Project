package ch.heigvd.amt.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.TEXT_HTML)
@Path("/login")
public class LoginResource {

    @POST
    @Path("signin")
    public Response connectToExistingAccount(@FormParam("name") String name, @FormParam("password") String password) {


        return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/view/product")).build();
    }

    @POST
    @Path("signup")
    public Response createNewAccount(@FormParam("name") String name, @FormParam("password") String password,
                                     @FormParam("confirmPassword") String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/view/login")).header("PasswordsNotMatching", true).build();
        }


        return Response.status(Response.Status.CREATED).build();
    }
}
