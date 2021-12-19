package ch.heigvd.amt.resources;

import ch.heigvd.amt.mock.MockServerExtension;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(MockServerExtension.class)
class LoginTest {

  /*
  @Test
  void login() {
    given()
        .when()
        .formParams("username", "a", "password", "b")
        .post("/login/signin")
        .then()
        .statusCode(301);
  }
   */
}
