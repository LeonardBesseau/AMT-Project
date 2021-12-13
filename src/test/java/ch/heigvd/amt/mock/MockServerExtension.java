package ch.heigvd.amt.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.HashMap;
import java.util.Map;

public class MockServerExtension implements QuarkusTestResourceLifecycleManager {
  private WireMockServer wireMockServer;

  @Override
  public Map<String, String> start() {
    wireMockServer = new WireMockServer();
    wireMockServer.start();

    ObjectNode dataLogin =
        new ObjectMapper()
            .createObjectNode()
            .put(
                "token",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicm9sZSI6Ik1lbWJlciJ9.8dwWWPpQANT5Lb5bT1kU_8x0nmQP4SIBr4vRBoLzigE")
            .set(
                "account",
                new ObjectMapper().createObjectNode().put("role", "Member").put("username", "A"));

    stubFor(
        post(urlEqualTo("/auth/login"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(dataLogin.toString())));

    stubFor(
        post(urlEqualTo("/accounts/register"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"username\":\"A\"," + "\"role\":\"Member\"" + "}")));

    Map<String, String> config = new HashMap<>();
    // Hijack url at runtime
    config.put("auth.server.url", wireMockServer.baseUrl());
    return config;
  }

  @Override
  public void stop() {
    if (null != wireMockServer) {
      wireMockServer.stop();
    }
  }
}