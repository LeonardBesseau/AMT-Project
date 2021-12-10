package ch.heigvd.amt.resources;

import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.services.ImageService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Object addImage(@MultipartForm MultipartFormDataInput input) {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    List<InputPart> inputParts = uploadForm.get("image");
    for (InputPart inputPart : inputParts) {
      if (imageService.manageImage(inputPart, 0) > -1) {
        return Response.status(301).location(URI.create("/product/admin/view/")).build();
      } else {
        return Response.ok().entity("File non-uploaded").build();
      }
    }
    return Response.ok().entity("No file uploaded").build();
  }
}
