package se.kodapan.service.template.mq.devnull;

import se.kodapan.service.template.mq.MessageQueueConsumer;
import se.kodapan.service.template.mq.MessageQueueReader;
import se.kodapan.service.template.mq.MessageQueueTopic;

/**
 * Created by kalle on 2017-03-22.
 */
public class DevNullMessageQueueReader implements MessageQueueReader {

  private MessageQueueTopic topic;
  private MessageQueueConsumer consumer;

  public DevNullMessageQueueReader(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    this.topic = topic;
    this.consumer = consumer;
  }

  @Override
  public boolean open() throws Exception {
    return true;
  }

  @Override
  public boolean close() throws Exception {
    return true;
  }

  @Override
  public MessageQueueTopic getTopic() {
    return topic;
  }

  @Override
  public MessageQueueConsumer getConsumer() {
    return consumer;
  }
}
