package ch.heigvd.amt.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Cookie;

public class CookieClientRequestFilter implements ClientRequestFilter {
  private final Cookie cookie;

  public CookieClientRequestFilter(String cookie) {
    super();
    this.cookie = new Cookie("jwt_token", cookie);
  }

  @Override
  public void filter(ClientRequestContext clientRequestContext) throws IOException {
    List<Object> cookies = new ArrayList<>();
    cookies.add(this.cookie);
    clientRequestContext.getHeaders().put("Cookie", cookies);
  }
}
