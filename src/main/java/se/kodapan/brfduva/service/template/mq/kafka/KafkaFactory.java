package se.kodapan.brfduva.service.template.mq.kafka;

import com.google.inject.Singleton;
import se.kodapan.brfduva.service.template.mq.*;

/**
 * @author kalle
 * @since 2017-03-07 21:07
 */
@Singleton
public class KafkaFactory implements MessageQueueFactory {

  @Override
  public MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    return new KafkaReader(topic, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new KafkaWriter();
  }
}
