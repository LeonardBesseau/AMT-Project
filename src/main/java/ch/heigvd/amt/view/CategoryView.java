package ch.heigvd.amt.view;

import ch.heigvd.amt.services.CategoryService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/view/admin/category")
@ApplicationScoped
public class CategoryView {

  private final CategoryService categoryService;

  // Inject the template html.
  // We have to specify the path to the template from the template folder
  @Inject
  @Location("category/categoryList.html")
  Template productList;

  @Inject
  public CategoryView(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  /**
   * Get the view with the list of all products
   *
   * @return a html page with the list of all products
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAll() {
    return productList.data("items", categoryService.getAllCategory());
  }
}
