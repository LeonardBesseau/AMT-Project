package ch.heigvd.amt.services;

import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.utils.ResourceLoader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

@ApplicationScoped
public class ProductService {

  private final Jdbi jdbi;

  @Inject
  public ProductService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public List<Product> getAllProduct() {
    return jdbi.withHandle(
        handle -> handle.createQuery("SELECT * FROM product").mapTo(Product.class).list());
  }

  /**
   * Get a product from the database
   * @param name the name of the product
   * @return an optional. Contains the product if it exist
   */
  public Optional<Product> getProduct(String name) {
    return jdbi.withHandle(
        handle -> handle.createQuery(ResourceLoader.loadResource("sql/product/get.sql")).bind("name", name)
            .registerRowMapper(ConstructorMapper.factory(Product.class, "p"))
            .registerRowMapper(ConstructorMapper.factory(Category.class, "c"))
            .reduceRows(new LinkedHashMap<String, Product>(),
                (map, rowView) -> {
                  Product product = map.computeIfAbsent(
                      rowView.getColumn("p_name", String.class),
                      id -> rowView.getRow(Product.class));

                  if (rowView.getColumn("c_category_name", String.class) != null) {
                    product.getCategories().add(rowView.getRow(Category.class));
                  }

                  return map;
                })).values().stream().findFirst();
  }
}
