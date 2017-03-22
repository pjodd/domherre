package se.kodapan.service.template.mq.localfs;

import se.kodapan.service.template.mq.MessageQueueConsumer;
import se.kodapan.service.template.mq.MessageQueueReader;
import se.kodapan.service.template.mq.MessageQueueTopic;

/**
 * Created by kalle on 2017-03-22.
 */
public class LocalFsMessageQueueReader implements MessageQueueReader {

  private LocalFsMessageQueue queue;
  private MessageQueueTopic topic;
  private MessageQueueConsumer consumer;

  public LocalFsMessageQueueReader(LocalFsMessageQueue queue, MessageQueueTopic topic, MessageQueueConsumer consumer) {
    this.queue = queue;
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
