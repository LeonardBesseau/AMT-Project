package ch.heigvd.amt.provider;

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

@Singleton
public class JdbiProvider {

  // TODO DPO: C'est intéressant d'utiliser JDBI. Je connaissais pas et je suis aller me renseigner. Merci. J'utilise JDBC en
  //  général avec un ORM comme hibernate pour faciliter les requêtes. Je n'ai pas vu dans le wiki pourquoi vous avez
  //  choisi cette technologie. ça m'intéresse dans discuter durant la prochaine review.
  private final Jdbi jdbi;

  @Inject
  public JdbiProvider(DataSource dataSource) {
    jdbi = Jdbi.create(dataSource).installPlugin(new PostgresPlugin());
    jdbi.registerRowMapper(ConstructorMapper.factory(Product.class));
    jdbi.registerRowMapper(ConstructorMapper.factory(Category.class));
    jdbi.registerRowMapper(ConstructorMapper.factory(Image.class));
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
