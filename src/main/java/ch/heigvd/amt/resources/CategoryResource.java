package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.services.CategoryService;
import ch.heigvd.amt.services.ProductService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/category")
@ApplicationScoped
public class CategoryResource {

  private static final String CATEGORY = "category";
  private final CategoryService categoryService;
  private final ProductService productService;

  // Inject the template html.
  // We have to specify the path to the template from the template folder
  @Inject
  @Location("category/categoryList.html")
  Template productList;

  @Inject
  @Location("category/categoryAdd.html")
  Template categoryAdd;

  @Inject
  @Location("category/categoryDelete.html")
  Template categoryDelete;

  @Inject
  public CategoryResource(CategoryService categoryService, ProductService productService) {
    this.categoryService = categoryService;
    this.productService = productService;
  }

  /**
   * Get the view with the list of all products
   *
   * @return a html page with the list of all products
   */
  @GET
  @Path("/admin/view/")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAll() {
    return productList.data("items", categoryService.getAllCategory());
  }

  @GET
  @Path("/admin/view/create")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getFormAdd() {
    return categoryAdd.data(CATEGORY, null);
  }

  @POST
  @Path("/admin/create")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object addCategory(@FormParam("name") String category) {
    if (categoryService.addCategory(new Category(category)).getStatus() == UpdateStatus.SUCCESS) {
      return Response.status(301).location(URI.create("/category/admin/view/")).build();
    }
    return categoryAdd.data(CATEGORY, category);
  }

  @POST
  @Path("/admin/delete/{id}")
  @Produces(MediaType.TEXT_HTML)
  public Object deleteCategory(
      @PathParam("id") String category, @QueryParam("confirm") boolean confirm) {
    List<Product> list = productService.getAllProduct(Collections.singletonList(category));
    if (confirm || list.isEmpty()) {
      categoryService.deleteCategory(category);
      return Response.status(301).location(URI.create("/category/admin/view/")).build();
    }
    return categoryDelete.data("items", list, "clientDisplay", false);
  }
}
