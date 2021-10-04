package ch.heigvd.amt.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import ch.heigvd.amt.database.PostgisResource;
import ch.heigvd.amt.services.ProductService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(PostgisResource.class)
class ProductResourceTest {

  @Inject
   DataSource dataSource;


  @Test
  void testHelloEndpoint() {
    PostgisResource.runQuery(dataSource, "sql/create_products_table.sql");
    given()
        .when().get("/product")
        .then()
        .statusCode(200)
        .body(is("[]"));
  }

}
