package ch.heigvd.amt.resources;

import ch.heigvd.amt.services.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.logging.Log;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/login")
@ApplicationScoped
public class LoginResource {

  private static final String REGISTER_ERROR = "registerError";
  private static final String REGISTER_SUCCESS = "registerSuccess";
  private static final String LOGIN_ERROR = "loginError";
  private static final String AUTHSERV_ADDR = "http://10.0.1.92:8080";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final CartService cartService;

  @Inject
  @Location("LoginView/login.html")
  Template login;

  @Inject
  LoginResource(CartService cartService) {
    this.cartService = cartService;
  }

  @GET
  @Path("/view")
  @Produces(MediaType.TEXT_HTML)
  public Object getLoginPage(
      @CookieParam("jwt_token") NewCookie jwtToken, @CookieParam("user_role") NewCookie userRole) {

    try {
      if (jwtToken != null && userRole != null) {

        String resource = "/product/view";
        if (getUserInfo(jwtToken)[1].equals("admin")) {
          resource = "/product/admin/view";
        }
        return Response.status(Response.Status.MOVED_PERMANENTLY)
            .cookie(jwtToken, userRole)
            .location(URI.create(resource))
            .build();
      }
      return login.data(REGISTER_SUCCESS, null, REGISTER_ERROR, null, LOGIN_ERROR, null);
    } catch (JsonProcessingException e) {
      Log.error("JsonProcessingException occured");
      return Response.status(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @POST
  @Path("/signin")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object connectToExistingAccount(
      @FormParam("username") String username, @FormParam("password") String password) {

    try {
      Response response = sendToAuthServ("/auth/login", username, password);
      String body = response.readEntity(String.class);

      // we keep the switch structure if we have to add codes that produce different behaviours
      switch (response.getStatusInfo().getStatusCode()) {
        case 200:
          NewCookie[] cookies = createCookies(body);
          String resource = "/product/view";
          if (getUserInfo(cookies[0])[1].equals("admin")) {
            resource = "/product/admin/view";
          }
          return Response.status(Response.Status.MOVED_PERMANENTLY)
              .cookie(cookies[0], cookies[1])
              .location(URI.create(resource))
              .build();
        default: // currently 403
          return login.data(
              REGISTER_SUCCESS,
              null,
              REGISTER_ERROR,
              null,
              LOGIN_ERROR,
              OBJECT_MAPPER.readTree(body).get("error").asText());
      }
    } catch (IOException e) {
      Log.error("IOException occured");
      return Response.status(Status.INTERNAL_SERVER_ERROR);
    }
  }

  @POST
  @Path("/signup")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object createNewAccount(
      @FormParam("username") String username,
      @FormParam("password") String password,
      @FormParam("confirmPassword") String confirmPassword) {

    if (!password.equals(confirmPassword)) {
      return login.data(
          REGISTER_SUCCESS, null, REGISTER_ERROR, "Passwords do not match", LOGIN_ERROR, null);
    }

    try {
      Response response = sendToAuthServ("/accounts/register", username, password);
      String body = response.readEntity(String.class);

      switch (response.getStatusInfo().getStatusCode()) {
        case 201:
          // Create cart for the new user
          cartService.add(username);

          return login.data(
              REGISTER_SUCCESS, "Account created", REGISTER_ERROR, null, LOGIN_ERROR, null);
        case 409:
          return login.data(
              REGISTER_SUCCESS,
              null,
              REGISTER_ERROR,
              OBJECT_MAPPER.readTree(body).get("error").asText(),
              LOGIN_ERROR,
              null);
        case 422:
          return login.data(
              REGISTER_SUCCESS,
              null,
              REGISTER_ERROR,
              OBJECT_MAPPER.readTree(body).get("errors").get(0).get("message").asText(),
              LOGIN_ERROR,
              null);
        default:
          return Response.status(Status.NOT_ACCEPTABLE);
      }
    } catch (IOException e) {
      Log.error("IOException occured");
      return Response.status(Status.INTERNAL_SERVER_ERROR);
    }
  }

  private Response sendToAuthServ(String resource, String username, String password)
      throws IOException {

    Objects.requireNonNull(resource);
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);

    ObjectNode jsonInput = JsonNodeFactory.instance.objectNode();
    jsonInput.put("username", username);
    jsonInput.put("password", password);

    Client client = ClientBuilder.newClient();
    return client
        .target(AUTHSERV_ADDR)
        .path(resource)
        .request(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .post(Entity.json(jsonInput.toString()));
  }

  private NewCookie[] createCookies(String ResponseBody) throws JsonProcessingException {

    Objects.requireNonNull(ResponseBody);

    JsonNode jsonBody = OBJECT_MAPPER.readTree(ResponseBody);
    String token = jsonBody.get("token").asText();
    String role = jsonBody.get("account").get("role").asText();

    NewCookie cookieJWT = new NewCookie("jwt_token", token, "/", "localhost", "", -1, false, true);
    NewCookie cookieRole = new NewCookie("user_role", role, "/", "localhost", "", -1, false, false);

    return new NewCookie[] {cookieJWT, cookieRole};
  }

  public static String[] getUserInfo(NewCookie jwtToken) throws JsonProcessingException {

    Objects.requireNonNull(jwtToken);

    String[] chunks = jwtToken.toString().split("\\.");
    JsonNode payload = OBJECT_MAPPER.readTree(new String(Base64.getDecoder().decode(chunks[1])));

    return new String[] {payload.get("sub").asText(), payload.get("role").asText()};
  }
}
