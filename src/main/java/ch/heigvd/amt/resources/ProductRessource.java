package ch.heigvd.amt.resources;

import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.services.ProductService;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/product")
public class ProductRessource {

  private final ProductService productService;

  @Inject
  public ProductRessource(ProductService productService) {
    this.productService = productService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Product> getAll() {
    return productService.getAllProduct();
  }
}
