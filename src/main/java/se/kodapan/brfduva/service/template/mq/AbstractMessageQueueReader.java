package se.kodapan.brfduva.service.template.mq;

import lombok.Data;
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

  public AbstractMessageQueueReader(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    this.topic = topic;
    this.consumer = consumer;
  }
  
}
