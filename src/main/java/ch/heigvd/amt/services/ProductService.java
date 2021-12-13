package ch.heigvd.amt.services;

import ch.heigvd.amt.database.UpdateHandler;
import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.utils.ResourceLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@ApplicationScoped
public class ProductService {

  public static final String CATEGORY_LIST = "categoryList";
  public static final String NAME = "name";
  public static final String PRODUCT_NAME = "product_name";
  public static final String CATEGORY_NAME = "category_name";
  public static final String PRICE = "price";
  public static final String DESCRIPTION = "description";
  public static final String QUANTITY = "quantity";
  public static final String IMAGE_ID = "image_id";
  public static final String LIST = "list";
  private final Jdbi jdbi;
  private final UpdateHandler updateHandler;

  private static final Logger logger = Logger.getLogger(ProductService.class);

  @Inject
  public ProductService(Jdbi jdbi, UpdateHandler updateHandler) {
    this.jdbi = jdbi;
    this.updateHandler = updateHandler;
  }

  /**
   * Get all product from database
   *
   * @return a list of product present in the database
   */
  public List<Product> getAllProduct() {
    return getAllProduct(Collections.emptyList());
  }

  /**
   * Get all product with selected category from database
   *
   * @param categories A list of the categories to filter by
   * @return a list of product present in the database with the given filter applied
   */
  public List<Product> getAllProduct(List<String> categories) {
    return new ArrayList<>(
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(
                            ResourceLoader.loadResource("sql/product/getAllWithCategoryFilter.sql"))
                        .bindArray(CATEGORY_LIST, String.class, categories)
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
                    .bind(NAME, name)
                    .registerRowMapper(ConstructorMapper.factory(Product.class, "p"))
                    .registerRowMapper(ConstructorMapper.factory(Category.class, "c"))
                    .reduceRows(new LinkedHashMap<>(), accumulateProductRow()))
        .values()
        .stream()
        .findFirst();
  }

  /**
   * add a category to a product. Does nothing if the category is associated with the product but
   * will still return SUCCESS
   *
   * @param productName the name of the product
   * @param categoryName the name of the category
   */
  public void addCategory(String productName, String categoryName) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/product/addCategory.sql"))
                  .bind(PRODUCT_NAME, productName)
                  .bind(CATEGORY_NAME, categoryName)
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      updateHandler.handleUpdateError(e);
    }
  }

  /**
   * Delete a category from a product
   *
   * @param productName the identifier of the product
   * @param categoryName the identifier of the category
   */
  public void removeCategory(String productName, String categoryName) {
    jdbi.useHandle(
        handle ->
            handle
                .createUpdate(ResourceLoader.loadResource("sql/product/removeCategory.sql"))
                .bind(PRODUCT_NAME, productName)
                .bind(CATEGORY_NAME, categoryName)
                .execute());
  }

  /**
   * Create a new product
   *
   * @param product the identifier of the product
   */
  public void addProduct(Product product) {
    try {
      jdbi.useHandle(
          handle ->
              handle
                  .createUpdate(ResourceLoader.loadResource("sql/product/add.sql"))
                  .bindBean(product)
                  .bind(IMAGE_ID, product.getImage().getId())
                  .execute());
    } catch (UnableToExecuteStatementException e) {
      updateHandler.handleUpdateError(e);
    }
  }

  /**
   * Update a product Note that updating a non-existing product will not fail and will return
   * success
   *
   * @param product the product with updated data.
   * @return the status of the operation
   */
  public void updateProduct(Product product) {
    String toUpdate = "price=:price, quantity=:quantity";
    if (product.getImage() == null) {
      try {
        jdbi.useHandle(
            handle ->
                handle
                    .createUpdate(ResourceLoader.loadResource("sql/product/update.sql"))
                    .define(LIST, toUpdate)
                    .bindBean(product)
                    .execute());
      } catch (UnableToExecuteStatementException e) {
        updateHandler.handleUpdateError(e);
      }
    } else {
      try {
        jdbi.useHandle(
            handle ->
                handle
                    .createUpdate(ResourceLoader.loadResource("sql/product/update.sql"))
                    .define(
                        LIST,
                        product.getImage() == null ? toUpdate : toUpdate + ", image_id=:image_id")
                    .bindBean(product)
                    .bind(IMAGE_ID, product.getImage().getId())
                    .execute());
      } catch (UnableToExecuteStatementException e) {
        updateHandler.handleUpdateError(e);
      }
    }
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
        product
            .getCategories()
            .addAll(rowView.getColumn("c_name", new GenericType<List<Category>>() {}));
      }
      return map;
    };
  }
}
