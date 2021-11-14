package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateStatus;
import ch.heigvd.amt.database.UpdateResultHandler;
import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.utils.ResourceLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class CategoryService {

  private final Jdbi jdbi;

  private final UpdateResultHandler updateResultHandler;

  @Inject
  public CategoryService(Jdbi jdbi, UpdateResultHandler updateResultHandler) {
    this.jdbi = jdbi;
    this.updateResultHandler = updateResultHandler;
  }

  /**
   * Get all product from databases
   *
   * @return a list of product
   */
  public List<Category> getAllCategory() {
    return new ArrayList<>(
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(ResourceLoader.loadResource("sql/category/getAll.sql"))
                    .mapTo(Category.class)
                    .list()));
  }

  /**
   * Get a product from the database
   *
   * @param name the name of the product
   * @return an optional. Contains the product if it exists
   */
  public Optional<Category> getCategory(String name) {
    return jdbi.withHandle(
        handle ->
            handle
                .createQuery(ResourceLoader.loadResource("sql/category/get.sql"))
                .bind("name", name)
                .mapTo(Category.class)
                .findOne());
  }

  /**
   * Delete a category
   *
   * @param name the name of the category to delete
   */
  public void deleteCategory(String name) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(ResourceLoader.loadResource("sql/category/delete.sql"))
                .bind("name", name)
                .execute());
  }

  /**
   * Add category to the database
   *
   * @param category the name of the new category
   * @return the result of the operation
   */
  public UpdateResult addCategory(Category category) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/category/add.sql"))
                  .bind("name", category.getName())
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      return updateResultHandler.handleUpdateError(e);
    }
    return UpdateResult.success();
  }
}
