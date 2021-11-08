package ch.heigvd.amt.services;

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
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.result.RowView;

@ApplicationScoped
public class ProductService {

  private final Jdbi jdbi;

  @Inject
  public ProductService(Jdbi jdbi) {
    this.jdbi = jdbi;
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
      throw new IllegalArgumentException("Filter cannot be empty");
    }
    return new ArrayList<>(
        jdbi.withHandle(
                handle ->
                    handle
                        .createQuery(
                            ResourceLoader.loadResource("sql/product/getAllWithCategoryFilter.sql"))
                        .bindList("categoryList", categories) // even if the list
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
