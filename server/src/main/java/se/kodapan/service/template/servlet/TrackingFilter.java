package se.kodapan.service.template.servlet;

import se.kodapan.service.template.util.Tracking;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * @author kalle
 * @since 2017-09-29 00:46
 */
public class TrackingFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    String value = httpServletRequest.getHeader(Tracking.httpHeader);
    if (value != null) {
      Tracking.getInstance().set(UUID.fromString(value));
    } else {
      Tracking.getInstance().set(null);
    }

  }

  @Override
  public void destroy() {

  }
}
