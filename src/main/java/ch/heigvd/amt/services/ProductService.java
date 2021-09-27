package ch.heigvd.amt.services;

import ch.heigvd.amt.models.Product;
import java.util.List;
import javax.inject.Inject;
import org.jdbi.v3.core.Jdbi;

public class ProductService {

  private final Jdbi jdbi;

  @Inject
  public ProductService(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  public List<Product> getAllProduct() {
    return jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM products").mapTo(Product.class).list());
  }
}
