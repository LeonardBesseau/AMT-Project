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
     * Get all products of a specific user cart
     *
     * @param username name of the user
     * @return a list of products
     */
    public List<CartProduct> getAllProduct(String username) {
        return new ArrayList<>(
                jdbi.withHandle(handle -> handle
                        .createQuery(ResourceLoader.loadResource("sql/cart/getAllProduct.sql"))
                        .bind("username", username)
                        .mapTo(CartProduct.class)
                        .list()));
    }

    /**
     * Add a product to the cart of a specific user, update the quantity if it already exists
     *
     * @param username    name of the user
     * @param cartProduct cartProduct to add
     * @return the result of the operation
     */
    public UpdateResult addProduct(String username, CartProduct cartProduct) {
        try {
            jdbi.useHandle(handle -> handle
                    .createUpdate(ResourceLoader.loadResource("sql/cart/addProduct.sql"))
                    .bind("username", username)
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
     * @param username    name of the user
     * @param name        name of the product
     * @param newQuantity the new quantity to put
     * @return the result of the operation
     */
    public UpdateResult updateProductQuantity(String username, String name, int newQuantity) {
        if (newQuantity > 0) {
            try {
                jdbi.useHandle(handle -> handle
                        .createUpdate(ResourceLoader.loadResource("sql/cart/updateProductQuantity.sql"))
                        .bind("username", username)
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
     * @param username name of the user
     * @param name     name of the product
     */
    public void deleteProduct(String username, String name) {
        jdbi.useHandle(handle -> handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/deleteProduct.sql"))
                .bind("username", username)
                .bind("name", name)
                .execute());
    }

    /**
     * Clear the cart specific to the user
     *
     * @param username name of the user
     */
    public void clear(String username) {
        jdbi.useHandle(handle -> handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/clear.sql"))
                .bind("username", username)
                .execute());
    }

    /**
     * Delete the cart specific to the user
     *
     * @param username name of the user
     */
    public void delete(String username) {
        jdbi.useHandle(handle -> handle
                .createUpdate(ResourceLoader.loadResource("sql/cart/delete.sql"))
                .bind("username", username)
                .execute());
    }

    /**
     * Add a cart specific to the user
     *
     * @param username name of the user
     * @return the result of the operation
     */
    public UpdateResult add(String username) {
        try {
            jdbi.useHandle(handle -> handle
                    .createUpdate(ResourceLoader.loadResource("sql/cart/add.sql"))
                    .bind("username", username)
                    .execute());
        } catch (UnableToExecuteStatementException e) {
            return updateResultHandler.handleUpdateError(e);
        }
        return UpdateResult.success();
    }
}
