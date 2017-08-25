package se.kodapan.service.template.mq;

/**
 * @author kalle
 * @since 2017-03-07 21:05
 */
public interface MessageQueueFactory {

  public abstract MessageQueueReader readerFactory(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer);

  public abstract MessageQueueWriter writerFactory();

}
