package ch.heigvd.amt.service;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.services.ProductService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
class ProductServiceTest {

  @Inject DataSource dataSource;

  @Inject ProductService productService;

  @Test
  void get() {
    PostgisResource.runQuery(
        dataSource, "sql/init_db.sql", "sql/reset_db.sql", "sql/insert_product.sql");
    Assertions.assertTrue(productService.getProduct("Z").isEmpty());
    Optional<Product> result1 = productService.getProduct("1");
    Assertions.assertTrue(result1.isPresent());
    Product p1 = result1.get();
    Assertions.assertEquals("1", p1.getName());
    Assertions.assertEquals(2, p1.getCategories().size());

    Optional<Product> result2 = productService.getProduct("2");
    Assertions.assertTrue(result2.isPresent());
    Product p2 = result2.get();
    Assertions.assertEquals("2", p2.getName());
    Assertions.assertEquals(1, p2.getCategories().size());

    Optional<Product> result3 = productService.getProduct("3");
    Assertions.assertTrue(result3.isPresent());
    Product p3 = result3.get();
    Assertions.assertEquals("3", p3.getName());
    Assertions.assertEquals(0, p3.getCategories().size());
  }

  @Test
  void getAll() {
    PostgisResource.runQuery(
        dataSource, "sql/init_db.sql", "sql/reset_db.sql", "sql/insert_product.sql");
    List<Product> result1 = productService.getAllProduct();
    Assertions.assertEquals(3, result1.size());
    PostgisResource.runQuery(dataSource, "sql/reset_db.sql");
    List<Product> result2 = productService.getAllProduct();
    Assertions.assertTrue(result2.isEmpty());
  }
}
