package se.kodapan.service.template.prevalence;

import se.kodapan.service.template.mq.MessageQueueMessage;

import java.time.OffsetDateTime;

/**
 * @author kalle
 * @since 2017-02-12 22:07
 */
public interface Transaction<Root, Payload, Response> {

  /** @see MessageQueueMessage#getStereotype() */
  public abstract String getStereotype();

  /** @see MessageQueueMessage#getVersion() */
  public abstract Integer getVersion();

  public abstract Class<Payload> getPayloadClass();

  public abstract Class<Response> getResponseClass();

  /**
   * @param root
   * @param payload
   * @param created original transaction creation time
   * @return
   * @throws Exception
   */
  public abstract Response execute(Root root, Payload payload, OffsetDateTime created) throws Exception;

}
