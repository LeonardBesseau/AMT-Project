package ch.heigvd.amt.resources;

import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.services.ImageService;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/image")
public class ImageRessource {

  private final ImageService imageService;

  @Inject
  public ImageRessource(ImageService imageService) {
    this.imageService = imageService;
  }

  @GET
  @Path("/{id}")
  @Produces("image/png")
  public Response get(@PathParam("id") int id) {
    Optional<Image> a = imageService.getImage(id);
    if (a.isEmpty()) {
      return Response.status(404).build();
    }
    return Response.ok(new ByteArrayInputStream(a.get().getData())).build();
  }
}
