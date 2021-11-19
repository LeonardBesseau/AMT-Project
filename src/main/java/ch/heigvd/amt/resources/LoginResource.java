package ch.heigvd.amt.resources;


import io.quarkus.logging.Log;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

@Path("/login")
public class LoginResource {

    private static final String REGISTER_ERROR = "registerError";
    private static final String LOGIN_ERROR    = "loginError";
    private static final String AUTHSERV_ADDR  = "http://localhost:8082";

    @Inject
    @Location("LoginView/login.html")
    Template login;

    @GET
    @Path("/view")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLoginPage() {
        return login.data(REGISTER_ERROR, null).data(LOGIN_ERROR, null);
    }

    @POST
    @Path("/signin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Object connectToExistingAccount(@FormParam("username") String username, @FormParam("password") String password) {

        try {
            Response response = sendToAuthServ("/auth/login", username, password);
            String body = response.readEntity(String.class);

            switch(response.getStatusInfo().getStatusCode()){
                case 200:
                    NewCookie[] cookies = createCookies(body);
                    return Response.status(Response.Status.MOVED_PERMANENTLY).cookie(cookies[0], cookies[1]).location(URI.create("/view/product")).build();
                case 403:
                    return login.data(REGISTER_ERROR, null, LOGIN_ERROR, "Username or password incorrect.");
                default:
                    return login.data(REGISTER_ERROR, null, LOGIN_ERROR, "Unknown error.");
            }
        }
        catch (IOException e) {
            Log.error("IOException occured");
        }
        return null;
    }

    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Object createNewAccount(@FormParam("username") String username, @FormParam("password") String password,
                                     @FormParam("confirmPassword") String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            return login.data(REGISTER_ERROR, "Passwords do not match.", LOGIN_ERROR, null);
        }

        try {
            Response response = sendToAuthServ("/accounts/register", username, password);
            String body = response.readEntity(String.class);

            switch(response.getStatusInfo().getStatusCode()) {
                case 201:
                    NewCookie[] cookies = createCookies(body);
                    return Response.status(Response.Status.CREATED).cookie(cookies[0], cookies[1]).entity("Account created").build();
                case 409:
                    return login.data(REGISTER_ERROR, "Passwords do not match.", LOGIN_ERROR, null);
                case 422:
                    break;
                default:
            }

            String result = response.readEntity(String.class);
        }
        catch (IOException e) {

        }
        return null;
    }

    private Response sendToAuthServ(String resource, String username, String password) throws IOException {

        Objects.requireNonNull(resource);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        JSONObject jsonInput = new JSONObject();
        jsonInput.put("username", username);
        jsonInput.put("password", password);

        Client client = ClientBuilder.newClient();
        return client.target(AUTHSERV_ADDR).path(resource).request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(Entity.json(jsonInput));
    }

    private NewCookie[] createCookies(String ResponseBody) {

        JSONObject jsonBody = new JSONObject(ResponseBody);
        String token = jsonBody.getString("token");
        String role = jsonBody.getJSONObject("account").getString("role");

        NewCookie cookieJWT = new NewCookie("jwt_token", token, "/", "localhost",
                "", -1, false, true);
        NewCookie cookieRole = new NewCookie("user_role", role, "/", "localhost",
                "", -1, false, false);

        return new NewCookie[]{cookieJWT, cookieRole};
    }
}