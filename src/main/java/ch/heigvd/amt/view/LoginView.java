package ch.heigvd.amt.view;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/view/login")
@ApplicationScoped
public class LoginView {

    @Inject
    @Location("LoginView/login.html")
    Template login;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getLoginPage(@HeaderParam("PasswordsNotMatching") boolean arePasswordsNotMatching) {

        if (arePasswordsNotMatching) {
            return login.data("errorPassword", "Passwords do not match");
        }

        return login.data("errorPassword", "");
    }
}
