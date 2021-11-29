package ch.heigvd.amt.provider;

import ch.heigvd.amt.models.CartProduct;
import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.models.Image;
import ch.heigvd.amt.models.Product;
import ch.heigvd.amt.services.mapper.CategoryColumnMapper;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.postgres.PostgresPlugin;

/** Manage db connection lifetime */
@Singleton
public class JdbiProvider {

  private final Jdbi jdbi;

  @Inject
  public JdbiProvider(DataSource dataSource) {
    jdbi = Jdbi.create(dataSource).installPlugin(new PostgresPlugin());
    jdbi.registerRowMapper(ConstructorMapper.factory(Product.class));
    jdbi.registerRowMapper(ConstructorMapper.factory(Category.class));
    jdbi.registerRowMapper(ConstructorMapper.factory(Image.class));
    jdbi.registerRowMapper(ConstructorMapper.factory(CartProduct.class));
    jdbi.registerColumnMapper(new CategoryColumnMapper());
    jdbi.registerArrayType(String.class, "TEXT");
  }

  /**
   * Get the jdbi singleton
   *
   * @return the jdbi singleton
   */
  @Produces
  public Jdbi jdbi() {
    return jdbi;
  }
}
