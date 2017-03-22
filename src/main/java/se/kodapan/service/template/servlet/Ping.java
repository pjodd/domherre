package se.kodapan.service.template.servlet;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Data;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author kalle
 * @since 2017-03-07 22:31
 */
@Path("api")
public class Ping {

  @Inject
  @Named("service name")
  private String serviceName;

  @Path("ping")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Pong ping() {
    return new Pong();
  }

  @Data
  public class Pong {
    private String serviceName = Ping.this.serviceName;
  }

}
