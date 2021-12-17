package ch.heigvd.amt.service;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.database.exception.DuplicateEntryException;
import ch.heigvd.amt.database.exception.InvalidCheckConditionException;
import ch.heigvd.amt.database.exception.InvalidReferenceException;
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
class CartServiceTest {

  private static final String USERNAME = "test";
  private static final CartProduct PRODUCT_1 = new CartProduct("1", null, null, 1);
  private static final CartProduct PRODUCT_2 = new CartProduct("2", null, null, 2);

  @Inject DataSource dataSource;

  @Inject CartService cartService;

  @BeforeEach
  void setupEach() {
    PostgisResource.runQuery(
        dataSource, "sql/reset_db.sql", "sql/insert_product.sql", "sql/insert_cart.sql");
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
    // Add the product
    Assertions.assertDoesNotThrow(
        () -> cartService.addProduct(USERNAME, new CartProduct("3", null, null, 5)));
    int result2 = cartService.getAllProduct(USERNAME).size();
    Assertions.assertEquals(3, result2);
  }

  @Test
  void addProductDoesNotExist() {
    Assertions.assertThrows(
        InvalidReferenceException.class,
        () -> cartService.addProduct(USERNAME, new CartProduct("F", null, null, 5)));
  }

  @Test
  void addProductNonValidQuantity() {
    Assertions.assertThrows(
        InvalidCheckConditionException.class,
        () -> cartService.addProduct(USERNAME, new CartProduct("3", null, null, 0)));
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
    Assertions.assertDoesNotThrow(() -> cartService.updateProductQuantity(USERNAME, "1", 11));
    List<CartProduct> result1 = cartService.getAllProduct(USERNAME);
    Assertions.assertEquals(11, result1.get(0).getQuantity());
  }

  @Test
  void updateProductQuantityNonValid() {
    Assertions.assertThrows(
        InvalidCheckConditionException.class,
        () -> cartService.updateProductQuantity(USERNAME, "1", -5));
  }

  @Test
  void clearCart() {
    cartService.clearCart(USERNAME);
    List<CartProduct> result1 = cartService.getAllProduct(USERNAME);
    Assertions.assertTrue(result1.isEmpty());
  }

  @Test
  void deleteCart() {
    cartService.deleteCart(USERNAME);
    List<CartProduct> result1 = cartService.getAllProduct(USERNAME);
    Assertions.assertTrue(result1.isEmpty());
  }

  @Test
  void addCart() {
    // Check for duplicate
    Assertions.assertThrows(DuplicateEntryException.class, () -> cartService.addCart(USERNAME));

    // Add the cart
    Assertions.assertDoesNotThrow(() -> cartService.addCart("loup"));

    // Check the cart was added
    cartService.addProduct("loup", PRODUCT_1);
    int result2 = cartService.getAllProduct("loup").size();
    Assertions.assertEquals(1, result2);
  }
}
