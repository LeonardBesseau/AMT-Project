package ch.heigvd.amt.database;

import ch.heigvd.amt.utils.ResourceLoader;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgisResource implements QuarkusTestResourceLifecycleManager {

  PostgreSQLContainer<?> postgis =
      new PostgreSQLContainer<>(
          DockerImageName.parse("postgis/postgis:13-3.1").asCompatibleSubstituteFor("postgres"))
          .withExposedPorts(5432)
          .withDatabaseName("tests")
          .withUsername("username")
          .withPassword("password");

  public static void runQuery(DataSource dataSource, String... resources) {
    try (Handle handle = Jdbi.open(dataSource)) {
      for (String resource : resources) {
        String script = ResourceLoader.loadResource(resource);
        handle.createScript(script).execute();
      }
    }
  }

  @Override
  public Map<String, String> start() {
    postgis.start();
    Map<String, String> config = new HashMap<>();
    config.put("quarkus.datasource.jdbc.url", postgis.getJdbcUrl());
    config.put("quarkus.datasource.username", postgis.getUsername());
    config.put("quarkus.datasource.password", postgis.getPassword());
    return config;
  }

  @Override
  public void stop() {
    postgis.stop();
  }
}
