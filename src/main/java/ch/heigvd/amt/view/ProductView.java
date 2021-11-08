package ch.heigvd.amt.view;

import ch.heigvd.amt.services.ProductService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/view/product")
@ApplicationScoped
public class ProductView {

  private final ProductService productService;

  // Inject the template html.
  // We have to specify the path to the template from the template folder
  @Inject
  @Location("ProductView/productList.html")
  Template productList;

  @Inject
  public ProductView(ProductService productService) {
    this.productService = productService;
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
}
