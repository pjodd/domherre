package se.kodapan.service.template.servlet;

import com.google.inject.Singleton;
import se.kodapan.service.template.util.Tracking;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * @author kalle
 * @since 2017-09-29 00:46
 */
@Singleton
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

    HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
    httpServletResponse.setHeader(Tracking.httpHeader, Tracking.getInstance().get().toString());

    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }

  @Override
  public void destroy() {

  }
}
