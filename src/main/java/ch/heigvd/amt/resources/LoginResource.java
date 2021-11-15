package ch.heigvd.amt.resources;


import io.quarkus.logging.Log;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Objects;

public class LoginResource {

    private static final String REGISTER_ERROR = "registerError";
    private static final String LOGIN_ERROR    = "loginError";
    private static final String AUTHSERV_ADDR  = "http://10.0.1.192:8080";

    @Inject
    @Location("LoginView/login.html")
    Template login;

    @GET
    @Path("/view/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLoginPage() {
        return login.data(REGISTER_ERROR, null).data(LOGIN_ERROR, null);
    }

    @POST
    @Path("/login/signin")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object connectToExistingAccount(@FormParam("username") String username, @FormParam("password") String password) {

        try {
            CloseableHttpResponse response = sendToAuthServ("/auth/login", username, password);
            String body = EntityUtils.toString(response.getEntity());

            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_OK:

                    JSONObject jsonBody = new JSONObject(body);
                    String token = jsonBody.getString("token");
                    String role = jsonBody.getJSONObject("account").getString("role");

                    // Cookie duration : 1 day in seconds
                    int cookieDuration = 3600 * 24;
                    NewCookie cookieJWT = new NewCookie("jwt_token", token, "", null, null, cookieDuration, false, true);
                    NewCookie cookieRole = new NewCookie("user_role", role, "", null, null, cookieDuration, false, false);

                    return Response.status(Response.Status.MOVED_PERMANENTLY).cookie(cookieJWT, cookieRole).location(URI.create("/view/product")).build();
                case HttpStatus.SC_FORBIDDEN:
                    return login.data(REGISTER_ERROR, null).data(LOGIN_ERROR, "Username or password incorrect.");
                default:
                    return login.data(REGISTER_ERROR, null).data(LOGIN_ERROR, "Unknown error.");
            }
        }
        catch (IOException e) {
            Log.error("IOException occured");
        }
        return null;
    }

    @POST
    @Path("/login/signup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object createNewAccount(@FormParam("username") String username, @FormParam("password") String password,
                                     @FormParam("confirmPassword") String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            return login.data(REGISTER_ERROR, "Passwords do not match.");
        }

        try {
            CloseableHttpResponse response = sendToAuthServ("/accounts/register", username, password);

            switch(response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_CREATED:
                    return Response.status(Response.Status.CREATED).entity("Account created").build();
                case HttpStatus.SC_CONFLICT:
                    break;
                case HttpStatus.SC_UNPROCESSABLE_ENTITY:
                    break;
                default:
            }

            String result = EntityUtils.toString(response.getEntity());
        }
        catch (IOException e) {

        }
        return null;
    }

    private CloseableHttpResponse sendToAuthServ(String resource, String username, String password) throws IOException {

        Objects.requireNonNull(resource);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        HttpPost post = new HttpPost(AUTHSERV_ADDR + resource);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");

        JSONObject jsonInput = new JSONObject();
        jsonInput.put("username", username);
        jsonInput.put("password", password);

        // send a JSON data
        post.setEntity(new StringEntity(jsonInput.toString()));


        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return httpClient.execute(post);
        }
    }
}