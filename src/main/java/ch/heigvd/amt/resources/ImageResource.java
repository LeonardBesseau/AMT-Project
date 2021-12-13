package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.services.ImageService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/** Manages images related route */
@Path("/image")
@ApplicationScoped
public class ImageResource {

  private final ImageService imageService;

  @Inject
  @Location("defaultImageManagement.html")
  Template defaultImageManagement;

  @Inject
  public ImageResource(ImageService imageService) {
    this.imageService = imageService;
  }

  /**
   * Get the image data
   *
   * @param id the id of the image
   * @return A response containing the image data. 404 if not found
   */
  @GET
  @Path("/{id}")
  @PermitAll
  @Produces("image/png") // TODO check if can be improved
  public Response get(@PathParam("id") int id) {
    Optional<Image> image = imageService.getImage(id);
    if (image.isEmpty()) {
      return Response.status(404).build();
    }
    return Response.ok(new ByteArrayInputStream(image.get().getData())).build();
  }

  /**
   * Get the form to modify the default image
   *
   * @return an html view of the form
   */
  @GET
  @Path("/view/default")
  @RolesAllowed("Admin")
  @Produces(MediaType.TEXT_HTML)
  public Object getDefaultManagement() {
    return defaultImageManagement.data("imageError", null);
  }

  /**
   * Manages the upload of the default image
   *
   * @param input a form containing the image
   * @return a response indicating the state if failed. Redirects to the product view otherwise.
   */
  @POST
  @Path("/view/default")
  @RolesAllowed("Admin")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Object addImage(@MultipartForm MultipartFormDataInput input) throws IOException {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    List<InputPart> inputParts = uploadForm.get("image");
    for (InputPart inputPart : inputParts) {
      byte[] bytes = extractImageData(inputPart);
      if (bytes.length == 0) {
        return Response.status(Status.BAD_REQUEST).entity("The image cannot be empty").build();
      }
      UpdateResult result = imageService.updateImage(bytes, 0);
      if (result.getStatus() != UpdateStatus.SUCCESS) {
        return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity("An error occurred with the image")
            .build();
      } else {
        return Response.status(301).location(URI.create("/product/admin/view/")).build();
      }
    }
    return Response.ok().entity("No file uploaded").build();
  }

  public static byte[] extractImageData(InputPart inputPart) throws IOException {
    InputStream inputStream = inputPart.getBody(InputStream.class, null);
    return IOUtils.toByteArray(inputStream);
  }
}
