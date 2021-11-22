package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateResultHandler;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.utils.ResourceLoader;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CartService {

    private final Jdbi jdbi;
    private final UpdateResultHandler updateResultHandler;

    @Inject
    public CartService(Jdbi jdbi, UpdateResultHandler updateResultHandler) {
        this.jdbi = jdbi;
        this.updateResultHandler = updateResultHandler;
    }

    /**
     * Get all product from a specific cart
     *
     * @return a list of product
     */
    public List<CartProduct> getAllProduct(int userId) {
        return new ArrayList<>(
                jdbi.withHandle(handle -> handle
                        .createQuery(ResourceLoader.loadResource("sql/cart/getAllProduct.sql"))
                        .bind("id", userId)
                        .mapTo(CartProduct.class)
                        .list()));
    }

    /**
     * Add a product to the cart of a specific user
     *
     * @param userId      id of the user
     * @param cartProduct cartProduct to add
     * @return the result of the operation
     */
    public UpdateResult addProduct(int userId, CartProduct cartProduct) {
        try {
            jdbi.useHandle(handle -> handle
                    .createUpdate(ResourceLoader.loadResource("sql/cart/addProduct.sql"))
                    .bind("id", userId)
                    .bind("name", cartProduct.getName())
                    .bind("quantity", cartProduct.getQuantity())
                    .execute());
        } catch (UnableToExecuteStatementException e) {
            return updateResultHandler.handleUpdateError(e);
        }
        return UpdateResult.success();
    }

    /**
     * Update the quantity of a product in the cart specific to the user
     *
     * @param userId      id of the user
     * @param name        name of the product
     * @param newQuantity the new quantity to put
     * @return the result of the operation
     */
    public UpdateResult updateProductQuantity(int userId, String name, int newQuantity) {
        if (newQuantity > 1) {
            try {
                jdbi.useHandle(handle -> handle
                        .createUpdate(ResourceLoader.loadResource("sql/cart/updateProductQuantity.sql"))
                        .bind("id", userId)
                        .bind("name", name)
                        .bind("quantity", newQuantity)
                        .execute());
            } catch (UnableToExecuteStatementException e) {
                return updateResultHandler.handleUpdateError(e);
            }
            return UpdateResult.success();
        } else {
            return new UpdateResult(UpdateStatus.INVALID_CHECK);
        }
    }

    /**
     * Delete a product from the cart of a secific user
     *
     * @param userId id of the user
     * @param name   name of the product
     */
    public void deleteProduct(int userId, String name) {
        jdbi.useHandle(handle -> handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/deleteProduct.sql"))
                .bind("id", userId)
                .bind("name", name)
                .execute());
    }

    /**
     * Clear the cart specific to the user
     *
     * @param userId id of the user
     */
    public void clear(int userId) {
        jdbi.useHandle(handle -> handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/clear.sql"))
                .bind("id", userId)
                .execute());
    }

    /**
     * Delete the cart specific to the user
     *
     * @param userId id of the user
     */
    public void delete(int userId) {
        jdbi.useHandle(handle -> handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/delete.sql"))
                .bind("id", userId)
                .execute());
    }

    /**
     * Add a cart specific to the user
     *
     * @param userId id of the user
     * @return the result of the operation
     */
    public UpdateResult add(int userId) {
        try {
            jdbi.useHandle(handle -> handle
                    .createUpdate(ResourceLoader.loadResource("sql/cart/add.sql"))
                    .bind("id", userId)
                    .execute());
        } catch (UnableToExecuteStatementException e) {
            return updateResultHandler.handleUpdateError(e);
        }
        return UpdateResult.success();
    }
}
