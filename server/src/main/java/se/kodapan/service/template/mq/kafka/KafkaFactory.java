package se.kodapan.service.template.mq.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kodapan.service.template.mq.*;
import se.kodapan.service.template.util.Environment;

import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author kalle
 * @since 2017-03-07 21:07
 */
@Singleton
public class KafkaFactory implements MessageQueueFactory {

  @Inject
  private ObjectMapper objectMapper;

  @Override
  public MessageQueueReader readerFactory(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer) {
    return new KafkaReader(configuration, consumer, objectMapper);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new KafkaWriter(objectMapper);
  }

  @Override
  public long getQueueEndOffset(MessageQueueTopic topic) {
    try {
      return new KafkaGetQueueEndOffset(topic).execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
