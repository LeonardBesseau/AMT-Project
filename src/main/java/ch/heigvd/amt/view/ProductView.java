package ch.heigvd.amt.view;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.services.ImageService;
import ch.heigvd.amt.services.ProductService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/view/product")
@ApplicationScoped
public class ProductView {

  private final ProductService productService;
  private final ImageService imageService;

  private static final Logger logger = Logger.getLogger(ProductView.class);

  // Inject the template html.
  // We have to specify the path to the template from the template folder
  @Inject
  @Location("product/productListDisplay.html")
  Template productList;

  @Inject
  public ProductView(ProductService productService, ImageService imageService) {
    this.productService = productService;
    this.imageService = imageService;
  }

  /**
   * Get the view with the list of all products
   *
   * @return a html page with the list of all products
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAll() {
    return productList.data("items", productService.getAllProduct());
  }

  @POST
  @Path("/addImage")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Object addImage(@MultipartForm MultipartFormDataInput input) {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    List<InputPart> inputParts = uploadForm.get("file");
    logger.debug("inputParts size: " + inputParts.size());
    // TODO do we remove the part for managing multiple files at once ?
    // TODO add image treatment to set size.
    for (InputPart inputPart : inputParts) {
      try {

        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        byte[] bytes = IOUtils.toByteArray(inputStream);
        if (imageService.addImage(bytes) == UpdateResult.SUCCESS) {
          return Response.ok().entity("All files successfully.").build();
        }
        return Response.ok().entity("Error with database").build();
      } catch (Exception e) {
        e.printStackTrace();
        return Response.ok().entity("Error with file").build();
      }
    }
    return Response.ok().entity("No file uploaded").build();
  }
}
