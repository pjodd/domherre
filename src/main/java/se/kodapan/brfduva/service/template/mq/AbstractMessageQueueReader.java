package se.kodapan.brfduva.service.template.mq;

import lombok.Data;

/**
 * @author kalle
 * @since 2017-02-17 17:54
 */
@Data
public abstract class AbstractMessageQueueReader implements MessageQueueReader {

  private MessageQueueTopic topic;
  private MessageQueueConsumer consumer;

  @Override
  public void registerConsumer(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    setConsumer(consumer);
    setTopic(topic);
  }
  
}
