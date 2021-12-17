package ch.heigvd.amt.resources;

import static ch.heigvd.amt.resources.ImageResource.extractImageData;

import ch.heigvd.amt.database.exception.DatabaseGenericException;
import ch.heigvd.amt.database.exception.DuplicateEntryException;
import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.services.CategoryService;
import ch.heigvd.amt.services.ImageService;
import ch.heigvd.amt.services.ProductService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

/** Manages product related route */
@Path("/product")
@ApplicationScoped
public class ProductResource {

  private static final String LIST_KEY = "items";
  private static final String CATEGORIES_LIST_KEY = "categories";
  private static final String FILTERS_LIST_KEY = "filters";
  private static final String ADMIN_KEY = "admin";
  public static final String ITEM_KEY = "item";
  public static final String INVALID_PRICE_KEY = "invalidPrice";
  public static final String INVALID_QUANTITY_KEY = "invalidQuantity";
  public static final String IMAGE_ERROR = "imageError";
  public static final String PRODUCTS_ADMIN_VIEW_URL = "/product/admin/view/";
  public static final String MISSING_KEY = "missing";
  public static final String DUPLICATE_KEY = "duplicate";
  private final ProductService productService;
  private final ImageService imageService;
  private final CategoryService categoryService;

  private static final Logger logger = Logger.getLogger(ProductResource.class);

  // Inject the template html.
  // We have to specify the path to the template from the template folder
  @Inject
  @Location("product/shop.html")
  Template productList;

  @Inject
  @Location("product/productAdd.html")
  Template productAdd;

  @Inject
  @Location("product/productDetailsAdmin.html")
  Template productAdminDetails;

  @Inject
  @Location("product/product-details.html")
  Template productDetails;

  @Inject
  public ProductResource(
      ProductService productService, ImageService imageService, CategoryService categoryService) {
    this.productService = productService;
    this.imageService = imageService;
    this.categoryService = categoryService;
  }

  /**
   * Get product list as JSON
   *
   * @return a list of product
   */
  @GET
  @PermitAll
  @Produces(MediaType.APPLICATION_JSON)
  public List<Product> getAll() {
    return productService.getAllProduct();
  }

  /**
   * Get the view with the list of all products
   *
   * @return a html page with the list of all products
   */
  @GET
  @Path("/view")
  @PermitAll
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAllView(@CookieParam("jwt_token") NewCookie jwtToken) {

    // Check if it's a member (we assume that admins can access this page too)
    boolean isMember = jwtToken != null;
    return productList.data(
        LIST_KEY,
        productService.getAllProduct(),
        CATEGORIES_LIST_KEY,
        categoryService.getAllUsedCategory(),
        FILTERS_LIST_KEY,
        null,
        ADMIN_KEY,
        false,
        "member",
        isMember);
  }

  /**
   * Get the details of a product for a user
   *
   * @param name the identifier of the product
   * @return 404 if not exist. A view
   */
  @GET
  @Path("/view/{id}")
  @PermitAll
  @Produces(MediaType.TEXT_HTML)
  public Object getAllView(
      @PathParam("id") String name, @CookieParam("jwt_token") NewCookie jwtToken) {
    boolean isMember = jwtToken != null;
    Optional<Product> product = productService.getProduct(name);
    if (product.isEmpty()) {
      return Response.status(Status.NOT_FOUND);
    }
    return productDetails.data(ITEM_KEY, product.get(), ADMIN_KEY, false, "member", isMember);
  }

  /**
   * Manage the filter for the view
   *
   * @param input the filter to apply
   * @return the updated view
   */
  @POST
  @Path("/view")
  @PermitAll
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object getAllViewWithFilter(MultivaluedMap<String, String> input) {
    List<String> selectedFilter = new ArrayList<>(input.keySet());
    return productList.data(
        LIST_KEY,
        productService.getAllProduct(selectedFilter),
        CATEGORIES_LIST_KEY,
        categoryService.getAllUsedCategory(),
        FILTERS_LIST_KEY,
        selectedFilter,
        ADMIN_KEY,
        false);
  }

