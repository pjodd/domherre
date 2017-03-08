package se.kodapan.brfduva.service.template.mq;

/**
 * @author kalle
 * @since 2017-03-07 21:05
 */
public interface MessageQueueFactory {

  public abstract MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer);
  public abstract MessageQueueWriter writerFactory();

}
