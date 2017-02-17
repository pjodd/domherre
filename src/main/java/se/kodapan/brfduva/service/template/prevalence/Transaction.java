package se.kodapan.brfduva.service.template.prevalence;

import java.time.OffsetDateTime;

/**
 * @author kalle
 * @since 2017-02-12 22:07
 */
public interface Transaction<Root, Payload, Response> {

  public abstract String getStereotype();
  public abstract Integer getVersion();
  public abstract Class<Payload> getPayloadClass();
  public abstract Class<Response> getResponseClass();

  public abstract Response execute(Root root, Payload payload) throws Exception;

}