  /**
   * Get the view with the list of all products
   *
   * @return a html page with the list of all products
   */
  @GET
  @Path("/admin/view")
  @RolesAllowed("Admin")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAdminView() {

    return productList.data(
        LIST_KEY,
        productService.getAllProduct(),
        CATEGORIES_LIST_KEY,
        categoryService.getAllUsedCategory(),
        FILTERS_LIST_KEY,
        null,
        ADMIN_KEY,
        true);
  }

  /**
   * Manage the filter for the view
   *
   * @param input the filter to apply
   * @return the updated view
   */
  @POST
  @Path("/admin/view")
  @RolesAllowed("Admin")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object getAdminViewWithFilter(MultivaluedMap<String, String> input) {
    List<String> selectedFilter = new ArrayList<>(input.keySet());
    return productList.data(
        LIST_KEY,
        productService.getAllProduct(selectedFilter),
        CATEGORIES_LIST_KEY,
        categoryService.getAllUsedCategory(),
        FILTERS_LIST_KEY,
        selectedFilter,
        ADMIN_KEY,
        true);
  }

  /**
   * Get the form view to modify a product
   *
   * @param name the identifier of the product
   * @return the form
   */
  @GET
  @Path("/admin/view/{id}")
  @RolesAllowed("Admin")
  @Produces(MediaType.TEXT_HTML)
  public Object getDetails(@PathParam("id") String name) {
    Optional<Product> product = productService.getProduct(name);
    if (product.isPresent()) {
      var categories = categoryService.getAllCategory();
      return productAdminDetails.data(
          ITEM_KEY, product.get(),
          CATEGORIES_LIST_KEY, categories,
          INVALID_PRICE_KEY, false,
          INVALID_QUANTITY_KEY, false,
          IMAGE_ERROR, false);
    }
    return Response.status(Status.BAD_REQUEST);
  }

  /**
   * Manage the form update for a product about its details
   *
   * @param input the form data
   * @param name the identifier of the object
   * @return the form with error indication. Redirects otherwise
   * @throws IOException if an error occurs while managing the image
   */
  @POST
  @Path("/admin/view/{id}")
  @RolesAllowed("Admin")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Object updateProduct(
      @MultipartForm MultipartFormDataInput input, @PathParam("id") String name)
      throws IOException {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
    boolean isPriceInvalid = false;
    boolean isQuantityInvalid = false;
    boolean imageError = false;

    Double price = null;
    Integer quantity = null;
    int imageId = Image.DEFAULT_IMAGE_ID;

    String res = uploadForm.get("price").get(0).getBodyAsString();
    if (!res.isEmpty()) {
      try {
        price = Double.parseDouble(res);
        if (price < 0) {
          isPriceInvalid = true;
        }
      } catch (NumberFormatException e) {
        isPriceInvalid = true;
      }
    }

    res = uploadForm.get("quantity").get(0).getBodyAsString();
    try {
      quantity = Integer.parseInt(res);
      if (quantity < 0) {
        isQuantityInvalid = true;
      }
    } catch (NumberFormatException e) {
      isQuantityInvalid = true;
    }

    List<InputPart> inputParts = uploadForm.get("image");
    if (!inputParts.isEmpty()) {
      try {
        imageId = imageService.addImage(extractImageData(inputParts.get(0)));
      } catch (DatabaseGenericException e) {
        imageError = true;
      }
    }

    if (imageError || isQuantityInvalid || isPriceInvalid) {
      return productAdminDetails.data(
          INVALID_PRICE_KEY,
          isPriceInvalid,
          INVALID_QUANTITY_KEY,
          isQuantityInvalid,
          IMAGE_ERROR,
          imageError);
    }

    productService.updateProduct(
        new Product(
            name,
            price,
            null,
            quantity,
            imageId == Image.DEFAULT_IMAGE_ID ? null : new Image(imageId, null),
            null));

    return Response.status(Status.MOVED_PERMANENTLY)
        .location(URI.create(PRODUCTS_ADMIN_VIEW_URL))
        .build();
  }

