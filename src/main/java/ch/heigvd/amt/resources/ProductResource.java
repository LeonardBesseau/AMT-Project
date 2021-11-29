package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.UpdateStatus;
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
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/product")
@ApplicationScoped
public class ProductResource {

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

  @GET
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
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAllView(@CookieParam("jwt_token") NewCookie jwtToken) {

    // Check if it's a member (we assume that admins can access this page too)
    boolean isMember = jwtToken != null;
    return productList.data(
        "items",
        productService.getAllProduct(),
        "categories",
        categoryService.getAllUsedCategory(),
        "filters",
        null,
        "admin",
        false,
        "member",
        isMember);
  }

  @GET
  @Path("/view/{id}")
  @Produces(MediaType.TEXT_HTML)
  public Object getAllView(
      @PathParam("id") String name, @CookieParam("jwt_token") NewCookie jwtToken) {
    boolean isMember = jwtToken != null;
    Optional<Product> product = productService.getProduct(name);
    if (product.isEmpty()) {
      return Response.status(Status.NOT_FOUND);
    }
    return productDetails.data("item", product.get(), "admin", false, "member", isMember);
  }

  @POST
  @Path("/view")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object getAllViewWithFilter(MultivaluedMap<String, String> input) {
    List<String> selectedFilter = new ArrayList<>(input.keySet());
    return productList.data(
        "items",
        productService.getAllProduct(selectedFilter),
        "categories",
        categoryService.getAllUsedCategory(),
        "filters",
        selectedFilter,
        "admin",
        false);
  }

  @GET
  @Path("/admin/view")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAdminView() {

    return productList.data(
        "items",
        productService.getAllProduct(),
        "categories",
        categoryService.getAllUsedCategory(),
        "filters",
        null,
        "admin",
        true);
  }

  @POST
  @Path("/admin/view")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object getAdminViewWithFilter(MultivaluedMap<String, String> input) {
    List<String> selectedFilter = new ArrayList<>(input.keySet());
    return productList.data(
        "items",
        productService.getAllProduct(selectedFilter),
        "categories",
        categoryService.getAllUsedCategory(),
        "filters",
        selectedFilter,
        "admin",
        true);
  }

  @GET
  @Path("/admin/view/{id}")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getDetails(@PathParam("id") String name) {
    Optional<Product> product = productService.getProduct(name);
    if (product.isPresent()) {
      var categories = categoryService.getAllCategory();
      return productAdminDetails.data(
          "item", product.get(),
          "categories", categories,
          "invalidPrice", false,
          "invalidQuantity", false,
          "imageError", false);
    }
    // TODO return error
    return productAdminDetails.data(
        "item", null,
        "categories", null,
        "invalidPrice", false,
        "invalidQuantity", false,
        "imageError", false);
  }

  @POST
  @Path("/admin/view/{id}")
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
      imageId = imageService.manageImage(inputParts.get(0));
      if (imageId < 0) {
        imageError = true;
      }
    }

    if (imageError || isQuantityInvalid || isPriceInvalid) {
      return productAdminDetails.data(
          "invalidPrice",
          isPriceInvalid,
          "invalidQuantity",
          isQuantityInvalid,
          "imageError",
          imageError);
    }

    if (productService
            .updateProduct(
                new Product(
                    name,
                    price,
                    null,
                    quantity,
                    imageId == Image.DEFAULT_IMAGE_ID ? null : new Image(imageId, null),
                    null))
            .getStatus()
        != UpdateStatus.SUCCESS) {
      return Response.status(Status.BAD_REQUEST);
    }

    return Response.status(Status.MOVED_PERMANENTLY)
        .location(URI.create("/product/admin/view/"))
        .build();
  }

  @POST
  @Path("/admin/view/{id}/category")
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

      return Response.status(301).location(URI.create("/product/admin/view/")).build();
    }
    // TODO return error
    return productAdminDetails.data("item", null, "categories", null);
  }

  @GET
  @Path("admin/view/create")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance createProductView() {
    return productAdd.data(
        "missing",
        null,
        "duplicate",
        null,
        "invalidPrice",
        null,
        "invalidQuantity",
        null,
        "imageError",
        null);
  }

  @POST
  @Path("/admin/view/create")
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
      imageId = imageService.manageImage(inputParts.get(0));
      if (imageId < 0) {
        imageError = true;
      }
    }

    if (imageError || isQuantityInvalid || isPriceInvalid || isNameMissing) {
      return productAdd.data(
          "missing",
          isNameMissing,
          "duplicate",
          null,
          "invalidPrice",
          isPriceInvalid,
          "invalidQuantity",
          isQuantityInvalid,
          "imageError",
          imageError);
    }

    if (productService
            .addProduct(
                new Product(name, price, description, quantity, new Image(imageId, null), null))
            .getStatus()
        == UpdateStatus.DUPLICATE) {
      return productAdd.data(
          "missing",
          null,
          "duplicate",
          name,
          "invalidPrice",
          null,
          "invalidQuantity",
          null,
          "imageError",
          null);
    }

    return Response.status(301).location(URI.create("/product/admin/view/")).build();
  }
}
