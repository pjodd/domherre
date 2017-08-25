package se.kodapan.service.template.prevalence;

import java.time.OffsetDateTime;

/**
 * @author kalle
 * @since 2017-02-12 22:07
 */
public interface Query<Root, Response> {

  public abstract Response execute(Root root, OffsetDateTime date) throws Exception;

}