  /**
   * Manage the form update for a product about its categories
   *
   * @param name the identifier of the product
   * @param input the category to be associated with the product
   * @return reidrects when successful.
   */
  @POST
  @Path("/admin/view/{id}/category")
  @RolesAllowed("Admin")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object updateCategoryForProduct(
      @PathParam("id") String name, MultivaluedMap<String, String> input) {
    Optional<Product> product = productService.getProduct(name);
    if (product.isPresent()) {
      List<Category> categories = product.get().getCategories();
      // remove deleted category
      categories.stream()
          .map(Category::getName)
          .filter(s -> input.get(s) == null)
          .forEach(s -> productService.removeCategory(name, s));

      // add categories (we do not care about duplicates and invalid categories will be ignored)
      input.values().stream()
          .map(strings -> strings.get(0))
          .forEach(s -> productService.addCategory(name, s));

      return Response.status(301).location(URI.create(PRODUCTS_ADMIN_VIEW_URL)).build();
    }
    return Response.status(Status.BAD_REQUEST);
  }

  /**
   * Get the product creation form
   *
   * @return the form
   */
  @GET
  @Path("admin/view/create")
  @RolesAllowed("Admin")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance createProductView() {
    return productAdd.data(
        MISSING_KEY,
        null,
        DUPLICATE_KEY,
        null,
        INVALID_PRICE_KEY,
        null,
        INVALID_QUANTITY_KEY,
        null,
        IMAGE_ERROR,
        null);
  }

  /**
   * Manage the product form creation
   *
   * @param input the form data
   * @return the form with the error indicated. Redirects otherwise
   * @throws IOException if an error occurs while managing the image
   */
  @POST
  @Path("/admin/view/create")
  @RolesAllowed("Admin")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Object addProduct(@MultipartForm MultipartFormDataInput input) throws IOException {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    boolean isNameMissing;
    boolean isPriceInvalid = false;
    boolean isQuantityInvalid = false;
    boolean imageError = false;

    String name = uploadForm.get("name").get(0).getBodyAsString();
    String description;
    Double price = null;
    Integer quantity = null;
    int imageId = Image.DEFAULT_IMAGE_ID;

    isNameMissing = name.isEmpty();

    description = uploadForm.get("description").get(0).getBodyAsString();

    String res = uploadForm.get("price").get(0).getBodyAsString();
    if (!res.isEmpty()) {
      try {
        price = Double.parseDouble(res);
        if (price < 0) {
          isPriceInvalid = true;
        }
      } catch (NumberFormatException e) {
        isPriceInvalid = true;
      }
    }

    res = uploadForm.get("quantity").get(0).getBodyAsString();
    try {
      quantity = Integer.parseInt(res);
      if (quantity < 0) {
        isQuantityInvalid = true;
      }
    } catch (NumberFormatException e) {
      isQuantityInvalid = true;
    }

    List<InputPart> inputParts = uploadForm.get("image");
    if (!inputParts.isEmpty()) {
      try {
        imageId = imageService.addImage(extractImageData(inputParts.get(0)));
      } catch (DatabaseGenericException e) {
        imageError = true;
      }
    }

    if (imageError || isQuantityInvalid || isPriceInvalid || isNameMissing) {
      return productAdd.data(
          MISSING_KEY,
          isNameMissing,
          DUPLICATE_KEY,
          null,
          INVALID_PRICE_KEY,
          isPriceInvalid,
          INVALID_QUANTITY_KEY,
          isQuantityInvalid,
          IMAGE_ERROR,
          imageError);
    }

    try {
      productService.addProduct(
          new Product(name, price, description, quantity, new Image(imageId, null), null));
    } catch (DuplicateEntryException e) {
      return productAdd.data(
          MISSING_KEY,
          null,
          DUPLICATE_KEY,
          name,
          INVALID_PRICE_KEY,
          null,
          INVALID_QUANTITY_KEY,
          null,
          IMAGE_ERROR,
          null);
    }

    return Response.status(301).location(URI.create(PRODUCTS_ADMIN_VIEW_URL)).build();
  }

  @GET
  @Path("/crash")
  public Object test() {
    throw new DuplicateEntryException();
  }
}
