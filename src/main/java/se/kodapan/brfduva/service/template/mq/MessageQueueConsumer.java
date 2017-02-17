package se.kodapan.brfduva.service.template.mq;

/**
 * @author kalle
 * @since 2017-02-13 23:02
 */
public interface MessageQueueConsumer {

  public abstract void consume(MessageQueueMessage message);

}
