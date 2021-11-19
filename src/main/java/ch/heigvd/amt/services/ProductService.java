package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateResult;
import ch.heigvd.amt.database.UpdateResultHandler;
import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.utils.ResourceLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class ProductService {

  private final Jdbi jdbi;
  private final UpdateResultHandler updateResultHandler;

  private static final Logger logger = Logger.getLogger(ProductService.class);

  @Inject
  public ProductService(Jdbi jdbi, UpdateResultHandler updateResultHandler) {
    this.jdbi = jdbi;
    this.updateResultHandler = updateResultHandler;
  }

  /**
   * Get all product from database
   *
   * @return a list of product present in the database
   */
  public List<Product> getAllProduct() {
    return new ArrayList<>(
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(ResourceLoader.loadResource("sql/product/getAll.sql"))
                        .registerRowMapper(ConstructorMapper.factory(Product.class, "p"))
                        .registerRowMapper(ConstructorMapper.factory(Category.class, "c"))
                        .reduceRows(new LinkedHashMap<>(), accumulateProductRow()))
            .values());
  }

  /**
   * Get all product with selected category from database
   *
   * @param categories A list of the categories to filter by
   * @return a list of product present in the database with the given filter applied
   */
  public List<Product> getAllProductForCategories(List<String> categories) {
    if (categories.isEmpty()) {
      return getAllProduct();
    }
    return new ArrayList<>(
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(
                            ResourceLoader.loadResource("sql/product/getAllWithCategoryFilter.sql"))
                        .bindList("categoryList", categories)
                        .registerRowMapper(ConstructorMapper.factory(Product.class, "p"))
                        .registerRowMapper(ConstructorMapper.factory(Category.class, "c"))
                        .reduceRows(new LinkedHashMap<>(), accumulateProductRow()))
            .values());
  }

  /**
   * Get a product from the database
   *
   * @param name the name of the product
   * @return an optional. Contains the product if it exists
   */
  public Optional<Product> getProduct(String name) {
    return jdbi
        .withHandle(
            handle ->
                handle
                    .createQuery(ResourceLoader.loadResource("sql/product/get.sql"))
                    .bind("name", name)
                    .registerRowMapper(ConstructorMapper.factory(Product.class, "p"))
                    .registerRowMapper(ConstructorMapper.factory(Category.class, "c"))
                    .reduceRows(new LinkedHashMap<>(), accumulateProductRow()))
        .values()
        .stream()
        .findFirst();
  }

  /**
   * add a category to a product. Does nothing if the category is associated with the product but will still return
   * SUCCESS
   *
   * @param productName  the name of the product
   * @param categoryName the name of the category
   * @return the result of the operation
   */
  public UpdateResult addCategory(String productName, String categoryName) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/product/addCategory.sql"))
                  .bind("product_name", productName)
                  .bind("category_name", categoryName)
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      return updateResultHandler.handleUpdateError(e);
    }
    return UpdateResult.success();
  }

  public void removeCategory(String productName, String categoryName) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(ResourceLoader.loadResource("sql/product/removeCategory.sql"))
                .bind("product_name", productName)
                .bind("category_name", categoryName)
                .execute());
  }

  public UpdateResult addProduct(Product product) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/product/add.sql"))
                  .bind("name", product.getName())
                  .bind("price", product.getPrice())
                  .bind("description", product.getDescription())
                  .bind("quantity", product.getQuantity())
                  .bind("image_id", product.getImage().getId())
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      return updateResultHandler.handleUpdateError(e);
    }
    return UpdateResult.success();
  }

  /**
   * Accumulator function for aggregating multiple categories for the same product
   *
   * @return a map of the of all the products with their categories aggregated
   */
  private BiFunction<LinkedHashMap<String, Product>, RowView, LinkedHashMap<String, Product>>
  accumulateProductRow() {
    return (map, rowView) -> {
      Product product =
          map.computeIfAbsent(
              rowView.getColumn("p_name", String.class), id -> rowView.getRow(Product.class));

      if (rowView.getColumn("c_name", String.class) != null) {
        product.getCategories().add(rowView.getRow(Category.class));
      }

      return map;
    };
  }
}
