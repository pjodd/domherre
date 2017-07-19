package se.kodapan.service.template.mq.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import se.kodapan.service.template.mq.*;

/**
 * @author kalle
 * @since 2017-03-07 21:07
 */
@Singleton
public class KafkaFactory implements MessageQueueFactory {

  @Inject
  private ObjectMapper objectMapper;

  @Override
  public MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    return new KafkaReader(topic, consumer, objectMapper);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new KafkaWriter();
  }
}
