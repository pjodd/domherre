package se.kodapan.service.template.mq;

import com.google.inject.ImplementedBy;

/**
 * @author kalle
 * @since 2017-03-07 21:05
 */
@ImplementedBy(se.kodapan.service.template.mq.kafka.KafkaFactory.class)
public interface MessageQueueFactory {

  public abstract MessageQueueReader readerFactory(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer);

  public abstract MessageQueueWriter writerFactory();

  /** @return Offset of message at end of queue */
  public abstract long getQueueEndOffset(MessageQueueTopic topic);

}
