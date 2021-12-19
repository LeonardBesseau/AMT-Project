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
import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** Class allowing to serve the login page and to treat the requests which are made on it */
@Path("/login")
@ApplicationScoped
public class LoginResource {

  // names of Qute templates
  private static final String REGISTER_ERROR =
      "registerError"; // used to display the errors about registration on the login page
  private static final String REGISTER_SUCCESS =
      "registerSuccess"; // used to display that the account has been created
  private static final String LOGIN_ERROR =
      "loginError"; // used to display the errors about registration on the login page

  @ConfigProperty(name = "auth.server.url")
  String AUTHSERV_ADDR;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(); // used to parse JSON object
  private final CartService cartService;

  @Inject
  @Location("LoginView/login.html")
  Template login;

  @Inject
  LoginResource(CartService cartService) {
    this.cartService = cartService;
  }

  /**
   * Method allowing to display the login page if the cookies are not already set
   *
   * @param jwtToken cookie that contains the JWT token
   * @return either a Response to go to the home page if the cookies are already set, or the
   *     template of the login page
   */
  @GET
  @Path("/view")
  @PermitAll
  @Produces(MediaType.TEXT_HTML)
  public Object getLoginPage(@CookieParam("jwt_token") NewCookie jwtToken) {

    if (jwtToken != null) {
      String resource = "/product/view";
      String[] userInfo = getUserInfo(jwtToken);
      if (userInfo.length == 0) {
        // Parsing error
        return Response.status(Status.INTERNAL_SERVER_ERROR);
      }
      if (userInfo[1].equals("ADMIN")) {
        resource = "/product/admin/view";
      }
      return Response.status(Response.Status.MOVED_PERMANENTLY)
          .cookie(jwtToken)
          .location(URI.create(resource))
          .build();
    }
    return login.data(REGISTER_SUCCESS, null, REGISTER_ERROR, null, LOGIN_ERROR, null);
  }

  /**
   * Method treating the sign-in form fields sent by the POST request
   *
   * @param username username of the user
   * @param password password of the user
   * @return either a Response to go to the home page if the cookies are already set, or the
   *     template of the login page
   */
  @POST
  @Path("/signin")
  @PermitAll
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
          NewCookie jwtCookie = createJwtCookie(body);
          String resource = "/product/view";

          // if the user is an admin, he will be redirected to the home page for admins
          if (getUserInfo(jwtCookie)[1].equals("ADMIN")) {
            resource = "/product/admin/view";
          }
          return Response.status(Response.Status.MOVED_PERMANENTLY)
              .cookie(jwtCookie)
              .location(URI.create(resource))
              .build(); // return the home page if the login was a success
        default: // currently 403
          return login.data(
              REGISTER_SUCCESS,
              null,
              REGISTER_ERROR,
              null,
              LOGIN_ERROR,
              OBJECT_MAPPER
                  .readTree(body)
                  .get("error")
                  .asText()); // return the login page template if the login went wrong
      }
    } catch (IOException e) {
      Log.error("IOException occured");
      return Response.status(Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Method treating the sign-up form fields sent by the POST request
   *
   * @param username username of the user
   * @param password password of the user
   * @param confirmPassword repeated password of the user to match with the first one
   * @return the template of the login page with Qute parameters that indicate if there was an error
   *     or not
   */
  @POST
  @Path("/signup")
  @PermitAll
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
          cartService.addCart(username);
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

  /**
   * Method allowing to communicate with the authentication server
   *
   * @param resource resource of the authentication server where to send the data
   * @param username username of the user
   * @param password password of the user
   * @return response of the authentication server
   */
  private Response sendToAuthServ(String resource, String username, String password) {

    Objects.requireNonNull(resource);
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);

    // creating the JSON object to send
    ObjectNode jsonInput = JsonNodeFactory.instance.objectNode();
    jsonInput.put("username", username);
    jsonInput.put("password", password);

    // sending the JSON object to the authentication server and get its response
    Client client = ClientBuilder.newClient();
    return client
        .target(AUTHSERV_ADDR)
        .path(resource)
        .request(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .post(Entity.json(jsonInput.toString()));
  }

  /**
   * Method allowing to create a jwt cookie
   *
   * @param responseBody body of the response
   * @return array of the JWT token cookie and the user role cookie
   * @throws JsonProcessingException if an error occurred when parsing the JSON object of the
   *     response
   */
  private NewCookie createJwtCookie(String responseBody) throws JsonProcessingException {

    Objects.requireNonNull(responseBody);

    // Parsing the response
    JsonNode jsonBody = OBJECT_MAPPER.readTree(responseBody);
    String token = jsonBody.get("token").asText();

    return new NewCookie("jwt_token", token, "/", "localhost", "", -1, false, true);
  }

  /**
   * Method allowing to extract the username and the role of the user of the JWT token
   *
   * @param jwtToken cookie that contains the JWT token
   * @return array containing the username and the role of the user
   */
  public static String[] getUserInfo(Cookie jwtToken) {

    Objects.requireNonNull(jwtToken);
    String[] userInfo;
    try {
      String[] chunks = jwtToken.toString().split("\\.");
      JsonNode payload = OBJECT_MAPPER.readTree(new String(Base64.getDecoder().decode(chunks[1])));
      userInfo = new String[] {payload.get("sub").asText(), payload.get("groups").get(0).asText()};
    } catch (JsonProcessingException e) {
      Log.error("An error occurred while parsing the jwt token");
      throw new IllegalArgumentException("The jwt is invalid", e);
    }
    return userInfo;
  }
}
