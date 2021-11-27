package ch.heigvd.amt.service;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.services.CartService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
public class CartServiceTest {

  private static final String USERNAME = "test";
  private static final CartProduct PRODUCT_1 = new CartProduct("1", null, null, 1);
  private static final CartProduct PRODUCT_2 = new CartProduct("2", null, null, 2);

  @Inject DataSource dataSource;

  @Inject CartService cartService;

  @BeforeEach
  void setupEach() {
    PostgisResource.runQuery(
        dataSource,
        "sql/init_db.sql",
        "sql/reset_db.sql",
        "sql/insert_product.sql",
        "sql/insert_cart.sql");
  }

  @Test
  void getAllProductCartExist() {
    List<CartProduct> result1 = cartService.getAllProduct(USERNAME);
    Assertions.assertEquals(2, result1.size());
  }

  @Test
  void getAllProductCartDoesNotExist() {
    List<CartProduct> result2 = cartService.getAllProduct("loup");
    Assertions.assertTrue(result2.isEmpty());
  }

  @Test
  void addProductExist() {
    // Check for duplicate
    Assertions.assertEquals(
        new UpdateResult(UpdateStatus.DUPLICATE), cartService.addProduct(USERNAME, PRODUCT_1));

    // Add the product
    Assertions.assertEquals(
        UpdateResult.success(),
        cartService.addProduct(USERNAME, new CartProduct("3", null, null, 5)));
    int result2 = cartService.getAllProduct(USERNAME).size();
    Assertions.assertEquals(3, result2);
  }

  @Test
  void addProductDoesNotExist() {
    Assertions.assertEquals(
        new UpdateResult(UpdateStatus.INVALID_REFERENCE),
        cartService.addProduct(USERNAME, new CartProduct("F", null, null, 5)));
  }

  @Test
  void addProductNonValidQuantity() {
    Assertions.assertEquals(
        new UpdateResult(UpdateStatus.INVALID_CHECK),
        cartService.addProduct(USERNAME, new CartProduct("3", null, null, 0)));
  }

  @Test
  void deleteProductExist() {
    cartService.deleteProduct(USERNAME, "1");
    int result1 = cartService.getAllProduct(USERNAME).size();
    Assertions.assertEquals(1, result1);
  }

  @Test
  void deleteProductDoesNotExist() {
    cartService.deleteProduct(USERNAME, "M");
    int result1 = cartService.getAllProduct(USERNAME).size();
    Assertions.assertEquals(2, result1);
  }

  @Test
  void updateProductQuantity() {
    Assertions.assertEquals(
        UpdateResult.success(), cartService.updateProductQuantity(USERNAME, "1", 11));
    List<CartProduct> result1 = cartService.getAllProduct(USERNAME);
    Assertions.assertEquals(11, result1.get(0).getQuantity());
  }

  @Test
  void updateProductQuantityNonValid() {
    Assertions.assertEquals(
        new UpdateResult(UpdateStatus.INVALID_CHECK),
        cartService.updateProductQuantity(USERNAME, "1", -5));
  }

  @Test
  void clearCart() {
    cartService.clear(USERNAME);
    List<CartProduct> result1 = cartService.getAllProduct(USERNAME);
    Assertions.assertTrue(result1.isEmpty());
  }

  @Test
  void deleteCart() {
    cartService.delete(USERNAME);
    List<CartProduct> result1 = cartService.getAllProduct(USERNAME);
    Assertions.assertTrue(result1.isEmpty());
  }

  @Test
  void addCart() {
    // Check for duplicate
    Assertions.assertEquals(new UpdateResult(UpdateStatus.DUPLICATE), cartService.add(USERNAME));

    // Add the cart
    Assertions.assertEquals(UpdateResult.success(), cartService.add("loup"));

    // Check the cart was added
    cartService.addProduct("loup", PRODUCT_1);
    int result2 = cartService.getAllProduct("loup").size();
    Assertions.assertEquals(1, result2);
  }
}
