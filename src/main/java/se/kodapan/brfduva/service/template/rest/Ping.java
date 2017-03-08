package se.kodapan.brfduva.service.template.rest;

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

  @Path("ping")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Pong ping() {
    return new Pong();
  }

  public static class Pong {
    
  }

}
