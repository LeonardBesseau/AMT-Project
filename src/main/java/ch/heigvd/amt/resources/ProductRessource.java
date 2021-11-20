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
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/product")
public class ProductRessource {

  private final ProductService productService;
  private final ImageService imageService;
  private final CategoryService categoryService;

  private static final Logger logger = Logger.getLogger(ProductRessource.class);

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
  Template productAddCategory;

  @Inject
  public ProductRessource(
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
  public TemplateInstance getAllView() {

    return productList.data(
        "items",
        productService.getAllProduct(),
        "categories",
        categoryService.getAllUsedCategory(),
        "filters", null);
  }

  @POST
  @Path("/view")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Object getAllViewWithFilter(MultivaluedMap<String, String> input) {
    List<String> selectedFilter = new ArrayList<>(input.keySet());
    logger.info(selectedFilter);
    logger.info(input.values());
    return productList.data(
        "items",
        productService.getAllProduct(selectedFilter),
        "categories",
        categoryService.getAllUsedCategory(),
        "filters", selectedFilter);
  }

  @GET
  @Path("/admin/view/{id}")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getView(@PathParam("id") String name) {
    Optional<Product> product = productService.getProduct(name);
    if (product.isPresent()) {
      var categories = categoryService.getAllCategory();
      return productAddCategory.data("item", product.get(), "categories", categories);
    }
    // TODO return error
    return productAddCategory.data("item", null, "categories", null);
  }

  @POST
  @Path("/admin/view/{id}")
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

      return Response.status(301).location(URI.create("/product/view/")).build();
    }
    // TODO return error
    return productAddCategory.data("item", null, "categories", null);
  }

  @POST
  @Path("/view/create")
  @Consumes({MediaType.MULTIPART_FORM_DATA})
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
      imageId = manageImage(inputParts.get(0));
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

    return Response.status(301).location(URI.create("/product/view/")).build();
  }

  @GET
  @Path("/view/create")
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
  @Path("/view/addImage")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.TEXT_HTML)
  public Object addImage(@MultipartForm MultipartFormDataInput input) {
    Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

    List<InputPart> inputParts = uploadForm.get("file");
    logger.debug("inputParts size: " + inputParts.size());
    for (InputPart inputPart : inputParts) {
      if (manageImage(inputPart) < 0) {
        return Response.ok().entity("File uploaded").build();
      } else {
        return Response.ok().entity("File non-uploaded").build();
      }
    }
    return Response.ok().entity("No file uploaded").build();
  }

  private int manageImage(InputPart inputPart) {
    // TODO do we remove the part for managing multiple files at once ?
    // TODO add image treatment to set size.
    try {
      InputStream inputStream = inputPart.getBody(InputStream.class, null);
      byte[] bytes = IOUtils.toByteArray(inputStream);
      var res = imageService.addImage(bytes);
      if (res.getStatus() == UpdateStatus.SUCCESS) {
        return res.getGeneratedId();
      }
      return -1;
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
  }
}
