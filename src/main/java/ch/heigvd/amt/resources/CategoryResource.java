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
import javax.annotation.security.RolesAllowed;
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

/** Manages category related routes */
@Path("/category")
@ApplicationScoped
public class CategoryResource {

  private static final String CATEGORY = "category";
  private static final String CATEGORY_ADMIN_VIEW_URL = "/category/admin/view/";
  private static final String LIST_KEY = "items";

  private final CategoryService categoryService;
  private final ProductService productService;

  // Inject the template html.
  // We have to specify the path to the template from the template folder
  @Inject
  @Location("category/categoryList.html")
  Template categoryList;

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
   * Get the view for the admin with the list of all products
   *
   * @return a html page with the list of all products
   */
  @GET
  @Path("/admin/view/")
  @RolesAllowed("Admin")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAll() {
    return categoryList.data(LIST_KEY, categoryService.getAllCategory());
  }

  /**
   * Get form to modify a category
   *
   * @return a html view
   */
  @GET
  @Path("/admin/view/create")
  @RolesAllowed("Admin")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getFormAdd() {
    return categoryAdd.data(CATEGORY, null);
  }

  /**
   * Manages the categoryCreation form
   *
   * @param category a string the name of the category (Must be a valid category, is validated by
   *     the db)
   * @return the form page if invalid. Redirects to the category view otherwise
   */
  @POST
  @Path("/admin/create")
  @RolesAllowed("Admin")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object addCategory(@FormParam("name") String category) {
    if (categoryService.addCategory(new Category(category)).getStatus() == UpdateStatus.SUCCESS) {
      return Response.status(301).location(URI.create(CATEGORY_ADMIN_VIEW_URL)).build();
    }
    return categoryAdd.data(CATEGORY, category);
  }

  /**
   * Manages the deleteCategory Form
   *
   * @param category the identifier of the category
   * @param confirm A boolean to indicates the warning for used category can be skipped. (The
   *     warning has been shown)
   * @return a warning page displaying the products associated if the category is used. Otherwise,
   *     redirects to the category list view
   */
  @POST
  @Path("/admin/delete/{id}")
  @RolesAllowed("Admin")
  @Produces(MediaType.TEXT_HTML)
  public Object deleteCategory(
      @PathParam("id") String category, @QueryParam("confirm") boolean confirm) {
    List<Product> list = productService.getAllProduct(Collections.singletonList(category));
    if (confirm || list.isEmpty()) {
      categoryService.deleteCategory(category);
      return Response.status(301).location(URI.create(CATEGORY_ADMIN_VIEW_URL)).build();
    }
    return categoryDelete.data(LIST_KEY, list, CATEGORY, category, "clientDisplay", false);
  }
}
