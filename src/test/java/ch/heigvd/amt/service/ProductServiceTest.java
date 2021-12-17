package ch.heigvd.amt.service;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.database.exception.DuplicateEntryException;
import ch.heigvd.amt.database.exception.InvalidReferenceException;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.services.ProductService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
class ProductServiceTest {

  private static final String CATEGORY_A_NAME = "A";
  private static final String CATEGORY_B_NAME = "B";
  private static final String UNKNOWN = "Z";
  private static final String PRODUCT_NAME_1 = "1";
  private static final String PRODUCT_NAME_2 = "2";
  private static final String PRODUCT_NAME_3 = "3";

  @Inject DataSource dataSource;

  @Inject ProductService productService;

  @BeforeEach
  void setupEach() {
    PostgisResource.runQuery(dataSource, "sql/reset_db.sql", "sql/insert_product.sql");
  }

  @Test
  void get() {
    Assertions.assertTrue(productService.getProduct(UNKNOWN).isEmpty());
    Optional<Product> result1 = productService.getProduct(PRODUCT_NAME_1);
    Assertions.assertTrue(result1.isPresent());
    Product p1 = result1.get();
    Assertions.assertEquals(PRODUCT_NAME_1, p1.getName());
    Assertions.assertEquals(2, p1.getCategories().size());

    Optional<Product> result2 = productService.getProduct(PRODUCT_NAME_2);
    Assertions.assertTrue(result2.isPresent());
    Product p2 = result2.get();
    Assertions.assertEquals(PRODUCT_NAME_2, p2.getName());
    Assertions.assertEquals(1, p2.getCategories().size());

    Optional<Product> result3 = productService.getProduct(PRODUCT_NAME_3);
    Assertions.assertTrue(result3.isPresent());
    Product p3 = result3.get();
    Assertions.assertEquals(PRODUCT_NAME_3, p3.getName());
    Assertions.assertEquals(0, p3.getCategories().size());
  }

  @Test
  void getAllNoFiltering() {
    List<Product> result1 = productService.getAllProduct();
    Assertions.assertEquals(3, result1.size());
    PostgisResource.runQuery(dataSource, "sql/reset_db.sql");
    List<Product> result2 = productService.getAllProduct();
    Assertions.assertTrue(result2.isEmpty());
  }

  @Test
  void getAllWithCategoriesFiltering() {
    List<String> categories = new ArrayList<>();
    Assertions.assertDoesNotThrow(() -> productService.getAllProduct(categories));
    Assertions.assertEquals(3, productService.getAllProduct(categories).size());

    categories.add(UNKNOWN);
    List<Product> result1 = productService.getAllProduct(categories);
    Assertions.assertTrue(result1.isEmpty());

    // Check intersection with one invalid
    categories.add(CATEGORY_B_NAME);
    List<Product> result2 = productService.getAllProduct(categories);
    Assertions.assertEquals(0, result2.size());

    categories.remove(UNKNOWN);
    List<Product> result3 = productService.getAllProduct(categories);
    Assertions.assertEquals(1, result3.size());

    // Check intersection with both valid
    categories.add(CATEGORY_A_NAME);
    List<Product> result4 = productService.getAllProduct(categories);
    Assertions.assertEquals(1, result4.size());

    categories.remove(CATEGORY_B_NAME);
    List<Product> result5 = productService.getAllProduct(categories);
    Assertions.assertEquals(2, result5.size());
  }

  @Test
  void addCategory() {
    Product product = productService.getProduct(PRODUCT_NAME_3).orElseThrow();
    Assertions.assertTrue(product.getCategories().isEmpty());

    Assertions.assertDoesNotThrow(
        () -> productService.addCategory(PRODUCT_NAME_3, CATEGORY_A_NAME));
    product = productService.getProduct(PRODUCT_NAME_3).orElseThrow();
    Assertions.assertEquals(1, product.getCategories().size());
    Assertions.assertEquals(CATEGORY_A_NAME, product.getCategories().get(0).getName());

    // Check idempotent
    Assertions.assertDoesNotThrow(
        () -> productService.addCategory(PRODUCT_NAME_3, CATEGORY_A_NAME));
    product = productService.getProduct(PRODUCT_NAME_3).orElseThrow();
    Assertions.assertEquals(1, product.getCategories().size());
    Assertions.assertEquals(CATEGORY_A_NAME, product.getCategories().get(0).getName());

    Assertions.assertDoesNotThrow(
        () -> productService.addCategory(PRODUCT_NAME_3, CATEGORY_B_NAME));
    product = productService.getProduct(PRODUCT_NAME_3).orElseThrow();
    Assertions.assertEquals(2, product.getCategories().size());

    Assertions.assertThrows(
        InvalidReferenceException.class, () -> productService.addCategory(PRODUCT_NAME_3, UNKNOWN));
    Assertions.assertThrows(
        InvalidReferenceException.class,
        () -> productService.addCategory(UNKNOWN, CATEGORY_A_NAME));
  }

  @Test
  void removeCategory() {
    Product product = productService.getProduct(PRODUCT_NAME_1).orElseThrow();
    Assertions.assertEquals(2, product.getCategories().size());

    productService.removeCategory(product.getName(), CATEGORY_A_NAME);
    product = productService.getProduct(PRODUCT_NAME_1).orElseThrow();
    Assertions.assertEquals(1, product.getCategories().size());

    productService.removeCategory(product.getName(), UNKNOWN);
    product = productService.getProduct(PRODUCT_NAME_1).orElseThrow();
    Assertions.assertEquals(1, product.getCategories().size());
  }

  @Test
  void addProduct() {
    Product newProduct =
        new Product("New product", 10.0, "Test product", 1, Image.DEFAULT_IMAGE, null);
    Assertions.assertDoesNotThrow(() -> productService.addProduct(newProduct));

    Assertions.assertThrows(
        DuplicateEntryException.class, () -> productService.addProduct(newProduct));
  }

  @Test
  void updateProduct() {
    Product invalidProduct =
        new Product(UNKNOWN, 10.0, "Test product", 1, Image.DEFAULT_IMAGE, null);
    Assertions.assertDoesNotThrow(() -> productService.updateProduct(invalidProduct));

    double price = 99999999.999;
    Product updatedProduct =
        new Product(PRODUCT_NAME_1, price, "Test product", 1, Image.DEFAULT_IMAGE, null);
    Assertions.assertDoesNotThrow(() -> productService.updateProduct(updatedProduct));

    Product product = productService.getProduct(PRODUCT_NAME_1).orElseThrow();
    Assertions.assertEquals(price, product.getPrice());
  }
}
