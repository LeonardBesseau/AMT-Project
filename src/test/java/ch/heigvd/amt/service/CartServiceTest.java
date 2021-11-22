package ch.heigvd.amt.service;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.services.CartService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.List;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
public class CartServiceTest {

    private static final int USER_ID = 1;
    private static final CartProduct PRODUCT_1 = new CartProduct("1", null, null, 1);
    private static final CartProduct PRODUCT_2 = new CartProduct("2", null, null, 2);


    @Inject
    DataSource dataSource;

    @Inject
    CartService cartService;

    @BeforeEach
    void setupEach() {
        PostgisResource.runQuery(
                dataSource, "sql/init_db.sql", "sql/reset_db.sql", "sql/insert_product.sql", "sql/insert_cart.sql");
    }

    @Test
    void getAllProductCartExist() {
        List<CartProduct> result1 = cartService.getAllProduct(USER_ID);
        Assertions.assertEquals(2, result1.size());
    }

    @Test
    void getAllProductCartDoesNotExist() {
        List<CartProduct> result2 = cartService.getAllProduct(0);
        Assertions.assertTrue(result2.isEmpty());
    }

    @Test
    void addProductExist() {
        // Check for duplicate
        Assertions.assertEquals(new UpdateResult(UpdateStatus.DUPLICATE), cartService.addProduct(USER_ID, PRODUCT_1));

        // Add the product
        Assertions.assertEquals(UpdateResult.success(),
                cartService.addProduct(USER_ID, new CartProduct("3", null, null, 5)));
        int result2 = cartService.getAllProduct(USER_ID).size();
        Assertions.assertEquals(3, result2);
    }

    @Test
    void addProductDoesNotExist() {
        Assertions.assertEquals(new UpdateResult(UpdateStatus.INVALID_REFERENCE),
                cartService.addProduct(USER_ID, new CartProduct("F", null, null, 5)));
    }

    @Test
    void addProductNonValidQuantity() {
        Assertions.assertEquals(new UpdateResult(UpdateStatus.INVALID_CHECK),
                cartService.addProduct(USER_ID, new CartProduct("3", null, null, 0)));
    }

    @Test
    void deleteProductExist() {
        cartService.deleteProduct(USER_ID, "1");
        int result1 = cartService.getAllProduct(USER_ID).size();
        Assertions.assertEquals(1, result1);
    }

    @Test
    void deleteProductDoesNotExist() {
        cartService.deleteProduct(USER_ID, "M");
        int result1 = cartService.getAllProduct(USER_ID).size();
        Assertions.assertEquals(2, result1);
    }

    @Test
    void updateProductQuantity() {
        Assertions.assertEquals(UpdateResult.success(),
                cartService.updateProductQuantity(USER_ID, "1", 11));
        List<CartProduct> result1 = cartService.getAllProduct(USER_ID);
        Assertions.assertEquals(11, result1.get(0).getQuantity());
    }

    @Test
    void updateProductQuantityNonValid() {
        Assertions.assertEquals(new UpdateResult(UpdateStatus.INVALID_CHECK),
                cartService.updateProductQuantity(USER_ID, "1", -5));
    }

    @Test
    void clearCart() {
        cartService.clear(USER_ID);
        List<CartProduct> result1 = cartService.getAllProduct(USER_ID);
        Assertions.assertTrue(result1.isEmpty());
    }

    @Test
    void deleteCart() {
        cartService.delete(USER_ID);
        List<CartProduct> result1 = cartService.getAllProduct(USER_ID);
        Assertions.assertTrue(result1.isEmpty());
    }

    @Test
    void addCart() {
        // Check for duplicate
        Assertions.assertEquals(new UpdateResult(UpdateStatus.DUPLICATE), cartService.add(USER_ID));

        // Add the cart
        Assertions.assertEquals(UpdateResult.success(),
                cartService.add(2));

        // Check the cart was added
        cartService.addProduct(2, PRODUCT_1);
        int result2 = cartService.getAllProduct(2).size();
        Assertions.assertEquals(1, result2);
    }
}
