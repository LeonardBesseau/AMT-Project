package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.exception.DatabaseGenericException;
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
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
  @RolesAllowed("ADMIN")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAll(@CookieParam("jwt_token") Cookie jwtToken) {
    return categoryList.data(
        LIST_KEY,
        categoryService.getAllCategory(),
        "username",
        LoginResource.getUserInfo(jwtToken)[0]);
  }

  /**
   * Get form to modify a category
   *
   * @return a html view
   */
  @GET
  @Path("/admin/view/create")
  @RolesAllowed("ADMIN")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getFormAdd(@CookieParam("jwt_token") Cookie jwtToken) {
    return categoryAdd.data(CATEGORY, null, "username", LoginResource.getUserInfo(jwtToken)[0]);
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
  @RolesAllowed("ADMIN")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object addCategory(@FormParam("name") String category) {
    try {
      categoryService.addCategory(new Category(category));
      return Response.status(301).location(URI.create(CATEGORY_ADMIN_VIEW_URL)).build();
    } catch (DatabaseGenericException e) {
      return categoryAdd.data(CATEGORY, category);
    }
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
  @RolesAllowed("ADMIN")
  @Produces(MediaType.TEXT_HTML)
  public Object deleteCategory(
      @PathParam("id") String category,
      @QueryParam("confirm") boolean confirm,
      @CookieParam("jwt_token") Cookie jwtToken) {
    List<Product> list = productService.getAllProduct(Collections.singletonList(category));
    if (confirm || list.isEmpty()) {
      categoryService.deleteCategory(category);
      return Response.status(Status.MOVED_PERMANENTLY)
          .location(URI.create(CATEGORY_ADMIN_VIEW_URL))
          .build();
    }
    return categoryDelete.data(
        LIST_KEY,
        list,
        CATEGORY,
        category,
        "clientDisplay",
        false,
        "username",
        LoginResource.getUserInfo(jwtToken)[0]);
  }
}
