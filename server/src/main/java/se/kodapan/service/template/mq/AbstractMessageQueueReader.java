package se.kodapan.service.template.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

/**
 * @author kalle
 * @since 2017-02-17 17:54
 */
public abstract class AbstractMessageQueueReader implements MessageQueueReader {

  @Getter
  private MessageQueueReaderConfiguration configuration;

  @Getter
  private MessageQueueConsumer consumer;

  @Getter
  private ObjectMapper objectMapper;

  public AbstractMessageQueueReader(MessageQueueReaderConfiguration configuration, MessageQueueConsumer consumer, ObjectMapper objectMapper) {
    this.configuration = configuration;
    this.consumer = consumer;
    this.objectMapper = objectMapper;
  }

}
