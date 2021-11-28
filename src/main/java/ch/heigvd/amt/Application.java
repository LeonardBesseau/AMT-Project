package ch.heigvd.amt;

import ch.heigvd.amt.models.Category;
import ch.heigvd.amt.models.Product;
import org.jboss.logging.Logger;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.postgres.PostgresPlugin;

public class Application {

  private static final Logger logger = Logger.getLogger(Application.class);

  public static void main(String[] args) {
    // TODO DPO: Ah ben je comprends maintenant pourquoi ça fonctionnait pas en local chez moi...dur
    Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/amt?user=amt&password=amt");
    jdbi.installPlugin(new PostgresPlugin());
    // TODO DPO: Il manque pas l'objet image ? (accessoirement le code est pas le même dans la classe provider...)
    jdbi.registerRowMapper(ConstructorMapper.factory(Product.class));
    jdbi.registerRowMapper(ConstructorMapper.factory(Category.class));
    // TODO DPO - Hello to you too
    logger.info("HELLO\n");
  }
}
