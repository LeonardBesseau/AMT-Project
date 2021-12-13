package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateHandler;
import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.utils.ResourceLoader;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class CartService {

  public static final String USERNAME = "username";
  private final Jdbi jdbi;
  private final UpdateHandler updateHandler;

  @Inject
  public CartService(Jdbi jdbi, UpdateHandler updateHandler) {
    this.jdbi = jdbi;
    this.updateHandler = updateHandler;
  }

  /**
   * Get all products of a specific user cart
   *
   * @param username name of the user
   * @return a list of products
   */
  public List<CartProduct> getAllProduct(String username) {
    return new ArrayList<>(
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(ResourceLoader.loadResource("sql/cart/getAllProduct.sql"))
                    .bind(USERNAME, username)
                    .mapTo(CartProduct.class)
                    .list()));
  }

  /**
   * Add a product to the cart of a specific user, update the quantity if it already exists
   *
   * @param username name of the user
   * @param cartProduct cartProduct to add
   * @return the result of the operation
   */
  public UpdateResult addProduct(String username, CartProduct cartProduct) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/cart/addProduct.sql"))
                  .bind(USERNAME, username)
                  .bind("name", cartProduct.getName())
                  .bind("quantity", cartProduct.getQuantity())
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      return updateHandler.handleUpdateError(e);
    }
    return UpdateResult.success();
  }

  /**
   * Update the quantity of a product in the cart specific to the user
   *
   * @param username name of the user
   * @param name name of the product
   * @param newQuantity the new quantity to put
   * @return the result of the operation
   */
  public UpdateResult updateProductQuantity(String username, String name, int newQuantity) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/cart/updateProductQuantity.sql"))
                  .bind(USERNAME, username)
                  .bind("name", name)
                  .bind("quantity", newQuantity)
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      return updateHandler.handleUpdateError(e);
    }
    return UpdateResult.success();
  }

  /**
   * Delete a product from the cart of a secific user
   *
   * @param username name of the user
   * @param name name of the product
   */
  public void deleteProduct(String username, String name) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/deleteProduct.sql"))
                .bind(USERNAME, username)
                .bind("name", name)
                .execute());
  }

  /**
   * Clear the cart specific to the user
   *
   * @param username name of the user
   */
  public void clearCart(String username) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/clear.sql"))
                .bind(USERNAME, username)
                .execute());
  }

  /**
   * Delete the cart specific to the user
   *
   * @param username name of the user
   */
  public void deleteCart(String username) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/delete.sql"))
                .bind(USERNAME, username)
                .execute());
  }

  /**
   * Add a cart specific to the user
   *
   * @param username name of the user
   * @return the result of the operation
   */
  public UpdateResult addCart(String username) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/cart/add.sql"))
                  .bind(USERNAME, username)
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      return updateHandler.handleUpdateError(e);
    }
    return UpdateResult.success();
  }
}
