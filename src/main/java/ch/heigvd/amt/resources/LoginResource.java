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
import java.util.Base64;
import java.util.Objects;

@Path("/login")
public class LoginResource {

    private static final String REGISTER_ERROR   = "registerError";
    private static final String REGISTER_SUCCESS = "registerSuccess";
    private static final String LOGIN_ERROR      = "loginError";
    private static final String LOGGED           = "logged";
    private static final String AUTHSERV_ADDR    = "http://localhost:8082";

    @Inject
    @Location("LoginView/login.html")
    Template login;

    @GET
    @Path("/view")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLoginPage(@CookieParam("jwt_token") NewCookie jwtToken, @CookieParam("user_role") NewCookie userRole) {

        if (jwtToken != null && userRole != null) {

            String[] chunks = jwtToken.toString().split("\\.");
            Base64.Decoder decoder = Base64.getDecoder();
            //String payload = new String(decoder.decode(chunks[1]));
            JSONObject payload = new JSONObject(new String(decoder.decode(chunks[1])));
            payload.getString("sub");
            payload.getString("role");

            return login.data(REGISTER_SUCCESS, null, REGISTER_ERROR, null, LOGIN_ERROR, null, LOGGED, payload.getString("role") + " : " + payload.getString("sub"));
        }

        return login.data(REGISTER_SUCCESS, null, REGISTER_ERROR, null, LOGIN_ERROR, null, LOGGED, null);
    }

    @POST
    @Path("/signin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Object connectToExistingAccount(@FormParam("username") String username, @FormParam("password") String password) {

        try {
            Response response = sendToAuthServ("/auth/login", username, password);
            String body = response.readEntity(String.class);

            // we keep the switch structure if we have to add codes that produce different behaviours
            switch(response.getStatusInfo().getStatusCode()){
                case 200:
                    NewCookie[] cookies = createCookies(body);
                    return Response.status(Response.Status.MOVED_PERMANENTLY).cookie(cookies[0], cookies[1]).location(URI.create("/view/product")).build();
                default: // currently 403
                    return login.data(REGISTER_SUCCESS, null, REGISTER_ERROR, null, LOGIN_ERROR, new JSONObject(body).getString("error"), LOGGED, null);
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
            return login.data(REGISTER_SUCCESS, null, REGISTER_ERROR, "Passwords do not match", LOGIN_ERROR, null, LOGGED, null);
        }

        try {
            Response response = sendToAuthServ("/accounts/register", username, password);
            String body = response.readEntity(String.class);

            switch(response.getStatusInfo().getStatusCode()) {
                case 201:
                    return login.data(REGISTER_SUCCESS, "Account created", REGISTER_ERROR, null, LOGIN_ERROR, null, LOGGED, null);
                case 409:
                    return login.data(REGISTER_SUCCESS, null, REGISTER_ERROR, new JSONObject(body).getString("error"), LOGIN_ERROR, null, LOGGED, null);
                case 422:
                    return login.data(REGISTER_SUCCESS, null,
                            REGISTER_ERROR, new JSONObject(body).getJSONArray("errors").getJSONObject(0).getString("message"),
                            LOGIN_ERROR, null, LOGGED, null);
            }
        }
        catch (IOException e) {
            Log.error("IOException occured");
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
        return client.target(AUTHSERV_ADDR).path(resource).request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(Entity.json(jsonInput.toString()));
    }

    private NewCookie[] createCookies(String ResponseBody) {

        Objects.requireNonNull(ResponseBody);

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