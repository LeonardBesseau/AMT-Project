package ch.heigvd.amt;
import ch.heigvd.amt.models.Product;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.postgres.PostgresPlugin;

public class Application {

  public static void main(String[] args) {
    Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/amt?user=amt&password=amt");
    jdbi.installPlugin(new PostgresPlugin());
    jdbi.registerRowMapper(ConstructorMapper.factory(Product.class));
    jdbi.useHandle(handle -> {
      handle.createQuery("SELECT * FROM products").mapTo(Product.class).forEach(System.out::println);
    });
  }

}
