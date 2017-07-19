package se.kodapan.service.template.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

/**
 * @author kalle
 * @since 2017-02-17 17:54
 */
public abstract class AbstractMessageQueueReader implements MessageQueueReader {

  @Getter
  private MessageQueueTopic topic;

  @Getter
  private MessageQueueConsumer consumer;

  @Getter
  private ObjectMapper objectMapper;

  public AbstractMessageQueueReader(MessageQueueTopic topic, MessageQueueConsumer consumer, ObjectMapper objectMapper) {
    this.topic = topic;
    this.consumer = consumer;
    this.objectMapper = objectMapper;
  }
  
}
