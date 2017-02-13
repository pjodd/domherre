package se.kodapan.brfduva.service.template.prevalence;

import java.time.OffsetDateTime;

/**
 * @author kalle
 * @since 2017-02-12 22:06
 */
public class Prevalence<Root> {

  private Root root;

  public <Response> Response execute(Query<Root, Response> query) throws Exception {
    return query.execute(root, OffsetDateTime.now());
  }

  public <Response, Payload> Response execute(Transaction<Root, Payload, Response> transaction, Payload payload) throws Exception {
    return transaction.execute(root, payload, OffsetDateTime.now());
  }



}
