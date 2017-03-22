package se.kodapan.service.template.mq.devnull;

import se.kodapan.service.template.mq.*;

/**
 * Created by kalle on 2017-03-22.
 */
public class DevNullMessageQueueFactory implements MessageQueueFactory {

  @Override
  public MessageQueueReader readerFactory(MessageQueueTopic topic, MessageQueueConsumer consumer) {
    return new DevNullMessageQueueReader(topic, consumer);
  }

  @Override
  public MessageQueueWriter writerFactory() {
    return new DevNullMessageQueueWriter();
  }
}
