package ch.heigvd.amt.resources;

import ch.heigvd.amt.database.PostgisResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
class ProductResourceTest {

  @Inject
  DataSource dataSource;


  @Test
  void testHelloEndpoint() {
    PostgisResource.runQuery(dataSource, "sql/init_db.sql", "sql/insert_product.sql");

  }
}
