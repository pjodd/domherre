package se.kodapan.brfduva.service.template.prevalence;

import java.time.OffsetDateTime;

/**
 * @author kalle
 * @since 2017-02-12 22:07
 */
public interface Transaction<Root, Payload, Response> {

  public abstract <Response> Response execute(Root root, Payload payload, OffsetDateTime date) throws Exception;

}
